package Backend.Commands;

import Backend.SaveLoadJSON.SaveJSON;
import Backend.SocketServer.ErrorClient;

import static Backend.SocketServer.Server.databases;
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
        if (databases == null) {
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
