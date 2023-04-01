package Backend.Commands;

import Backend.Databases.Databases;
import Backend.SaveLoadJSON.LoadJSON;
import Backend.SaveLoadJSON.SaveJSON;

public class DropDatabase implements Command {
    // Drop database from json file
    private final String command;

    public DropDatabase(String command) {
        this.command = command;
    }

    @Override
    public void performAction() {
        //DROP DATABASE PERSONS;
        String currentDatabaseName = command.split(" ")[2];

        Databases databases = LoadJSON.load("databases.json");
        if (databases == null) {
            System.out.println("JSONFile Doesn't exists!");
        } else {
            databases.dropDatabase(currentDatabaseName);
        }
        SaveJSON.save(databases, "databases.json");

//        MongoClient mongoClient = MongoDBConnection.connect();
//        MongoDatabase database = mongoClient.getDatabase(currentDatabaseName);
//        database.drop();
    }
}
