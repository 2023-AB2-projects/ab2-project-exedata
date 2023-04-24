package Backend.MongoDBManagement;

import Backend.Backend;
import Backend.SocketServer.ErrorClient;
import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;

import java.util.Arrays;
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
            ErrorClient.send("Error with connection to localhost!");
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

    public void updateDocument(String id, String collectionName, String appendString){
        Document update = new Document("$set", new Document("field1", new Document("$concat", Arrays.asList("$field1", appendString))));
        database.getCollection(collectionName).updateOne(eq("_id", new ObjectId(id)), update);
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
            Backend.goodInsert = true;
        } else {
            System.out.println("Insert error to " + collectionName + ", " + document.get("_id") + " primary key already exists!");
            ErrorClient.send("Insert error to " + collectionName + ", " + document.get("_id") + " primary key already exists!");
        }
    }

    public void insertMany(String collectionName, List<Document> documents) {
        for (int i = 0; i < documents.size(); i++) {
            if (!existsID(collectionName, documents.get(i))) {
                database.getCollection(collectionName).insertOne(documents.get(i));
                System.out.println("Document inserted to " + collectionName + "!" + " (data: " + i + ")");
                Backend.goodInsert = true;
            } else {
                System.out.println("Insert error to " + collectionName + ", primary key already exists!" + " (data: " + i + ")");
                ErrorClient.send("Insert error to " + collectionName + ", primary key already exists!" + " (data: " + i + ")");
            }
        }
    }

    public void deleteOne(String collectionName, String fieldName, String value) {
        database.getCollection(collectionName).deleteOne(Filters.eq(fieldName, value));
        System.out.println("Document deleted from " + collectionName + "!");
        Backend.goodDelete = true;
    }

    public void deleteMany(String collectionName, String fieldName, String value) {
        database.getCollection(collectionName).deleteMany(Filters.eq(fieldName, value));
        System.out.println("All documents delete from " + collectionName + "!");
        Backend.goodDelete = true;
    }

    public void deleteAll(String collectionName) {
        database.getCollection(collectionName).drop();
        database.createCollection(collectionName);
    }

    public boolean existsID(String collectionName, Document document) {
        return (database.getCollection(collectionName).find(new Document("_id", document.get("_id"))).first() != null);
    }

    public MongoCollection<Document> getDocuments(String collectionName) {
        return database.getCollection(collectionName);
    }
}
