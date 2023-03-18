package Backend.Commands;

import java.io.FileWriter;
import java.io.IOException;

import Backend.MongoDBConnection;
import Backend.SaveLoadJSON.LoadJSON;
import Backend.SaveLoadJSON.SaveJSON;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class DropDatabase implements Command {
    // Drop database from json file
    private String command;

    public DropDatabase(String command) {
        this.command = command;
    }

    @Override
    public void performAction() {

        String currentDatabaseName = command.split(" ")[2];
        if (currentDatabaseName.charAt(currentDatabaseName.length() - 1) == ';') {
            currentDatabaseName = currentDatabaseName.substring(0, currentDatabaseName.length() - 1);
        }
        //System.out.println(currentDatabaseName);


        JSONObject databases = LoadJSON.load("databases.json");
        if (databases == null) {
            System.out.println("JSONFile Doesn't exists!");
        } else {
            JSONArray jsonArray = (JSONArray) databases.get("Databases"); //main name
            for (Object object : jsonArray) {
                JSONObject jsonObjectSearch = (JSONObject) object;
                String name = (String) jsonObjectSearch.get("databaseName");
                if (name != null && name.equals(currentDatabaseName)) { //searched databasename
                    jsonArray.remove(object);
                    break;
                }
            }
            SaveJSON.save(databases, "databases.json");
        }



//        MongoClient mongoClient = MongoDBConnection.connect();
//        MongoDatabase database = mongoClient.getDatabase(currentDatabaseName);
//        database.drop();
    }
}
