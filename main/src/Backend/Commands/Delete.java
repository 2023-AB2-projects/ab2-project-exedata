package Backend.Commands;

import Backend.Databases.Databases;
import Backend.Databases.IndexFile;
import Backend.Parser;
import Backend.SaveLoadJSON.LoadJSON;
import Backend.SocketServer.ErrorClient;
import Backend.MongoDBManagement.MongoDB;
import org.bson.Document;

import javax.print.Doc;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static Backend.Commands.FormatCommand.getPrimaryKeysValuesSeparateByHash;
import static Backend.Commands.ValidateInsertDeleteData.checkDeleteData;

public class Delete implements Command {
    private final String command;
    List<String> primaryKeys;

    public Delete(String command) {
        this.command = command;
    }

    @Override
    public void performAction() throws ParserConfigurationException, TransformerException {
        Pattern pattern = Pattern.compile("^\\s*DELETE\\s+FROM\\s+([A-Za-z0-9]+)\\s+WHERE\\s+([^ ]*)\\s*=\\s*([^ ;]*)\\s*;?", Pattern.CASE_INSENSITIVE);
        Pattern patternAll = Pattern.compile("^\\s*DELETE\\s+FROM\\s+([A-Za-z0-9]+)\\s*;?", Pattern.CASE_INSENSITIVE);
        Pattern patternMultiplePK = Pattern.compile("^\\s*DELETE\\s+FROM\\s+([A-Za-z0-9]+)\\s+WHERE\\s+([^ ]*\\s*=\\s*[^ ;]*\\s+AND\\s+.*)\\s*;?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(command);
        Matcher matcherAll = patternAll.matcher(command);
        Matcher matcherMultiplePK = patternMultiplePK.matcher(command);
        Databases databases = LoadJSON.load("databases.json");
        if (databases == null || databases.getDatabaseList().size() == 0) {
            System.out.println("Database doesn't exists!");
            ErrorClient.send("Database doesn't exists!");
            return;
        }

        if (Parser.currentDatabaseName == null) {
            System.out.println("Please select your database first!");
            ErrorClient.send("Please select your database first!");
        } else {
            MongoDB mongoDB = new MongoDB();
            if (matcher.matches()) {
                String tableName = matcher.group(1);
                String fieldName = matcher.group(2);
                String value = matcher.group(3);

                if (value.charAt(0) == '"' || value.charAt(0) == '\'') {
                    value = value.substring(1, value.length() - 1);
                }

                primaryKeys = getPrimaryKeys(Parser.currentDatabaseName, tableName, databases);
                mongoDB.createDatabaseOrUse(Parser.currentDatabaseName);
                if (isPrimaryKey(fieldName)) {
                    if (primaryKeys.size() != 1) {
                        System.out.println("Error with deletion! Please specify all the primary keys!");
                        ErrorClient.send("Error with deletion! Please specify all the primary keys!");
                    } else {
                        if (checkDeleteData(tableName, value, databases, mongoDB)) {
                            mongoDB.deleteOne(tableName, "_id", value);
//                            updateIndexFiles
                        } else {
                            System.out.println("Error with foreign key constraint!");
                            ErrorClient.send("Error with foreign key constraint!");
                        }
                    }
                } else {
                    System.out.println("Document can only be deleted according to the PRIMARY KEY!");
                    ErrorClient.send("Document can only be deleted according to the PRIMARY KEY!");
                }

            } else if (matcherAll.matches()) {
                String tableName = matcherAll.group(1);

                mongoDB.createDatabaseOrUse(Parser.currentDatabaseName);
//                mongoDB.deleteAll(tableName);
                for (Document i : mongoDB.getDocuments(tableName).find()) {
                    if (checkDeleteData(tableName, i.getString("_id"), databases, mongoDB)) {
                        mongoDB.deleteOne(tableName, "_id", i.getString("_id"));
//                            updateIndexFiles
                    }
                }

            } else if (matcherMultiplePK.matches()) {
                String tableName = matcherMultiplePK.group(1);
                String keyString = matcherMultiplePK.group(2);
                keyString = keyString.replace(" ", "");
                keyString = keyString.replaceAll("(?i)and", "AND");
                String[] keyValuePairs = keyString.split("AND");
                primaryKeys = getPrimaryKeys(Parser.currentDatabaseName, tableName, databases);
                String deleteValue = buildKey(primaryKeys, keyValuePairs);
                if (deleteValue.equals("!!!!!")) {
                    System.out.println("Error with deletion! Please specify all the primary keys!");
                    ErrorClient.send("Error with deletion! Please specify all the primary keys!");
                } else {
                    mongoDB.createDatabaseOrUse(Parser.currentDatabaseName);
                    if (checkDeleteData(tableName, deleteValue, databases, mongoDB)) {
                        mongoDB.deleteOne(tableName, "_id", deleteValue);
                        for (IndexFile i : databases.getDatabase(Parser.currentDatabaseName).getTable(tableName).getIndexFiles()) {
                            deleteUpdateIndexFile(keyValuePairs, i, databases, mongoDB, tableName);
                        }
                    } else {
                        System.out.println("Error with foreign key constraint!");
                        ErrorClient.send("Error with foreign key constraint!");
                    }
                }
            }
            mongoDB.disconnectFromLocalhost();
        }

    }

    public void deleteUpdateIndexFile(String[] keyValuePairs, IndexFile indexFile, Databases databases, MongoDB mongoDB, String tableName) {
        List<String> fieldName = new ArrayList<>();
        List<String> value = new ArrayList<>();
        for (String i : keyValuePairs) {
            fieldName.add(i.split("=")[0]);
            System.out.println(fieldName.get(fieldName.size()-1));
            value.add(getValue(i.split("=")[1]));
            System.out.println(value.get(value.size()-1));
        }
        StringBuilder keyIndexFile = new StringBuilder();
        List<String> indexAttributes = indexFile.getIndexAttributes();
        List<String> primaryKeys = databases.getDatabase(Parser.currentDatabaseName).getTable(tableName).getPrimaryKey();

        for (int i = 0; i < fieldName.size(); i++) {
            if (indexAttributes.contains(fieldName.get(i))) {
                keyIndexFile.append(value.get(i)).append("#");
            }
        }
        keyIndexFile = new StringBuilder(keyIndexFile.substring(0, keyIndexFile.length() - 1));

        if (indexFile.getIsUnique().equals("1")) {
            //if unique
            mongoDB.createDatabaseOrUse(Parser.currentDatabaseName);
            mongoDB.deleteOne(indexFile.getIndexName(), "_id", keyIndexFile.toString());
        } else {
            Document document = mongoDB.getDocument(indexFile.getIndexName(),keyIndexFile.toString());
            List<String> valueIndexFile = new ArrayList<>(List.of(document.getString("Value").split("#")));
            valueIndexFile.remove(getPrimaryKeysValuesSeparateByHash(primaryKeys,fieldName,value));
            mongoDB.deleteOne(indexFile.getIndexName(), "_id", keyIndexFile.toString());
            StringBuilder valueInsert= new StringBuilder();
            for(String i:valueIndexFile){
                valueInsert.append(i).append("#");
            }
//            valueInsert.append().append("#");
            valueInsert = new StringBuilder(valueInsert.substring(0, valueInsert.length() - 1));
            Document document1 =new Document();
            document1.append("_id",keyIndexFile.toString());
            document1.append("Value",valueInsert.toString());
            mongoDB.insertOne(indexFile.getIndexName(), document1);
            //mongoDB.updateDocument(keyIndexFile.substring(0, keyIndexFile.length() - 1), indexFile.getIndexName(), valueIndexFile);
        }
    }

    private String getValue(String value) {
        if(value.charAt(value.length()-1)==';')
            value=value.substring(0,value.length()-1);
        if (value.charAt(value.length() - 1) == '\'' || value.charAt(value.length() - 1) == '\"') {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    public List<String> getPrimaryKeys(String dataBaseName, String tableName, Databases databases) {
        assert databases != null;
        return databases.getDatabase(dataBaseName).getTable(tableName).getPrimaryKey();
    }

    public boolean isPrimaryKey(String fieldName) {
        for (String primaryKey : primaryKeys) {
            if (primaryKey.equals(fieldName)) {
                return true;
            }
        }
        return false;
    }

    public String buildKey(List<String> primaryKeys, String[] keyValuePair) {
        String deleteValue = "";
        String[] key = new String[keyValuePair.length];
        String[] value = new String[keyValuePair.length];
        String[] oneKeyOneValue;
        for (int i = 0; i < keyValuePair.length; i++) {
            oneKeyOneValue = keyValuePair[i].split("=");
            ;
            key[i] = oneKeyOneValue[0];
            value[i] = oneKeyOneValue[1];
            if (value[i].charAt(0) == '\"' || value[i].charAt(0) == '\'') {
                value[i] = value[i].substring(1, value[i].length() - 1);
            }
        }
        int nr = 0;
        for (int i = 0; i < primaryKeys.size(); i++) {
            for (int j = 0; j < key.length; j++) {
                if (primaryKeys.get(i).equals(key[j])) {
                    deleteValue = deleteValue.concat(value[j] + "#");
                    nr++;
                }
            }
        }
        deleteValue = deleteValue.substring(0, deleteValue.length() - 1);
        if (nr != primaryKeys.size()) {
            deleteValue = "!!!!!";
        }
        return deleteValue;
    }
}
