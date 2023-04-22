package Backend.Commands;

import Backend.Databases.Databases;
import Backend.Parser;
import Backend.SaveLoadJSON.LoadJSON;
import Backend.SaveLoadJSON.SaveJSON;
import Backend.SocketServer.ErrorClient;
import Backend.MongoDBManagement.MongoDB;

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

        Databases databases = LoadJSON.load("databases.json");
        if (databases == null) {
            System.out.println("JSONFile Doesn't exists!");
            ErrorClient.send("JSONFile Doesn't exists!");
        } else {
            databases.getDatabase(Parser.currentDatabaseName).dropTable(currentTableName);
            SaveJSON.save(databases, "databases.json");
            MongoDB mongoDB = new MongoDB();
            mongoDB.createDatabaseOrUse(Parser.currentDatabaseName);
            mongoDB.dropCollection(currentTableName);
            mongoDB.disconnectFromLocalhost();
            ErrorClient.send("The " + currentTableName + "table is deleted!");
        }
    }
}
