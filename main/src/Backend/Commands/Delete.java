package Backend.Commands;

import Backend.Parser;
import MongoDBManagement.MongoDB;
import org.bson.Document;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Delete implements Command {
    private final String command;
    private MongoDB mongoDB;

    public Delete(String command) {
        this.command = command;
        mongoDB = new MongoDB();

        Pattern pattern = Pattern.compile("^\\s*DELETE\\s+FROM\\s+([A-Za-z0-9]+)\\s+WHERE\\s+(.*)\\s+=\\s+(.*);?");
        Matcher matcher = pattern.matcher(command);

        if (matcher.matches()) {
            String tableName = matcher.group(1);
            String fieldName = matcher.group(2);
            String value = matcher.group(3);
            System.out.println("Document deleted!");
        }
        mongoDB.disconnectFromLocalhost();
    }

    @Override
    public void performAction() throws ParserConfigurationException, TransformerException {

    }
}
