package Backend;
import Backend.Exceptions.UnknownCommandException;
import Backend.HttpServer.Server;
import Backend.SaveLoadJSON.LoadJSON;
import Backend.SaveLoadJSON.SaveJSON;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

public class Backend {
    public static void main(String[] args) throws IOException {
        Server server = new Server(12000);
        //server.runServer();
        try {
            Parser.commandType("CREATE DATABASE PERSONS;").performAction(); //create a new database
        } catch (UnknownCommandException | ParserConfigurationException | TransformerException e) {
            throw new RuntimeException(e);
        }
        JSONObject a = new JSONObject();
        //SaveJSON.save(a, "databases.json");
        //JSONObject b = LoadJSON.load("databases.json");
        //System.out.println(b);

    }
}
