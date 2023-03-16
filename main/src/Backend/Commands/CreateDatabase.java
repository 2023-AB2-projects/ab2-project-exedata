package Backend.Commands;
import Backend.MongoDBConnection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.*;

public class CreateDatabase implements Command {
    // create a database with name in json file
    private String command;

    public CreateDatabase(String command) {
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
        try {
            database.createCollection(currentDatabaseName);
        } catch (Exception e) {
            System.out.println("Collection is already created!");
        }

//        //CREATE DATABASE PERSONS;
//        JSONObject databaseName = new JSONObject();
//        databaseName.put("@dataBaseName", currentDatabaseName);
//        JSONObject database = new JSONObject();
//        database.put("Database", databaseName);
//
//        JSONArray databasesList = new JSONArray();
//        databasesList.add(database);
//
//        JSONObject databases = new JSONObject();
//        databases.put("Databases", databasesList);
//        System.out.println(databases);
//
//        try (FileWriter file = new FileWriter("databases.json")) {
//            file.write(databases.toJSONString());
//            file.flush();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
