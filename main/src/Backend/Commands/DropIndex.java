package Backend.Commands;

import Backend.Parser;
import Backend.SaveLoadJSON.SaveJSON;
import Backend.SocketServer.ErrorClient;

import static Backend.Parser.currentDatabaseName;
import static Backend.SocketServer.Server.databases;
import static Backend.SocketServer.Server.mongoDB;

public class DropIndex implements Command {
    private final String command;

    public DropIndex(String command) {
        this.command = command;
    }

    @Override
    public void performAction() {
        //DROP INDEX index_name ON table_name;
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
                mongoDB.createDatabaseOrUse(currentDatabaseName);
                mongoDB.dropCollection(indexName);
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
