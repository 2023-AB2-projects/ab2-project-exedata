package Backend.Commands;

import Backend.Parser;
import Backend.SaveLoadJSON.SaveJSON;
import Backend.SocketServer.ErrorClient;

import static Backend.SocketServer.Server.databases;
import static Backend.SocketServer.Server.mongoDB;

public class DropTable implements Command {
    // drop table from json file
    private final String command;

    public DropTable(String command) {
        this.command = command;

    }

    @Override
    public void performAction() {
        //drop table tableName;
        String currentTableName = command.split(" ")[2];
        if (databases == null) {
            ErrorClient.send("JSONFile Doesn't exists!");
        } else {
            databases.getDatabase(Parser.currentDatabaseName).dropTable(currentTableName);
            SaveJSON.save(databases, "databases.json");
            mongoDB.createDatabaseOrUse(Parser.currentDatabaseName);
            mongoDB.dropCollection(currentTableName);
            ErrorClient.send("The " + currentTableName + "table is deleted!");
        }
    }
}
