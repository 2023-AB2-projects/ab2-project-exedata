package Backend.Commands;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

public class Select implements Command {
    private final String command;

    public Select(String command) {
        this.command = command;
    }

    @Override
    public void performAction() throws ParserConfigurationException, TransformerException {
        System.out.println("Select command");
    }
}
