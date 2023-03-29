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
        String currentDatabaseName = command.split(" ")[2];
        if (currentDatabaseName.charAt(currentDatabaseName.length() - 1) == ';') {
            currentDatabaseName = currentDatabaseName.substring(0, currentDatabaseName.length() - 1);
        }

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
