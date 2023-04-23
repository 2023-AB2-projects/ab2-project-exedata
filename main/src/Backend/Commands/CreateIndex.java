package Backend.Commands;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Backend.Common;
import Backend.Databases.*;
import Backend.MongoDBManagement.MongoDB;
import Backend.Parser;
import Backend.SaveLoadJSON.LoadJSON;
import Backend.SaveLoadJSON.SaveJSON;
import Backend.SocketServer.ErrorClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;

import static com.fasterxml.jackson.databind.type.LogicalType.Map;

public class CreateIndex implements Command {

    private final String command;
    private Databases databases;

    public CreateIndex(String command) {
        this.command = command;
    }

    @Override
    public void performAction() {
        //CREATE INDEX index_name
        //ON table_name (column1, column2, ...);
        databases = LoadJSON.load("databases.json");
        if (databases == null) {
            ErrorClient.send("Databases doesn't exists!");
            return;
        }
        Pattern pattern = Pattern.compile("^\\s*CREATE\\s+INDEX\\s+([A-Za-z0-9]+)\\s+ON\\s+([A-Za-z0-9]+)\\s+\\((.*)\\)\\s*;?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(command);
        String indexName;
        String tableName;
        String[] attributeNames;
        if (matcher.matches()) {
            indexName = matcher.group(1);
            tableName = matcher.group(2);
            attributeNames = matcher.group(3).replaceAll(" ", "").split(",");
            if (!databases.getDatabase(Parser.currentDatabaseName).getTable(tableName).existIndexName(indexName)) {
                if (databases.getDatabase(Parser.currentDatabaseName) != null) {
                    if (databases.getDatabase(Parser.currentDatabaseName).checkTableExists(tableName)) {
                        if (createIndex(indexName, tableName, attributeNames)) {
                            //createEmptyIndexFile(indexName + ".ind");
                            SaveJSON.save(databases, "databases.json");
                        } else {
                            ErrorClient.send("Syntax error!");
                        }
                    } else {
                        ErrorClient.send("Table doesn't exists!");
                    }
                } else {
                    ErrorClient.send("Databases doesn't exists!");
                }
            } else {
                ErrorClient.send("IndexName already exists!");
            }

            // insert index file to MongoDB
            createIndexFileInMongoDB(indexName, tableName, attributeNames);
        } else {
            ErrorClient.send("Wrong command!");
        }
    }

    private boolean createIndex(String IndexName, String tableName, String[] attributeNames) {
        //table.addIndexFile(new IndexFile(currentTableName, currentTableName + ".ind", attributeName));
        String indexFileName = IndexName + ".ind";
        String column;
        List<String> indexAttributes = new ArrayList<>();
        String isUnique = "0";
        for (String attributeName : attributeNames) {
            if (databases.getDatabase(Parser.currentDatabaseName).getTable(tableName).checkAttributeExists(attributeName)) {
                indexAttributes.add(attributeName);
                if (databases.getDatabase(Parser.currentDatabaseName).getTable(tableName).isUnique(attributeName))
                    isUnique = "1";
            } else {
                ErrorClient.send("Column doesn't exists!");
                return false;
            }
        }
        databases.getDatabase(Parser.currentDatabaseName).getTable(tableName).addIndexFile(new IndexFile(IndexName, indexAttributes, isUnique));
        return true;
    }

    protected static void createEmptyIndexFile(String indexFileName) {
        try (FileWriter fileWriter = new FileWriter(indexFileName)) {
            fileWriter.write("");
            fileWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createIndexFileInMongoDB(String indexName, String tableName, String[] attributeNames) {
        // Create index file (collection) in MongoDB

        // Connection
        MongoDB mongoDB = new MongoDB();
        mongoDB.createDatabaseOrUse(Parser.currentDatabaseName);
        mongoDB.createCollection(indexName);
        List<Attribute> attributeList = databases.getDatabase(Parser.currentDatabaseName).getTable(tableName).getStructure();
        List<String> primaryKeyList = databases.getDatabase(Parser.currentDatabaseName).getTable(tableName).getPrimaryKey();

        // Get all documents from a table
        MongoCollection<Document> collection = mongoDB.getDocuments(tableName);

        // check that index attributes will be unique or not
        if (isUnique(tableName, attributeNames)) {
            // add documents to indexFile (unique)
            try (MongoCursor<Document> cursor = collection.find().iterator()) {
                while (cursor.hasNext()) {
                    Document document = cursor.next();
                    StringBuilder keyIndexFile = new StringBuilder();

                    // build string (new key)
                    for (int i = 0; i < attributeNames.length; i++) {
                        String value = Common.getValueByAttributeName(document, attributeNames[i], primaryKeyList, attributeList);
                        keyIndexFile.append(value).append("#");
                    }

                    keyIndexFile = new StringBuilder(keyIndexFile.substring(0, keyIndexFile.length() - 1));
                    String valueIndexFile = (String) document.get("_id");

                    Document documentNew = new Document();
                    documentNew.append("_id", keyIndexFile.toString());
                    documentNew.append("Value", valueIndexFile);
                    mongoDB.insertOne(indexName, documentNew);
                }
            }
        } else {
            // add documents to indexFile (not unique)
            HashMap<String, String> map = new HashMap<>();
            try (MongoCursor<Document> cursor = collection.find().iterator()) {
                while (cursor.hasNext()) {
                    Document document = cursor.next();
                    StringBuilder keyIndexFile = new StringBuilder();

                    // build string (new key)
                    for (int i = 0; i < attributeNames.length; i++) {
                        String value = Common.getValueByAttributeName(document, attributeNames[i], primaryKeyList, attributeList);
                        keyIndexFile.append(value).append("#");
                    }

                    keyIndexFile = new StringBuilder(keyIndexFile.substring(0, keyIndexFile.length() - 1));
                    String valueIndexFile = (String) document.get("_id");

                    if (!map.containsKey(keyIndexFile.toString())) {
                        map.put(keyIndexFile.toString(), valueIndexFile);
                    } else {
                        map.put(keyIndexFile.toString(), map.get(keyIndexFile.toString()) + "#" + valueIndexFile);
                    }
                }
            }

            for (String key : map.keySet()) {
                String value = map.get(key);
                Document documentNew = new Document();
                documentNew.append("_id", key);
                documentNew.append("Value", value);
                mongoDB.insertOne(indexName, documentNew);
            }
        }
    }

    private boolean isUnique(String tableName, String[] attributeNames) {
        Table table = databases.getDatabase(Parser.currentDatabaseName).getTable(tableName);
        int count = 0;
        for (String attribute : attributeNames) {
            if (table.isUnique(attribute)) {
                return true;
            } else if (table.isPrimaryKey(attribute)) {
                count++;
            }
        }
        if (table.getPrimaryKey().size() == count) {
            return true;
        }
        return false;
    }
}
