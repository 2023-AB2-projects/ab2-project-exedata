package Backend.Commands;

import Backend.Databases.Databases;
import Backend.Parser;
import Backend.SaveLoadJSON.LoadJSON;
import Backend.SaveLoadJSON.SaveJSON;
import MongoDBManagement.MongoDB;
import org.bson.Document;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Insert implements Command {
    private final String command;
    private MongoDB mongoDB;
    private List<String> primaryKeys;

    public Insert(String command) {
        this.command = command;
    }

    @Override
    public void performAction() throws ParserConfigurationException, TransformerException {
        mongoDB = new MongoDB();

        Pattern pattern = Pattern.compile("^\\s*INSERT\\s+INTO\\s+([A-Za-z0-9]+)\\s+\\((.*)\\)\\s+VALUES\\s+\\((.*)\\);?");
        Matcher matcher = pattern.matcher(command);

        if (matcher.matches()) {
            String tableName = matcher.group(1);
            String[] fieldName = matcher.group(2).replaceAll("\\s+", "").split(",");
            String[] value = matcher.group(3).replaceAll("\\s+", "").split(",");

            // if(insertValidation(tableName,fieldName,value)) {

            Parser.currentDatabaseName = "University";
            if (Parser.currentDatabaseName == null) {
                System.out.println("Please select your database first!");
            } else {
                primaryKeys = getPrimaryKeys(Parser.currentDatabaseName, tableName);
                String primaryKeysString = "";
                for (String s : fieldName) {
                    for (int i=0; i<primaryKeys.size(); i++) {
                        if (Objects.equals(primaryKeys.get(i), s)) {
                            primaryKeysString = primaryKeysString.concat(value[i] + "#");
                        }
                    }
                }
                primaryKeysString = primaryKeysString.substring(0, primaryKeysString.length()-1);

                String insertValue = "";
                for(int i=0; i<fieldName.length; i++) {
                    if (value[i].charAt(0) == '\"') {
                        value[i] = value[i].substring(1, value[i].length()-1);
                    }
                    if (!isPrimaryKey(fieldName[i])) {
                        insertValue = insertValue.concat(value[i] + "#");
                    }
                }
                insertValue = insertValue.substring(0, insertValue.length()-1);

                Document document = new Document();
                document.append("_id", primaryKeysString);
                document.append("Value", insertValue);
                mongoDB.createDatabaseOrUse(Parser.currentDatabaseName);
                mongoDB.insertOne(tableName, document);
            }
            // }
        }
        mongoDB.disconnectFromLocalhost();
    }

    public List<String> getPrimaryKeys(String dataBaseName, String tableName) {
//        Databases databases2 = new Databases();
//        SaveJSON.save(databases2, "databases3.json");
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
