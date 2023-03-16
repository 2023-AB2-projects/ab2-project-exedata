package Backend;
import Backend.Exceptions.UnknownCommandException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

public class Backend {
    public static void main(String[] args) throws IOException {
        // Create the HTTP server and start listening to requests
        // Server server = new Server(12000);
        // server.runServer();
        try {
            Parser.commandType("CREATE DATABASE PERSONS;").performAction(); //create a new database
        } catch (UnknownCommandException | ParserConfigurationException | TransformerException e) {
            throw new RuntimeException(e);
        }

    }
}
