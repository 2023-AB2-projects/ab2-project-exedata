package Backend.Commands;

import Backend.SaveLoadJSON.LoadJSON;
import Backend.SaveLoadJSON.SaveJSON;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import static Backend.Parser.currentDatabaseName;

public class DropIndex implements Command {
    private String command;

    public DropIndex(String command) {
        this.command = command;
    }

    @Override
    public void performAction() {
        //DROP INDEX index_name ON table_name;
        command = command.substring(0, command.length() - 1);
        JSONObject databases = LoadJSON.load("databases.json");
        if (databases == null) {
            System.out.println("Databases doesn't exists!");
            return;
        }
        String[] commandWords = command.split(" ");
        String currentTableName = commandWords[4];
        String indexName = commandWords[2];

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
                        JSONArray indexFiles = (JSONArray) table.get("IndexFiles");
                        for (Object j : indexFiles) {
                            JSONArray indexes = (JSONArray) j;
                            for (Object k : indexes) {
                                if (k.getClass() == jsonObjectSearch.getClass()) {
                                    JSONObject indexx = (JSONObject) k;
                                    String indexname2 = (String) indexx.get("indexName");
                                    if (indexname2 != null && indexname2.equals(indexName)) {
                                        indexFiles.remove(j);
                                        exists = true;
                                    }
                                }
                                if (exists) break;
                            }
                            if (exists) break;
                        }
                    }
                    if (exists) break;
                }
            }
            if (exists) break;
        }
        if (!exists) {
            System.out.println("Tables or Database doesn't exist!");
        }
        SaveJSON.save(databases, "databases.json");
    }
}
