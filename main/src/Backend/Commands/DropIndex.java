package Backend.Commands;

import Backend.Databases.Databases;
import Backend.MongoDBManagement.MongoDB;
import Backend.Parser;
import Backend.SaveLoadJSON.LoadJSON;
import Backend.SaveLoadJSON.SaveJSON;
import Backend.SocketServer.ErrorClient;

import static Backend.Parser.currentDatabaseName;

public class DropIndex implements Command {
    private final String command;

    public DropIndex(String command) {
        this.command = command;
    }

    @Override
    public void performAction() {
        //DROP INDEX index_name ON table_name;
        Databases databases = LoadJSON.load("databases.json");
        if (databases == null) {
            System.out.println("Databases doesn't exists!");
            ErrorClient.send("Databases doesn't exists!");
            return;
        }
        String[] commandWords = command.split(" ");
        String currentTableName = commandWords[4];
        String indexName = commandWords[2];

        if (databases.getDatabase(Parser.currentDatabaseName) != null) {
            if (databases.getDatabase(Parser.currentDatabaseName).checkTableExists(currentTableName)) {
                databases.getDatabase(currentDatabaseName).getTable(currentTableName).dropIndex(indexName);
                MongoDB mongoDB =new MongoDB();
                mongoDB.createDatabaseOrUse(currentDatabaseName);
                mongoDB.dropCollection(indexName);
                mongoDB.disconnectFromLocalhost();
                SaveJSON.save(databases, "databases.json");
                ErrorClient.send("The " + indexName + "index is deleted!");
            } else {
                System.out.println("Table doesn't exists!");
                ErrorClient.send("Table doesn't exists!");
            }
        } else {
            System.out.println("Database doesn't exists!");
            ErrorClient.send("Databases doesn't exists!");
        }
    }
}
