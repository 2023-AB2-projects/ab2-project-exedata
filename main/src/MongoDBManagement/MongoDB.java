package MongoDBManagement;
import com.mongodb.*;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;

public class MongoDB {

    private MongoClient mongoClient;
    private MongoDatabase database;
    public MongoDB() {
        mongoClient = null;
    }
    public void connectToLocalhost() {
        try {
            mongoClient = new MongoClient("localhost", 27017);
        } catch (Exception e) {
            System.out.println("Error with connection to localhost!");
        }
    }

    public void disconnectFromLocalhost() {
        mongoClient.close();
    }

    public void createDatabaseOrUse(String databaseName) {
        database = mongoClient.getDatabase(databaseName);
    }

    public void dropDatabase(String databaseName) {
        mongoClient.getDatabase(databaseName).drop();
    }

    public void createCollection(String collectionName) {
        if (mongoClient == null) {
            System.out.println("Firstly you have to connect to MongoDB!");
        } else {
            try {
                database.createCollection(collectionName);
            } catch (Exception e) {
                System.out.println("Collection is already created!");
            }
        }
    }

    public void dropCollection(String collectionName) {
        if (database == null) {
            System.out.println("Firstly you have to select your database!");
        } else {
            database.getCollection(collectionName).drop();
        }
    }

    public void insertOne(String collectionName, Document document) {
        database.getCollection(collectionName).insertOne(document);
    }

    public void deleteOne(String collectionName, String fieldName, String value) {
        database.getCollection(collectionName).deleteOne(Filters.eq(fieldName, value));
    }
}
