package Backend.Commands;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

public class Use implements Command{
    private String command;

    public Use(String command) {
        this.command = command;
    }
    @Override
    public void performAction() throws ParserConfigurationException, TransformerException {
        //USE persons;
        String currentDatabaseName = command.split(" ")[1];
        currentDatabaseName=currentDatabaseName.substring(0,currentDatabaseName.length()-1);

    }
}
