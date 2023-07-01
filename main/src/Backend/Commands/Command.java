package Backend.Commands;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

public interface Command {
    void performAction() throws ParserConfigurationException, TransformerException;
}
