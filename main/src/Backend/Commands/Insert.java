package Backend.Commands;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

public class Insert implements Command {
    private final String command;

    public Insert(String command) {
        this.command = command;
    }

    @Override
    public void performAction() throws ParserConfigurationException, TransformerException {

    }
}
