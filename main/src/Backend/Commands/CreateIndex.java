package Backend.Commands;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Backend.Databases.Attribute;
import Backend.Databases.Databases;
import Backend.Databases.IndexFile;
import Backend.MongoDBManagement.MongoDB;
import Backend.Parser;
import Backend.SaveLoadJSON.LoadJSON;
import Backend.SaveLoadJSON.SaveJSON;
import Backend.SocketServer.ErrorClient;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;

import static Backend.Commands.FormatCommand.formatWords;

public class CreateIndex implements Command {

    private final String command;
    private Databases databases;

    private List<String> primaryKeyList;
    private List<Attribute> attributeList;

    public CreateIndex(String command) {
        this.command = command;
    }

    @Override
    public void performAction() {
        //CREATE INDEX index_name
        //ON table_name (column1, column2, ...);
        Parser.currentDatabaseName = "University";
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
                            createEmptyIndexFile(indexName + ".ind");
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
                System.out.println("IndexName already exists!");
                // ErrorClient.send("IndexName already exists!");
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
        // create index file (collection) in mongodb
        MongoDB mongoDB = new MongoDB();
        mongoDB.createDatabaseOrUse(Parser.currentDatabaseName);
        mongoDB.createCollection(indexName);
        attributeList = databases.getDatabase(Parser.currentDatabaseName).getTable(tableName).getStructure();
        primaryKeyList = databases.getDatabase(Parser.currentDatabaseName).getTable(tableName).getPrimaryKey();

        int[] attributeNamesInIndex = new int[attributeList.size()];

        MongoCollection<Document> collection = mongoDB.getDocuments(tableName);

        // get primary key and not primary key coordinates from given attributes
        int[] primaryKeyCoordinate = new int[attributeList.size()];
        int[] notPrimaryKeyCoordinate = new int[attributeList.size()];

        for (int i=0; i<primaryKeyCoordinate.length; i++) {
            primaryKeyCoordinate[i] = -1;
            notPrimaryKeyCoordinate[i] = -1;
        }

        for (int i=0; i<attributeNames.length; i++) {
            int coordinate = getPrimaryKeyCoordinate(attributeNames[i]);
            if (coordinate>=0) {
                primaryKeyCoordinate[i] = coordinate;
            } else {
                coordinate = getAttributeCoordinate(attributeNames[i]);
                notPrimaryKeyCoordinate[i] = coordinate;
            }
        }

        // add documents to indexFile (unique)
        try (MongoCursor<Document> cursor = collection.find().iterator()) {
            while (cursor.hasNext()) {
                Document document = cursor.next();
                String[] primaryKeys = ((String) document.get("_id")).split("#");
                String[] values = ((String) document.get("Value")).split("#");
                StringBuilder keyIndexFile = new StringBuilder();

                // build string
                for (int j : primaryKeyCoordinate) {
                    if (j != -1) {
                        keyIndexFile.append(primaryKeys[j]).append("#");
                    }
                }
                for (int j : notPrimaryKeyCoordinate) {
                    if (j != -1) {
                        keyIndexFile.append(values[j]).append("#");
                    }
                }

                keyIndexFile = new StringBuilder(keyIndexFile.substring(0, keyIndexFile.length() - 1));
                String valueIndexFile = (String) document.get("_id");

                Document documentNew = new Document();
                documentNew.append("_id", keyIndexFile.toString());
                documentNew.append("Value", valueIndexFile);
                mongoDB.insertOne(indexName, documentNew);
            }
        }


    }

    private boolean isPrimaryKey(String fieldName) {
        for (String primaryKey : primaryKeyList) {
            if (primaryKey.equals(fieldName)) {
                return true;
            }
        }
        return false;
    }

    private int getPrimaryKeyCoordinate(String fieldName) {
        for (int i=0; i<primaryKeyList.size(); i++) {
            if (primaryKeyList.get(i).equals(fieldName)) {
                return i;
            }
        }
        return -1;
    }

    private int getAttributeCoordinate(String fieldName) {
        int nr = 0;
        for (int i=0; i<attributeList.size(); i++) {
            if (!isPrimaryKey(attributeList.get(i).getName())) {
                if (attributeList.get(i).getName().equals(fieldName)) {
                    return nr;
                }
                nr++;
            }
        }
        return -1;
    }
}
