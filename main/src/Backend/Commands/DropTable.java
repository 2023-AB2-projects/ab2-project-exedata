package Backend.Commands;

import Backend.Parser;
import Backend.SaveLoadJSON.LoadJSON;
import Backend.SaveLoadJSON.SaveJSON;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class DropTable implements Command {
    // drop table from json file
    private String command;

    public DropTable(String command) {
        this.command = command;

    }

    @Override
    public void performAction() {
        String currentTableName = command.split(" ")[2];
        if (currentTableName.charAt(currentTableName.length() - 1) == ';') {
            currentTableName = currentTableName.substring(0, currentTableName.length() - 1);
        }
        JSONObject databases = LoadJSON.load("databases.json");
        if (databases == null) {
            System.out.println("JSONFile Doesn't exists!");
        } else {
            JSONArray jsonArray = (JSONArray) databases.get("Databases"); //main name
            for (Object object : jsonArray) {
                JSONObject jsonObjectSearch = (JSONObject) object;
                String name = (String) jsonObjectSearch.get("databaseName");
                if (name != null && name.equals(Parser.currentDatabaseName)) { //searched databaseName
                    JSONArray tables = (JSONArray) jsonObjectSearch.get("Tables");
                    for (Object object2 : tables) {
                        JSONObject table = (JSONObject) object2;
                        String tableName = (String) table.get("tableName");
                        if (tableName != null && tableName.equals(currentTableName)) {
                            tables.remove(table);
                            break;
                        }
                    }
                    break;
                }
            }
            SaveJSON.save(databases, "databases.json");
        }

    }
}
