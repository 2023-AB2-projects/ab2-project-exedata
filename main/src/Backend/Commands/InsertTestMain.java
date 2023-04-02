package Backend.Commands;

import Backend.Parser;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

public class InsertTestMain {
    public static void main(String[] args) throws ParserConfigurationException, TransformerException {
        Parser.currentDatabaseName = "University";
        Command insert = new Insert("INSERT INTO Students4 (ID, NRMATRICOL) VALUES (5, 2000);");
        insert.performAction();
        Command delete = new Delete("DELETE FROM Students4 WHERE ID = 5;");
        delete.performAction();
    }
}
