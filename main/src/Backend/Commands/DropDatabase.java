package Backend.Commands;
import java.io.FileWriter;
import java.io.IOException;

import Backend.MongoDBConnection;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class DropDatabase implements Command {
    // Drop database from json file
    private String command;

    public DropDatabase(String command) {
        this.command = command;
    }

    @Override
    public void performAction() {
        String currentDatabaseName = command.split(" ")[2];
        if (currentDatabaseName.charAt(currentDatabaseName.length() - 1) == ';') {
            currentDatabaseName = currentDatabaseName.substring(0, currentDatabaseName.length() - 1);
        }
        MongoClient mongoClient = MongoDBConnection.connect();
        MongoDatabase database = mongoClient.getDatabase(currentDatabaseName);
        database.drop();
    }
}
