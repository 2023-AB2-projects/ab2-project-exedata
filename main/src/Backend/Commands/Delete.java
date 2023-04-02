package Backend.Commands;

import Backend.Databases.Databases;
import Backend.Parser;
import Backend.SaveLoadJSON.LoadJSON;
import MongoDBManagement.MongoDB;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static Backend.SocketServer.Server.errorClient;

public class Delete implements Command {
    private final String command;
    List<String> primaryKeys;

    public Delete(String command) {
        this.command = command;
    }

    @Override
    public void performAction() throws ParserConfigurationException, TransformerException {
        Pattern pattern = Pattern.compile("^\\s*DELETE\\s+FROM\\s+([A-Za-z0-9]+)\\s+WHERE\\s+([^ ]*)\\s*=\\s*([^ ]*)\\s*;?", Pattern.CASE_INSENSITIVE);
        Pattern patternAll = Pattern.compile("^\\s*DELETE\\s+FROM\\s+([A-Za-z0-9]+)\\s*;?", Pattern.CASE_INSENSITIVE);
        Pattern patternMultiplePK = Pattern.compile("^\\s*DELETE\\s+FROM\\s+([A-Za-z0-9]+)\\s+WHERE\\s+([^ ]*\\s*=\\s*[^ ]*\\s+AND\\s+.*)\\s*;?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(command);
        Matcher matcherAll = patternAll.matcher(command);
        Matcher matcherMultiplePK = patternMultiplePK.matcher(command);

        //Parser.currentDatabaseName = "University";
        if (Parser.currentDatabaseName == null) {
            System.out.println("Please select your database first!");
            errorClient.send("Please select your database first!");
        } else {
            MongoDB mongoDB = new MongoDB();
            if (matcher.matches()) {
                String tableName = matcher.group(1);
                String fieldName = matcher.group(2);
                String value = matcher.group(3);

                if (fieldName.charAt(0) == '"' || fieldName.charAt(0) == '\'') {
                    fieldName = fieldName.substring(1, fieldName.length() - 1);
                }

                primaryKeys = getPrimaryKeys(Parser.currentDatabaseName, tableName);
                mongoDB.createDatabaseOrUse(Parser.currentDatabaseName);
                if (isPrimaryKey(fieldName)) {
                    if (primaryKeys.size() != 1) {
                        System.out.println("Error with deletion! Please specify all the primary keys!");
                        errorClient.send("Error with deletion! Please specify all the primary keys!");
                    } else {
                        mongoDB.deleteOne(tableName, "_id", value);
                    }
                } else {
                    System.out.println("Document can only be deleted according to the PRIMARY KEY!");
                    errorClient.send("Document can only be deleted according to the PRIMARY KEY!");
                }

            } else if (matcherAll.matches()) {
                String tableName = matcherAll.group(1);

                mongoDB.createDatabaseOrUse(Parser.currentDatabaseName);
                mongoDB.deleteAll(tableName);
            } else if (matcherMultiplePK.matches()) {
                String tableName = matcherMultiplePK.group(1);
                String keyString = matcherMultiplePK.group(2);
                keyString = keyString.replace(" ", "");
                String[] keyValuePairs = keyString.split("AND");
                keyValuePairs = keyString.split("and");

                primaryKeys = getPrimaryKeys(Parser.currentDatabaseName, tableName);
                String deleteValue = buildKey(primaryKeys, keyValuePairs);
                if (deleteValue.equals("!!!!!")) {
                    System.out.println("Error with deletion! Please specify all the primary keys!");
                    errorClient.send("Error with deletion! Please specify all the primary keys!");
                } else {
                    mongoDB.createDatabaseOrUse(Parser.currentDatabaseName);
                    mongoDB.deleteOne(tableName, "_id", deleteValue);
                }
            }
            mongoDB.disconnectFromLocalhost();
        }

    }


    public List<String> getPrimaryKeys(String dataBaseName, String tableName) {
        Databases databases = LoadJSON.load("databases.json");
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
            oneKeyOneValue = keyValuePair[i].split("=");;
            key[i] = oneKeyOneValue[0];
            value[i] = oneKeyOneValue[1];
            if (value[i].charAt(0) == '\"' || value[i].charAt(0) == '\'') {
                value[i] = value[i].substring(1, value[i].length()-1);
            }
            System.out.println(key[i] + " " + value[i]);
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
