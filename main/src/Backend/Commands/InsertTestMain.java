package Backend.Commands;

import Backend.Parser;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

public class InsertTestMain {
    public static void main(String[] args) throws ParserConfigurationException, TransformerException {
        CreateIndex createIndex = new CreateIndex("CREATE INDEX DisciplinesInd ON disciplines (CreditNr, DiscID);");
        createIndex.performAction();

    }
}
