package Backend.Commands;

import Backend.Databases.Databases;
import Backend.SaveLoadJSON.LoadJSON;
import Backend.SaveLoadJSON.SaveJSON;
import Backend.SocketServer.ErrorClient;

import static Backend.SocketServer.Server.mongoDB;

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
            ErrorClient.send("JSONFile Doesn't exists!");
            return;
        } else {
            databases.dropDatabase(currentDatabaseName);
        }
        SaveJSON.save(databases, "databases.json");
        mongoDB.dropDatabase(currentDatabaseName);
        ErrorClient.send(currentDatabaseName + " is deleted!");
    }
}
