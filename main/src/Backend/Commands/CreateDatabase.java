package Backend.Commands;
import java.io.FileWriter;
import java.io.IOException;

import Backend.MongoDBConnection;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class CreateDatabase implements Command {
    // create a database with name in json file
    private String command;

    public CreateDatabase(String command) {
        this.command = command;
    }

    @Override
    public void performAction() {

        JSONObject databases = new JSONObject();
        JSONObject database = new JSONObject();
        JSONObject databaseName = new JSONObject();
        String currentDatabaseName = command.split(" ")[2];
        if (currentDatabaseName.charAt(currentDatabaseName.length()-1) == ';'){
            currentDatabaseName = currentDatabaseName.substring(0,currentDatabaseName.length()-1);
        }
        //CREATE DATABASE PERSONS;
        databaseName.put("@dataBaseName",currentDatabaseName);
        database.put("Database", databaseName);
        databases.put("Databases", database);
        System.out.println(databases);

        JSONArray databasesList = new JSONArray();
        databasesList.add(databases);

        try (FileWriter file = new FileWriter("databases.json")) {
            file.write(databases.toJSONString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        MongoDBConnection mongoDBConnection=new MongoDBConnection();
        mongoDBConnection.connect();
    }
}
