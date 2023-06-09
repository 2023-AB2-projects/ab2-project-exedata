package Backend.Commands.InsertDelete;

import Backend.Backend;
import Backend.Commands.Command;
import Backend.Databases.Databases;
import Backend.Databases.IndexFile;
import Backend.Parser;
import Backend.SocketServer.ErrorClient;
import org.bson.Document;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static Backend.Commands.Create.CreateIndex.createIndexFileInMongoDB;
import static Backend.Commands.FormatCommand.getPrimaryKeysValuesSeparateByHash;
import static Backend.Commands.InsertDelete.ValidateInsertDeleteData.checkDeleteData;
import static Backend.SocketServer.Server.databases;
import static Backend.SocketServer.Server.mongoDB;

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
        if (databases == null || databases.getDatabaseList().size() == 0) {
            ErrorClient.send("Database doesn't exists!");
            return;
        }

        if (Parser.currentDatabaseName == null) {
            ErrorClient.send("Please select your database first!");
        } else {
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
                        ErrorClient.send("Error with deletion! Please specify all the primary keys!");
                    } else {
                        if (checkDeleteData(tableName, value, databases)) {
                            mongoDB.deleteOne(tableName, "_id", value);
                            for (IndexFile i : databases.getDatabase(Parser.currentDatabaseName).getTable(tableName).getIndexFiles()) {
                                String[] v = new String[1];
                                v[0] = fieldName + '=' + value;
                                deleteUpdateIndexFile(v, i, databases, tableName);
                            }
                        } else {
                            ErrorClient.send("Error with foreign key constraint!");
                        }
                    }
                } else {
                    ErrorClient.send("Document can only be deleted according to the PRIMARY KEY!");
                }
            } else if (matcherAll.matches()) {
                String tableName = matcherAll.group(1);
                mongoDB.createDatabaseOrUse(Parser.currentDatabaseName);
                for (Document i : mongoDB.getDocuments(tableName).find()) {
                    if (checkDeleteData(tableName, i.getString("_id"), databases)) {
                        mongoDB.deleteOne(tableName, "_id", i.getString("_id"));
                    }
                }
                for (IndexFile i : databases.getDatabase(Parser.currentDatabaseName).getTable(tableName).getIndexFiles()) {
                    mongoDB.dropCollection(i.getIndexName());
                    createIndexFileInMongoDB(i.getIndexName(), tableName, i.getIndexAttributes().toArray(new String[0]), databases);
                }
            } else if (matcherMultiplePK.matches()) {
                String tableName = matcherMultiplePK.group(1);
                String keyString = matcherMultiplePK.group(2);
                keyString = keyString.replaceAll("(?i)and ", "AND ");
                keyString = keyString.replace(" ", "");
                String[] keyValuePairs = keyString.split("AND");
                primaryKeys = getPrimaryKeys(Parser.currentDatabaseName, tableName, databases);
                String deleteValue = buildKey(primaryKeys, keyValuePairs);
                if (deleteValue.equals("!!!!!")) {
                    ErrorClient.send("Error with deletion! Please specify all the primary keys!");
                } else {
                    mongoDB.createDatabaseOrUse(Parser.currentDatabaseName);
                    if (checkDeleteData(tableName, deleteValue, databases)) {
                        mongoDB.deleteOne(tableName, "_id", deleteValue);
                        for (IndexFile i : databases.getDatabase(Parser.currentDatabaseName).getTable(tableName).getIndexFiles()) {
                            deleteUpdateIndexFile(keyValuePairs, i, databases, tableName);
                        }
                    } else {
                        ErrorClient.send("Error with foreign key constraint!");
                    }
                }
            }
        }

    }

    public void deleteUpdateIndexFile(String[] keyValuePairs, IndexFile indexFile, Databases databases, String tableName) {
        List<String> fieldName = new ArrayList<>();
        List<String> value = new ArrayList<>();
        for (String i : keyValuePairs) {
            fieldName.add(i.split("=")[0]);
            value.add(getValue(i.split("=")[1]));
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
            Document document = mongoDB.getDocument(indexFile.getIndexName(), keyIndexFile.toString());
            List<String> valueIndexFile = new ArrayList<>(List.of(document.getString("Value").split("&")));
            valueIndexFile.remove(getPrimaryKeysValuesSeparateByHash(primaryKeys, fieldName, value));
            mongoDB.deleteOne(indexFile.getIndexName(), "_id", keyIndexFile.toString());
            StringBuilder valueInsert = new StringBuilder();
            for (String i : valueIndexFile) {
                valueInsert.append(i).append("&");
            }
            if (!valueInsert.toString().equals("")) {
                valueInsert = new StringBuilder(valueInsert.substring(0, valueInsert.length() - 1));
                Document document1 = new Document();
                document1.append("_id", keyIndexFile.toString());
                document1.append("Value", valueInsert.toString());
                mongoDB.insertOne(indexFile.getIndexName(), document1);
                Backend.goodInsert = false;
            }
        }
    }

    private String getValue(String value) {
        if (value.charAt(value.length() - 1) == ';')
            value = value.substring(0, value.length() - 1);
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
            key[i] = oneKeyOneValue[0];
            value[i] = oneKeyOneValue[1];
            if (value[i].charAt(0) == '\"' || value[i].charAt(0) == '\'') {
                value[i] = value[i].substring(1, value[i].length() - 1);
            }
        }
        int nr = 0;
        for (String primaryKey : primaryKeys) {
            for (int j = 0; j < key.length; j++) {
                if (primaryKey.equals(key[j])) {
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
