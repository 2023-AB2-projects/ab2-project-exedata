package Backend.Commands;

import Backend.Databases.Databases;
import Backend.SaveLoadJSON.LoadJSON;
import Backend.SaveLoadJSON.SaveJSON;
import MongoDBManagement.MongoDB;

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
            return;
        } else {
            databases.dropDatabase(currentDatabaseName);
        }
        SaveJSON.save(databases, "databases.json");
        MongoDB mongoDB = new MongoDB();
        mongoDB.dropDatabase(currentDatabaseName);
        mongoDB.disconnectFromLocalhost();
    }
}
