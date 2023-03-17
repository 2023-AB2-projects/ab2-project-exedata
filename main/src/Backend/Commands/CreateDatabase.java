package Backend.Commands;
import Backend.MongoDBConnection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.*;
import org.bson.Document;
import org.json.simple.JSONObject;

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
            System.out.println("Error. Collection is already created!");
        }
        JSONObject primaryKey1 = new JSONObject();

        Document document = new Document();
        document.append("name", "John");
        document.append("age", 30);
        document.append("email", "john@example.com");
        database.getCollection("PERSONS").insertOne(document);
        System.out.println("ok");

//        //CREATE DATABASE PERSONS;
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
