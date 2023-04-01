package Backend.Commands;

import Backend.Parser;
import MongoDBManagement.MongoDB;
import org.bson.Document;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Insert implements Command {
    private final String command;
    private MongoDB mongoDB;

    public Insert(String command) {
        this.command = command;
        mongoDB = new MongoDB();

        Pattern pattern = Pattern.compile("^\\s*INSERT\\s+INTO\\s+([A-Za-z0-9]+)\\s+\\((.*)\\)\\s+VALUES\\s+\\((.*)\\);?");
        Matcher matcher = pattern.matcher(command);

        if (matcher.matches()) {
            String tableName = matcher.group(1);
            String[] fieldName = matcher.group(2).replaceAll("\\s+", "").split(",");
            String[] value = matcher.group(3).replaceAll("\\s+", "").split(",");

            // if(insertValidation(tableName,fieldName,value)) {
            Document document = new Document();
            for(int i=0; i<fieldName.length; i++) {
                if (value[i].charAt(0) == '\"') {
                    document.append(fieldName[i], value[i].substring(1, value[i].length()-1));
                } else {
                    document.append(fieldName[i], value[i]);
                }
            }
            mongoDB.createDatabaseOrUse(Parser.currentDatabaseName);
            mongoDB.insertOne(Parser.currentDatabaseName, document);
            System.out.println("Document inserted!");
            // }
        }

    }

    @Override
    public void performAction() throws ParserConfigurationException, TransformerException {

    }
}
