package Backend.MongoDBManagement;

public class MongoTest {
    public static void main(String[] args) {
        MongoDB mongoDB = new MongoDB();
        mongoDB.connectToLocalhost();

//        mongoDB.useOrCreateDatabase("new");
//        Document document= new Document();
//        document.append("ID", "1");
//        document.append("Name", "John Smith");
//        document.append("Age", "45");
//        document.append("Location", "New York");
//        //mongoDB.insertOne("new", document);
//        mongoDB.deleteOne("new", "ID", "1");


    }
}
