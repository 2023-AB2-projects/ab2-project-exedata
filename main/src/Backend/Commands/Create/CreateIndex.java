package Backend.Commands.Create;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Backend.Backend;
import Backend.Commands.Command;
import Backend.CommonClass.Common;
import Backend.Databases.*;
import Backend.Parser;
import Backend.SaveLoadJSON.SaveJSON;
import Backend.SocketServer.ErrorClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;

import static Backend.SocketServer.Server.databases;
import static Backend.SocketServer.Server.mongoDB;

public class CreateIndex implements Command {

    private final String command;

    public CreateIndex(String command) {
        this.command = command;
    }

    @Override
    public void performAction() {
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

            if (databases.getDatabase(Parser.currentDatabaseName).getTable(tableName) == null) {
                ErrorClient.send("Table doesn't exists!");
                return;
            }
            if (databases.getDatabase(Parser.currentDatabaseName).getTable(indexName) != null) {
                ErrorClient.send("Index file already exists!");
                return;
            }
            if (!databases.getDatabase(Parser.currentDatabaseName).getTable(tableName).existIndexName(indexName)) {
                if (databases.getDatabase(Parser.currentDatabaseName) != null) {
                    if (databases.getDatabase(Parser.currentDatabaseName).checkTableExists(tableName)) {
                        if (createIndex(indexName, tableName, attributeNames)) {
                            SaveJSON.save(databases, "databases.json");
                            createIndexFileInMongoDB(indexName, tableName, attributeNames, databases);
                            ErrorClient.send(indexName + " index is created!");
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
        } else {
            ErrorClient.send("Wrong command!");
        }
    }

    private boolean createIndex(String IndexName, String tableName, String[] attributeNames) {
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

    public static void createIndexFileInMongoDB(String indexName, String tableName, String[] attributeNames, Databases databases) {
        // Create index file (collection) in MongoDB
        // Connection
        mongoDB.createDatabaseOrUse(Parser.currentDatabaseName);
        mongoDB.createCollection(indexName);
        List<Attribute> attributeList = databases.getDatabase(Parser.currentDatabaseName).getTable(tableName).getStructure();
        List<String> primaryKeyList = databases.getDatabase(Parser.currentDatabaseName).getTable(tableName).getPrimaryKey();

        // Get all documents from a table
        MongoCollection<Document> collection = mongoDB.getDocuments(tableName);

        // check that index attributes will be unique or not
        if (isUnique(tableName, attributeNames, databases)) {
            // add documents to indexFile (unique)
            try (MongoCursor<Document> cursor = collection.find().iterator()) {
                while (cursor.hasNext()) {
                    Document document = cursor.next();
                    StringBuilder keyIndexFile = new StringBuilder();

                    // build string (new key)
                    for (String attributeName : attributeNames) {
                        String value = Common.getValueByAttributeName(document, attributeName, primaryKeyList, attributeList);
                        keyIndexFile.append(value).append("#");
                    }

                    keyIndexFile = new StringBuilder(keyIndexFile.substring(0, keyIndexFile.length() - 1));
                    String valueIndexFile = (String) document.get("_id");

                    Document documentNew = new Document();
                    documentNew.append("_id", keyIndexFile.toString());
                    documentNew.append("Value", valueIndexFile);
                    mongoDB.insertOne(indexName, documentNew);
                    Backend.goodInsert = false;
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
                    for (String attributeName : attributeNames) {
                        String value = Common.getValueByAttributeName(document, attributeName, primaryKeyList, attributeList);
                        keyIndexFile.append(value).append("#");
                    }

                    keyIndexFile = new StringBuilder(keyIndexFile.substring(0, keyIndexFile.length() - 1));
                    String valueIndexFile = (String) document.get("_id");

                    if (!map.containsKey(keyIndexFile.toString())) {
                        map.put(keyIndexFile.toString(), valueIndexFile);
                    } else {
                        map.put(keyIndexFile.toString(), map.get(keyIndexFile.toString()) + "&" + valueIndexFile);
                    }
                }
            }

            for (String key : map.keySet()) {
                String value = map.get(key);
                Document documentNew = new Document();
                documentNew.append("_id", key);
                documentNew.append("Value", value);
                mongoDB.insertOne(indexName, documentNew);
                Backend.goodInsert = false;
            }
        }
    }

    private static boolean isUnique(String tableName, String[] attributeNames, Databases databases) {
        Table table = databases.getDatabase(Parser.currentDatabaseName).getTable(tableName);
        int count = 0;
        for (String attribute : attributeNames) {
            if (table.isUnique(attribute)) {
                return true;
            } else if (table.isPrimaryKey(attribute)) {
                count++;
            }
        }
        return table.getPrimaryKey().size() == count;
    }
}
