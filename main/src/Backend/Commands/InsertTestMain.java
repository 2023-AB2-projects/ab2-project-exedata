package Backend.Commands;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

public class InsertTestMain {
    public static void main(String[] args) throws ParserConfigurationException, TransformerException {
        Command insert = new Insert("INSERT INTO disciplines (DiscID, CreditNr, DName) VALUES (5, 100, \"Jozsef\");");
        insert.performAction();
    }
}
