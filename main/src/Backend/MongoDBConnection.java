package Backend;
import com.mongodb.*;

public class MongoDBConnection {

    public static MongoClient connect() {
        MongoClient mongoClient = null;
        try {
            mongoClient = new MongoClient("localhost", 27017);
        } catch (Exception e) {
            System.out.println("Error to connect localhost");
        }
        return mongoClient;
    }
}
