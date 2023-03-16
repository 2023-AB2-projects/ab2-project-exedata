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

        String currentDatabaseName = command.split(" ")[2];
        if (currentDatabaseName.charAt(currentDatabaseName.length() - 1) == ';') {
            currentDatabaseName = currentDatabaseName.substring(0, currentDatabaseName.length() - 1);
        }
        //CREATE DATABASE PERSONS;
        JSONObject databaseName = new JSONObject();
        databaseName.put("@dataBaseName", currentDatabaseName);
        JSONObject database = new JSONObject();
        database.put("Database", databaseName);

        JSONArray databasesList = new JSONArray();
        databasesList.add(database);

        JSONObject databases = new JSONObject();
        databases.put("Databases", databasesList);
        System.out.println(databases);

        try (FileWriter file = new FileWriter("databases.json")) {
            file.write(databases.toJSONString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //MongoDBConnection mongoDBConnection = new MongoDBConnection();
        //mongoDBConnection.connect();
    }
}
