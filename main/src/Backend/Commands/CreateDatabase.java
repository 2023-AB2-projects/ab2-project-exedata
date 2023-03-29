package Backend.Commands;

import Backend.Databases.*;
import Backend.SaveLoadJSON.LoadJSON;
import Backend.SaveLoadJSON.SaveJSON;

import java.util.ArrayList;
import java.util.List;

public class CreateDatabase implements Command {
    // create a database with name in json file
    private final String command;

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
        Databases databases = LoadJSON.load("databases.json");
        if (databases == null) {
            List<Database> databaseList = new ArrayList<>();
            List<Table> tableList = new ArrayList<>();
            databaseList.add(new Database(currentDatabaseName, tableList));
            databases = new Databases(databaseList);
        } else {
            if (databases.checkDatabaseExists(currentDatabaseName)) {
                System.out.println("Database is exists!");
            } else {
                List<Table> tableList = new ArrayList<>();
                databases.addDatabase(new Database(currentDatabaseName, tableList));
            }
        }
        SaveJSON.save(databases, "databases.json");

        //System.out.println(databases);

//        Document document = new Document();
//        document.append("name", "John");
//        document.append("age", 30);
//        document.append("email", "john@example.com");
//        database.getCollection("PERSONS").insertOne(document);
//        System.out.println("ok");
    }
}
