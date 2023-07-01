package Backend.Commands.InsertDelete;

import Backend.Databases.Attribute;
import Backend.Databases.Databases;
import Backend.Databases.ForeignKey;
import Backend.Databases.Table;
import Backend.Parser;
import Backend.SocketServer.ErrorClient;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static Backend.CommonClass.Common.getValueByAttributeName;
import static Backend.SocketServer.Server.databases;
import static Backend.SocketServer.Server.mongoDB;

public class ValidateInsertDeleteData {

    public static boolean checkDeleteData(String tableName, String id, Databases databases) {
        Document document = mongoDB.getDocument(tableName, id);
        List<String> primaryKeyList = databases.getDatabase(Parser.currentDatabaseName).getTable(tableName).getPrimaryKey();
        List<Attribute> attributeList = databases.getDatabase(Parser.currentDatabaseName).getTable(tableName).getStructure();
        //delete from disciplines where DiscID = 'DB1';
        String indexFileName;
        String value;
        for (Table i : databases.getDatabase(Parser.currentDatabaseName).getTables()) {
            for (ForeignKey j : i.getForeignKeys()) {
                if (j.getRefTable().equals(tableName)) {
                    indexFileName = i.getIndexFileName(new String[]{j.getName()});
                    value = getValueByAttributeName(document, j.getRefAttribute(), primaryKeyList, attributeList);
                    if (indexFileName == null) {
                        if (checkExistsValueInTableIfDoesNotHaveIndexFile(i.getName(), j.getName(), value, databases))
                            return false;
                    } else {
                        if (checkExistsValueInTableIfDoesHaveIndexFile(indexFileName, value)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public static boolean checkInsertData(String tableName, String[] column, String[] values) {
        if (databases == null) {
            ErrorClient.send("JSON file doesn't exists!");
            return false;
        }
        if (!databases.checkDatabaseExists(Parser.currentDatabaseName)) {
            ErrorClient.send("Database doesn't exists this database!");
            return false;
        }
        if (!databases.getDatabase(Parser.currentDatabaseName).checkTableExists(tableName)) {
            ErrorClient.send("Table doesn't exists!");
            return false;
        }
        if (!databases.getDatabase(Parser.currentDatabaseName).getTable(tableName).checkInsertColumn(column)) {
            ErrorClient.send("Syntax error!");
            return false;
        }
        for (int i = 0; i < values.length; i++) {
            if (!checkType(values[i], databases.getDatabase(Parser.currentDatabaseName).getTable(tableName).getAttribute(column[i]).getType())) {
                ErrorClient.send("The " + values[i] + " type isn't correct!");
                return false;
            }
            if (!checkUniqueConstraint(values[i], column[i], tableName, databases)) {
                ErrorClient.send("The " + column[i] + ": " + values[i] + " already exists!");
                return false;
            }
            if (!checkForeignKeyConstraint(values[i], column[i], tableName, databases)) {
                ErrorClient.send("The " + column[i] + ": " + values[i] + " doesn't exists in reference table!");
                return false;
            }
        }
        return true;
    }

    private static boolean checkForeignKeyConstraint(String value, String attributeName, String tableName, Databases databases) {
        ForeignKey foreignKey = null;
        for (ForeignKey i : databases.getDatabase(Parser.currentDatabaseName).getTable(tableName).getForeignKeys()) {
            if (i.getName().equals(attributeName)) {
                foreignKey = i;
                break;
            }
        }
        if (foreignKey == null) {
            return true;
        }
        String indexFileName = databases.getDatabase(Parser.currentDatabaseName).getTable(foreignKey.getRefTable()).getIndexFileName(new String[]{foreignKey.getRefAttribute()});
        if (indexFileName == null) {
            return checkExistsValueInTableIfDoesNotHaveIndexFile(foreignKey.getRefTable(), foreignKey.getRefAttribute(), value, databases);
        } else {
            return checkExistsValueInTableIfDoesHaveIndexFile(indexFileName, value);
        }
    }

    private static boolean checkUniqueConstraint(String value, String attributeName, String tableName, Databases databases) {
        if (!databases.getDatabase(Parser.currentDatabaseName).getTable(tableName).isUnique(attributeName)) {
            return true;
        }
        String indexFileName = databases.getDatabase(Parser.currentDatabaseName).getTable(tableName).getIndexFileName(new String[]{attributeName});
        if (indexFileName == null) {
            //doesn't exists indexFile
            return !checkExistsValueInTableIfDoesNotHaveIndexFile(tableName, attributeName, value, databases);
        } else {
            return !checkExistsValueInTableIfDoesHaveIndexFile(indexFileName, value);
        }
    }

    public static boolean checkExistsValueInTableIfDoesHaveIndexFile(String indexFileName, String value) {
        mongoDB.createDatabaseOrUse(Parser.currentDatabaseName);
        Document document = new Document("_id", value);
        return mongoDB.existsID(indexFileName, document);
    }

    public static boolean checkExistsValueInTableIfDoesNotHaveIndexFile(String tableName, String attributeName, String value, Databases databases) {
        mongoDB.createDatabaseOrUse(Parser.currentDatabaseName);
        MongoCollection<Document> documents = mongoDB.getDocuments(tableName);
        for (Document i : documents.find()) {
            if (Objects.equals(getValueByAttributeName(i, attributeName,
                    databases.getDatabase(Parser.currentDatabaseName).getTable(tableName).getPrimaryKey(),
                    databases.getDatabase(Parser.currentDatabaseName).getTable(tableName).getStructure()), value)) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkType(String value, String recommendedType) {
        switch (recommendedType) {
            case "INT":
                Pattern pattern1 = Pattern.compile("[0-9]+");
                Matcher matcher1 = pattern1.matcher(value);
                return matcher1.matches();
            case "FLOAT":
                Pattern pattern2 = Pattern.compile("[0-9]+\\.?[0-9]*");
                Matcher matcher2 = pattern2.matcher(value);
                return matcher2.matches();
            case "BIT":
                Pattern pattern3 = Pattern.compile("[0-1]");
                Matcher matcher3 = pattern3.matcher(value);
                return matcher3.matches();
            case "DATE":
                //YYYY-MM-DD
                Pattern pattern4 = Pattern.compile("[0-9]{4}-[0-1][0-9]-[0-3][0-9]");
                Matcher matcher4 = pattern4.matcher(value);
                return matcher4.matches();
            case "DATETIME":
                //YYYY-MM-DD hh:mm:ss
                Pattern pattern5 = Pattern.compile("[0-9]{4}-[0-1][0-9]-[0-3][0-9] [0-2][0-9]:[0-6][0-9]:[0-6][0-9]");
                Matcher matcher5 = pattern5.matcher(value);
                return matcher5.matches();
            case "VARCHAR":
                return true;
            default:
                return false;
        }
    }
}
