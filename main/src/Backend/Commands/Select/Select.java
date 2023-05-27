package Backend.Commands.Select;

import Backend.Commands.Command;
import Backend.Commands.Condition;
import Backend.Databases.Attribute;
import Backend.Databases.IndexFile;
import Backend.Databases.Table;
import Backend.Parser;
import Backend.SocketServer.ErrorClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.PrintWriter;
import java.util.*;

import static Backend.SocketServer.Server.databases;
import static Backend.SocketServer.Server.mongoDB;
import static com.mongodb.client.model.Filters.*;

public class Select implements Command {
    private final String command;
    private final PrintWriter writer;

    public Select(String command, PrintWriter w) {
        this.command = command;
        writer = w;
    }

    @Override
    public void performAction() throws ParserConfigurationException, TransformerException {
        //SELECT * FROM disciplines WHERE CreditNr > 4 AND DName=Databases1;
        SelectManager selectManager = new SelectManager(command, databases);
        String massage = selectManager.processing();
        if (massage != null) {
            ErrorClient.send(massage);
            System.out.println(massage);
            sendData(new ArrayList<>());
            return;
        }
        Projection projection = new Projection(selectManager);

        List<Document> result = projection.processing(0);
        System.out.println(result);
//        List<String> projectionResult = projection(result, selectManager);
//        sendData(projectionResult);
    }

    private void sendData(List<String> result) {
        System.out.println("Sending data...");
        for (String i : result) {
            writer.println(i);
        }
        writer.println("null");
        System.out.println("Finished!");
    }

    private List<String> projection(List<Document> values, SelectManager selectManager) {
        List<String> result = new ArrayList<>();
        StringBuilder columns = new StringBuilder();
        List<String> select = selectManager.getSelect();
        List<String> selectAS = selectManager.getSelectAS();
        for (int i = 0; i < select.size(); i++) {
            if (selectAS.get(i) != null)
                columns.append(selectAS.get(i)).append("#");
            else
                columns.append(select.get(i)).append("#");
        }
        result.add(columns.substring(0, columns.length() - 1));
        int[] indexArray = createIndexArray(select, selectManager.getFrom().get(0));
        //id nev email
        //id nev kor email
        //-1 0 2
        for (Document i : values) {
            result.add(getSelectedAttribute(i, indexArray));
        }
        return result;
    }

    private String getSelectedAttribute(Document document, int[] indexArray) {
        String[] primaryKey = ((String) document.get("_id")).split("#");
        String[] attribute = ((String) document.get("Value")).split("#");
        StringBuilder result = new StringBuilder();
        for (int i : indexArray) {
            if (i < 0) {
                result.append(primaryKey[(i + 1) * (-1)]).append("#");
            } else {
                result.append(attribute[i]).append("#");
            }
        }
        return result.substring(0, result.length() - 1);
    }

    private int[] createIndexArray(List<String> select, String tableName) {
        int[] result = new int[select.size()];
        List<Attribute> attributeList = databases.getDatabase(Parser.currentDatabaseName).getTable(tableName).getStructure();
        List<String> primaryKey = databases.getDatabase(Parser.currentDatabaseName).getTable(tableName).getPrimaryKey();
        //id nev email
        //id nev kor email
        //-1 0 2
        String column;
        int primaryKeyIndex;
        int attributeIndex;
        boolean isPrimaryKey;
        for (int i = 0; i < select.size(); i++) {
            if (select.get(i).contains(".")) {
                column = select.get(i).split("\\.")[1];
            } else {
                column = select.get(i);
            }
            isPrimaryKey = primaryKey.contains(column);
            primaryKeyIndex = 0;
            attributeIndex = 0;
            for (Attribute attribute : attributeList) {
                if (primaryKey.contains(attribute.getName())) {
                    primaryKeyIndex--;
                } else {
                    attributeIndex++;
                }
                if (attribute.getName().equals(column)) {
                    break;
                }
            }
            if (isPrimaryKey) {
                result[i] = primaryKeyIndex;
            } else {
                result[i] = attributeIndex - 1;
            }
        }
        return result;
    }
}
