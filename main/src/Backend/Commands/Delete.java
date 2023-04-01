package Backend.Commands;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

public class Delete implements Command {
    private final String command;

    public Delete(String command) {
        this.command = command;
    }

    @Override
    public void performAction() throws ParserConfigurationException, TransformerException {

    }
}

