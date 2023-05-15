package Backend.Commands;

import Backend.Databases.Attribute;
import Backend.Databases.Databases;
import Backend.Databases.IndexFile;
import Backend.Parser;
import Backend.SaveLoadJSON.LoadJSON;
import Backend.SocketServer.ErrorClient;
import org.bson.Document;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static Backend.Commands.FormatCommand.getPrimaryKeysValuesSeparateByHash;
import static Backend.SocketServer.Server.mongoDB;

public class Insert implements Command {
    private final String command;
    private List<String> primaryKeys;

    public Insert(String command) {
        this.command = command;
    }

    @Override
    public void performAction() throws ParserConfigurationException, TransformerException {
        Pattern pattern = Pattern.compile("^\\s*INSERT\\s+INTO\\s+([A-Za-z0-9]+)\\s+\\((.*)\\)\\s+VALUES\\s+\\((.*)\\);?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(command);

        if (Parser.currentDatabaseName == null) {
            System.out.println("Please select your database first!");
            ErrorClient.send("Please select your database first!");
        } else {
            if (matcher.matches()) {
                String tableName = matcher.group(1);
                String[] fieldName = matcher.group(2).replaceAll("\\s+", "").split(",");
                String[] value = matcher.group(3).replaceAll("\\s+", "").replaceAll("'", "").replaceAll("\"", "").split(",");

                if (ValidateInsertDeleteData.checkInsertData(tableName, fieldName, value)) {
                    Databases databases = LoadJSON.load("databases.json");
                    assert databases != null;
                    primaryKeys = getPrimaryKeys(Parser.currentDatabaseName, tableName, databases);
                    String primaryKeysString = allPrimaryKeyValueDividedByHash(fieldName, value, primaryKeys);
                    List<Attribute> attributeList = getAllAttribute(Parser.currentDatabaseName, tableName, databases);
                    String[] fieldNameFilled = listToStringArray(attributeList);
                    String[] valueFilled = addNullValues(fieldNameFilled, fieldName, value);
                    String insertValueWithHash = allAttributeValueExceptPKDividedByHash(fieldNameFilled, valueFilled);

                    Document document = new Document();
                    document.append("_id", primaryKeysString);
                    document.append("Value", insertValueWithHash);
                    mongoDB.createDatabaseOrUse(Parser.currentDatabaseName);
                    mongoDB.insertOne(tableName, document);

                    List<IndexFile> indexFileList = databases.getDatabase(Parser.currentDatabaseName).getTable(tableName).getIndexFiles();
                    for (IndexFile i : indexFileList) {
                        insertUpdateIndexFile(fieldName, value, i, databases, tableName);
                    }
                }
            }
        }
    }

    public void insertUpdateIndexFile(String[] fieldName, String[] value, IndexFile indexFile, Databases databases, String tableName) {
        StringBuilder keyIndexFile = new StringBuilder();
        List<String> indexAttributes = indexFile.getIndexAttributes();
        List<String> primaryKeys = databases.getDatabase(Parser.currentDatabaseName).getTable(tableName).getPrimaryKey();
        if (indexFile.getIsUnique().equals("1")) {
            //if unique
            for (int i = 0; i < fieldName.length; i++) {
                if (indexAttributes.contains(fieldName[i])) {
                    keyIndexFile.append(value[i]).append("#");
                }
            }
            keyIndexFile = new StringBuilder(keyIndexFile.substring(0, keyIndexFile.length() - 1));
            String valueIndexFile = getPrimaryKeysValuesSeparateByHash(primaryKeys, Arrays.stream(fieldName).toList(), Arrays.stream(value).toList());
            Document document = new Document();
            document.append("_id", keyIndexFile.toString());
            document.append("Value", valueIndexFile);
            mongoDB.createDatabaseOrUse(Parser.currentDatabaseName);
            mongoDB.insertOne(indexFile.getIndexName(), document);
        } else {
            for (int i = 0; i < fieldName.length; i++) {
                if (indexAttributes.contains(fieldName[i])) {
                    keyIndexFile.append(value[i]).append("#");
                }
            }
            String valueIndexFile = "&" + getPrimaryKeysValuesSeparateByHash(primaryKeys, Arrays.stream(fieldName).toList(), Arrays.stream(value).toList());
            mongoDB.updateDocument(keyIndexFile.substring(0, keyIndexFile.length() - 1), indexFile.getIndexName(), valueIndexFile);
        }
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

    public List<Attribute> getAllAttribute(String dataBaseName, String tableName, Databases databases) {
        assert databases != null;
        return databases.getDatabase(dataBaseName).getTable(tableName).getStructure();
    }

    public String allPrimaryKeyValueDividedByHash(String[] fieldName, String[] value, List<String> primaryKeys) {
        String primaryKeysString = "";
        for (int i = 0; i < fieldName.length; i++) {
            for (String primaryKey : primaryKeys) {
                if (value[i].charAt(0) == '\"' || value[i].charAt(0) == '\'') {
                    value[i] = value[i].substring(1, value[i].length() - 1);
                }
                if (Objects.equals(primaryKey, fieldName[i])) {
                    primaryKeysString = primaryKeysString.concat(value[i] + "#");
                }
            }
        }
        primaryKeysString = primaryKeysString.substring(0, primaryKeysString.length() - 1);
        return primaryKeysString;
    }

    public String allAttributeValueExceptPKDividedByHash(String[] fieldName, String[] value) {
        String insertValue = "";
        for (int i = 0; i < fieldName.length; i++) {
            if (value[i].charAt(0) == '\"' || value[i].charAt(0) == '\'') {
                value[i] = value[i].substring(1, value[i].length() - 1);
            }
            if (!isPrimaryKey(fieldName[i])) {
                insertValue = insertValue.concat(value[i] + "#");
            }
        }
        insertValue = insertValue.substring(0, insertValue.length() - 1);
        return insertValue;
    }

    public String[] listToStringArray(List<Attribute> attributeList) {
        String[] fieldName = new String[attributeList.size()];
        for (int i = 0; i < attributeList.size(); i++) {
            fieldName[i] = attributeList.get(i).getName();
        }
        return fieldName;
    }

    private String[] addNullValues(String[] fieldNameFilled, String[] fieldName, String[] value) {
        String[] newValue = new String[fieldNameFilled.length];
        boolean ok;
        for (int i = 0; i < fieldNameFilled.length; i++) {
            ok = false;
            for (int j = 0; j < fieldName.length; j++) {
                if (fieldNameFilled[i].equals(fieldName[j])) {
                    ok = true;
                    newValue[i] = value[j];
                    break;
                }
            }
            if (!ok) {
                newValue[i] = "null";
            }
        }
        return newValue;
    }
}
