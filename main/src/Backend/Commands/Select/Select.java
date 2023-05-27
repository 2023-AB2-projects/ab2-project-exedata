package Backend.Commands.Select;

import Backend.Commands.Command;
import Backend.Commands.Condition;
import Backend.Databases.Attribute;
import Backend.Databases.IndexFile;
import Backend.Databases.Table;
import Backend.Parser;
import Backend.SocketServer.ErrorClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.PrintWriter;
import java.util.*;

import static Backend.SocketServer.Server.databases;
import static Backend.SocketServer.Server.mongoDB;
import static com.mongodb.client.model.Filters.*;

public class Select implements Command {
    private final String command;
    private final PrintWriter writer;

    public Select(String command, PrintWriter w) {
        this.command = command;
        writer = w;
    }

    @Override
    public void performAction() throws ParserConfigurationException, TransformerException {
        //SELECT * FROM disciplines WHERE CreditNr > 4 AND DName=Databases1;
        SelectManager selectManager = new SelectManager(command, databases);
        String massage = selectManager.processing();
        if (massage != null) {
            ErrorClient.send(massage);
            System.out.println(massage);
            sendData(new ArrayList<>());
            return;
        }
        Selection selection = new Selection(selectManager);

        List<Document> result = selection.processing(0);
        System.out.println(result);
//        List<String> projectionResult = projection(result, selectManager);
//        sendData(projectionResult);
    }

    private void sendData(List<String> result) {
        System.out.println("Sending data...");
        for (String i : result) {
            writer.println(i);
        }
        writer.println("null");
        System.out.println("Finished!");
    }
}
