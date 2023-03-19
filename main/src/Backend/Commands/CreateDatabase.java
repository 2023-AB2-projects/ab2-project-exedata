package Backend.Commands;

import Backend.MongoDBConnection;
import Backend.SaveLoadJSON.LoadJSON;
import Backend.SaveLoadJSON.SaveJSON;
import com.mongodb.client.MongoDatabase;
import com.mongodb.*;
import com.mongodb.util.JSON;
import org.bson.Document;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.io.IOException;

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
//        MongoClient mongoClient = MongoDBConnection.connect();
//        MongoDatabase database = mongoClient.getDatabase(currentDatabaseName);
//        try {
//            database.createCollection(currentDatabaseName);
//        } catch (Exception e) {
//            System.out.println("Error. Collection is already created!");
//        }

        //CREATE DATABASE PERSONS;
        JSONArray TablesArray = new JSONArray();

        JSONObject database = new JSONObject();
        database.put("databaseName", currentDatabaseName);
        database.put("Tables", TablesArray);

        JSONObject databases = LoadJSON.load("databases.json");
        if (databases == null) {
            databases = new JSONObject();
            JSONArray databasesArray = new JSONArray();
            databasesArray.add(database);
            databases.put("Databases", databasesArray);
            SaveJSON.save(databases, "databases.json");
        } else {
            boolean exists = false;
            JSONArray jsonArray = (JSONArray) databases.get("Databases"); //main name
            for (Object object : jsonArray) {
                JSONObject jsonObjectSearch = (JSONObject) object;
                String name = (String) jsonObjectSearch.get("databaseName");
                if (name != null && name.equals(currentDatabaseName)) { //searched databasename
                    System.out.println(currentDatabaseName + " database is already exists!");
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                jsonArray.add(database);
                SaveJSON.save(databases, "databases.json");
            }
        }
        //System.out.println(databases);

//        Document document = new Document();
//        document.append("name", "John");
//        document.append("age", 30);
//        document.append("email", "john@example.com");
//        database.getCollection("PERSONS").insertOne(document);
//        System.out.println("ok");
    }
}
