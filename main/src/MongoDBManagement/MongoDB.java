package MongoDBManagement;
import com.mongodb.*;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.List;

public class MongoDB {

    private MongoClient mongoClient;
    private MongoDatabase database;
    public MongoDB() {
        mongoClient = null;
        connectToLocalhost();
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
        if (!existsID(collectionName, document)) {
            database.getCollection(collectionName).insertOne(document);
            System.out.println("Document inserted to " + collectionName + "!");
        } else {
            System.out.println("Insert error to " + collectionName + ", _id already exists!");
        }
    }

    public void insertMany(String collectionName, List<Document> documents) {
        for(int i=0; i<documents.size(); i++) {
            if (!existsID(collectionName, documents.get(i))) {
                database.getCollection(collectionName).insertOne(documents.get(i));
                System.out.println("Document inserted to " + collectionName + "!" + " (data: " + i + ")");
            } else {
                System.out.println("Insert error to " + collectionName + ", _id already exists!" + " (data: " + i + ")");
            }
        }
    }

    public void deleteOne(String collectionName, String fieldName, String value) {
        database.getCollection(collectionName).deleteOne(Filters.eq(fieldName, value));
        System.out.println("Document deleted from " + collectionName + "!");
    }

    public void deleteMany(String collectionName, String fieldName, String value) {
        database.getCollection(collectionName).deleteMany(Filters.eq(fieldName, value));
        System.out.println("All documents delete from " + collectionName + "!");
    }

    public void deleteAll(String collectionName) {
        database.getCollection(collectionName).drop();
        database.createCollection(collectionName);
    }

    public boolean existsID(String collectionName, Document document) {
        return (database.getCollection(collectionName).find(new Document("_id", document.get("_id"))).first()!=null);
    }
}
