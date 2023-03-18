package Backend;

import Backend.Exceptions.UnknownCommandException;
import Backend.HttpServer.Server;
import Backend.SaveLoadJSON.LoadJSON;
import Backend.SaveLoadJSON.SaveJSON;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

public class Backend {
    public static void main(String[] args) throws IOException {
        Server server = new Server(12000);
        //server.runServer();
        try {
            Parser.commandType("CREATE TABLE Alkalmazottak ( " +
                    "SzemSzám VARCHAR PRIMARY KEY, " +
                    "Név VARCHAR UNIQUE, PRIMARY KEY (KocsmaID, ItalID), " +
                    "RészlegID INT REFERENCES Részlegek (RészlegID), first_name VARCHAR NOT NULL, " +
                    "FOREIGN KEY (store_id) REFERENCES sales.stores (store_id), order_date DATE DEFAULT CURRENT_DATE, " +
                    "status VARCHAR CHECK (status IN ('New', 'Processing', 'Shipped', 'Delivered')), " +
                    "CONSTRAINT fk_customer FOREIGN KEY (customer_id) REFERENCES customers (customer_id), " +
                    "CONSTRAINT pk_order_details PRIMARY KEY (order_id, customer_id), " +
                    "Fizetés INT);").performAction(); //create a new database
        } catch (UnknownCommandException | ParserConfigurationException | TransformerException e) {
            throw new RuntimeException(e);
        }
    }
}
