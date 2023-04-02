package Backend.Commands;

import Backend.Databases.*;
import Backend.SaveLoadJSON.LoadJSON;
import Backend.SaveLoadJSON.SaveJSON;
import MongoDBManagement.MongoDB;

import java.util.ArrayList;
import java.util.List;

import static Backend.SocketServer.Server.errorClient;

public class CreateDatabase implements Command {
    // create a database with name in json file
    private final String command;

    public CreateDatabase(String command) {
        this.command = command;
    }

    @Override
    public void performAction() {
        //CREATE DATABASE PERSONS;
        String currentDatabaseName = command.split(" ")[2];
        Databases databases = LoadJSON.load("databases.json");
        if (databases == null) {
            List<Database> databaseList = new ArrayList<>();
            List<Table> tableList = new ArrayList<>();
            databaseList.add(new Database(currentDatabaseName, tableList));
            databases = new Databases(databaseList);
        } else {
            if (databases.checkDatabaseExists(currentDatabaseName)) {
                System.out.println("Database is exists!");
                errorClient.send("Database is exists!");
                return;
            } else {
                List<Table> tableList = new ArrayList<>();
                databases.addDatabase(new Database(currentDatabaseName, tableList));
            }
        }
        SaveJSON.save(databases, "databases.json");
        MongoDB mongoDB = new MongoDB();
        mongoDB.createDatabaseOrUse(currentDatabaseName);
        mongoDB.disconnectFromLocalhost();
    }
}
