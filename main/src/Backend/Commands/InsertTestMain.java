package Backend.Commands;

import Backend.Parser;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

public class InsertTestMain {
    public static void main(String[] args) throws ParserConfigurationException, TransformerException {
        Parser.currentDatabaseName = "University";
        //Command insert = new Insert("INSERT INTO disciplines (DiscID, DName) VALUES (9, \"Anna\");");
        //insert.performAction();
        Command delete = new Delete("DELETE FROM disciplines WHERE DiscID = 5 AND DiscID = 10;");
        delete.performAction();
    }
}
