package Backend.Commands;

import java.io.FileWriter;
import java.io.IOException;

import Backend.SaveLoadJSON.LoadJSON;
import Backend.SaveLoadJSON.SaveJSON;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import static Backend.Parser.currentDatabaseName;

public class CreateIndex implements Command {

    private final String command;
    private JSONArray indexFiles;
    private JSONArray tableStructure;

    public CreateIndex(String command) {
        this.command = command;
    }

    @Override
    public void performAction() {
        JSONObject databases = LoadJSON.load("databases.json");
        if (databases == null) {
            System.out.println("Databases doesn't exists!");
            return;
        }

        String[] commandWords = command.split(" ");
        String currentTableName = commandWords[4];
        String indexName=commandWords[2];
        setIndexFilesAndTableStructure(databases, currentTableName);
        createIndex(commandWords, indexName);
        SaveJSON.save(databases, "databases.json");
    }

    private void createIndex(String[] commandWords, String IndexName) {
        //CREATE INDEX index_name
        //ON table_name (column1, column2, ...);
        String indexFileName = IndexName + ".ind";
        String column;
        JSONObject indexName = new JSONObject();
        indexName.put("indexName", indexFileName);
        createEmptyIndexFile(indexFileName);
        JSONArray indexAttributes = new JSONArray();

        for (int i = 5; i < commandWords.length; i++) {
            column = withoutCommaAndBrackets(commandWords[i]);
            if (existsInStructure(column)) {
                JSONObject reserve = new JSONObject();
                reserve.put("IAttribute", column);
                indexAttributes.add(reserve);
            }
        }
        JSONArray indexFile = new JSONArray();
        indexFile.add(indexName);
        indexFile.add(indexAttributes);
        indexFiles.add(indexFile);
    }

    private boolean existsInStructure(String word) {
        JSONObject jsonObject;
        for (Object i : tableStructure) {
            jsonObject = (JSONObject) i;
            if (jsonObject.get("attributeName").equals(word))
                return true;
        }
        return false;
    }

    private String withoutCommaAndBrackets(String word) {
        String result = word;
        if (result.charAt(0) == '(') {
            result = result.substring(1);
        }
        if (result.charAt(result.length() - 1) == ';') {
            result = result.substring(0, result.length() - 1);
        }
        if (result.charAt(result.length() - 1) == ')') {
            result = result.substring(0, result.length() - 1);
        }
        if (result.charAt(result.length() - 1) == ',') {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    private void createEmptyIndexFile(String indexFileName) {
        try (FileWriter fileWriter = new FileWriter(indexFileName)) {
            fileWriter.write("");
            fileWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void setIndexFilesAndTableStructure(JSONObject databases, String currentTableName) {
        JSONArray jsonArray = (JSONArray) databases.get("Databases");
        boolean exists = false;
        for (Object object : jsonArray) {
            JSONObject jsonObjectSearch = (JSONObject) object;
            String name = (String) jsonObjectSearch.get("databaseName");
            if (name != null && name.equals(currentDatabaseName)) {
                JSONArray tables = (JSONArray) jsonObjectSearch.get("Tables");
                for (Object i : tables) {
                    JSONObject table = (JSONObject) i;
                    String tableName = (String) table.get("tableName");
                    if (tableName != null && tableName.equals(currentTableName)) {
                        indexFiles = (JSONArray) table.get("IndexFiles");
                        tableStructure = (JSONArray) table.get("Structure");
                        exists = true;
                    }
                }
            }
        }
        if (!exists) {
            System.out.println("Tables or Database doesn't exist!");
        }
    }
}
