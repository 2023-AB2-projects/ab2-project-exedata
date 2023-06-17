package Backend.Commands;

import Backend.Databases.*;
import Backend.SaveLoadJSON.SaveJSON;
import Backend.SocketServer.ErrorClient;

import java.util.ArrayList;
import java.util.List;

import static Backend.SocketServer.Server.databases;
import static Backend.SocketServer.Server.mongoDB;


public class CreateDatabase implements Command {
    // create a database with name in json file
    private final String command;

    public CreateDatabase(String command) {
        this.command = command;
    }

    @Override
    public void performAction() {
        String currentDatabaseName = command.split(" ")[2];
        if (databases == null) {
            List<Database> databaseList = new ArrayList<>();
            List<Table> tableList = new ArrayList<>();
            databaseList.add(new Database(currentDatabaseName, tableList));
            databases = new Databases(databaseList);
        } else {
            if (databases.checkDatabaseExists(currentDatabaseName)) {
                System.out.println("Database is exists!");
                ErrorClient.send("Database is exists!");
                return;
            } else {
                List<Table> tableList = new ArrayList<>();
                databases.addDatabase(new Database(currentDatabaseName, tableList));
            }
        }
        SaveJSON.save(databases, "databases.json");
        mongoDB.createDatabaseOrUse(currentDatabaseName);
        ErrorClient.send("Database " + currentDatabaseName + " created!");
    }
}
