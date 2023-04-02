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

public class Delete implements Command {
    private final String command;
    List<String> primaryKeys;

    public Delete(String command) {
        this.command = command;
    }

    @Override
    public void performAction() throws ParserConfigurationException, TransformerException {
        Pattern pattern = Pattern.compile("^\\s*DELETE\\s+FROM\\s+([A-Za-z0-9]+)\\s+WHERE\\s+([^ ]*)\\s*=\\s*([^ ]*)\\s*;", Pattern.CASE_INSENSITIVE);
        Pattern patternAll = Pattern.compile("^\\s*DELETE\\s+FROM\\s+([A-Za-z0-9]+)\\s*;", Pattern.CASE_INSENSITIVE);
        Pattern patternMultiplePK = Pattern.compile("^\\s*DELETE\\s+FROM\\s+([A-Za-z0-9]+)\\s+WHERE\\s+([^ ]*)\\s*=\\s*([^ ]*)\\s* AND .*;", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(command);
        Matcher matcherAll = patternAll.matcher(command);
        Matcher matcherMultiplePK = patternMultiplePK.matcher(command);

        //Parser.currentDatabaseName = "University";
        if (Parser.currentDatabaseName == null) {
            System.out.println("Please select your database first!");
        } else {
            MongoDB mongoDB = new MongoDB();
            if (matcher.matches()) {
                String tableName = matcher.group(1);
                String fieldName = matcher.group(2);
                String value = matcher.group(3);

                if (fieldName.charAt(0) == '"') {
                    fieldName = fieldName.substring(1, fieldName.length()-1);
                }

                primaryKeys = getPrimaryKeys(Parser.currentDatabaseName, tableName);
                mongoDB.createDatabaseOrUse(Parser.currentDatabaseName);
                if (isPrimaryKey(fieldName)) {
                    mongoDB.deleteOne(tableName, "_id", value);
                } else {
                    System.out.println("Document can only be deleted according to the PRIMARY KEY!");
                }

            } else if (matcherAll.matches()) {
                String tableName = matcherAll.group(1);

                mongoDB.createDatabaseOrUse(Parser.currentDatabaseName);
                mongoDB.deleteAll(tableName);
            } else if (matcherMultiplePK.matches()) {
                System.out.println("PK");
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
}
