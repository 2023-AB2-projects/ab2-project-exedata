package Backend.Commands;

import Backend.Databases.Databases;
import Backend.Databases.IndexFile;
import Backend.MongoDBManagement.MongoDB;
import Backend.Parser;
import Backend.SaveLoadJSON.LoadJSON;
import Backend.SocketServer.ErrorClient;
import org.bson.Document;
import org.bson.conversions.Bson;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.util.*;

import static com.mongodb.client.model.Filters.*;

public class Select implements Command {
    private final String command;
    private Databases databases;
    private MongoDB mongoDB;

    public Select(String command) {
        this.command = command;
    }

    @Override
    public void performAction() throws ParserConfigurationException, TransformerException {
        mongoDB = new MongoDB();
        databases = LoadJSON.load("databases.json");
        SelectManager selectManager = new SelectManager(command, databases);
        String massage = selectManager.check();
        if (massage != null) {
            ErrorClient.send(massage);
            System.out.println(massage);
            return;
        }
        List<Document> result = processing(selectManager);
    }

    private List<Document> processing(SelectManager selectManager) {
        //megnezni es vegig jarni a where-t, hogy van-e index az adott mezon
        //SELECT * FROM disciplines WHERE CreditNr > 4 AND DName=Databases1;
        List<Condition> restWhere = new ArrayList<>();
        Set<String> primaryKeySet = null;
        Set<String> tempPrimaryKeySet;
        for (Condition i : selectManager.getWhere()) {
            //ha van index allomany a feltetelen
            tempPrimaryKeySet = getPrimaryKeySetIfHaveIndexFile(i);
            if (tempPrimaryKeySet == null) {
                restWhere.add(i);//nem hasznaltuk fel
            } else {
                if (primaryKeySet == null)
                    primaryKeySet = tempPrimaryKeySet;
                else
                    primaryKeySet.retainAll(tempPrimaryKeySet);
            }
        }
        return null;
    }

    private Set<String> getPrimaryKeySetIfHaveIndexFile(Condition i) {
        String table1;
        String table2;
        String column1;
        String column2;
        IndexFile indexFile1 = null;
        IndexFile indexFile2 = null;
        if (i.getLeftSide().contains(".")) {
            table1 = i.getLeftSide().split("\\.")[0];
            column1 = i.getLeftSide().split("\\.")[1];
            indexFile1 = databases.getDatabase(Parser.currentDatabaseName).getTable(table1).getIndexFileIfKnowTheAttributes(new String[]{column1});
        } else {
            column1 = i.getLeftSide();
            table1 = databases.getDatabase(Parser.currentDatabaseName).whichTableContainsThisAttribute(column1);
            if (table1 != null)
                indexFile1 = databases.getDatabase(Parser.currentDatabaseName).getTable(table1).getIndexFileIfKnowTheAttributes(new String[]{column1});
        }
        if (i.getRightSide().contains(".")) {
            table2 = i.getRightSide().split("\\.")[0];
            column2 = i.getRightSide().split("\\.")[1];
            indexFile2 = databases.getDatabase(Parser.currentDatabaseName).getTable(table2).getIndexFileIfKnowTheAttributes(new String[]{column2});
        } else {
            column2 = i.getRightSide();
            table2 = databases.getDatabase(Parser.currentDatabaseName).whichTableContainsThisAttribute(column2);
            if (table2 != null)
                indexFile2 = databases.getDatabase(Parser.currentDatabaseName).getTable(table2).getIndexFileIfKnowTheAttributes(new String[]{column2});
        }
        // a.alma > a.korte
        // a.alma > 0
        // 0 > a.alma
        Set<String> result = null;
        if (indexFile1 != null && indexFile2 == null) {
            result = new HashSet<>();
            mongoDB.createDatabaseOrUse(Parser.currentDatabaseName);
            Bson filter = getFilter(i.getOperator(), i.getRightSide());
            for (org.bson.Document j : mongoDB.getDocuments(indexFile1.getIndexName()).find(filter)) {
                System.out.println(j);
                result.addAll(Arrays.asList(((String) j.get("Value")).split("&")));
            }
        } else if (indexFile1 == null && indexFile2 != null) {
            result = new HashSet<>();
            mongoDB.createDatabaseOrUse(Parser.currentDatabaseName);
            Bson filter = getFilter(i.getOperator(), i.getRightSide());
            for (org.bson.Document j : mongoDB.getDocuments(indexFile2.getIndexName()).find(filter)) {
                System.out.println(j);
                result.addAll(Arrays.asList(((String) j.get("Value")).split("&")));
            }
        }
        System.out.println(result);
//        Set<Integer> set1 = new HashSet<Integer>(list1);
//        Set<Integer> set2 = new HashSet<Integer>(list2);
//        Set<Integer> intersection = new HashSet<Integer>(set1);
//        intersection.retainAll(set2);
//        List<Integer> result = new ArrayList<Integer>(intersection);
//        System.out.println(result);
        return result;
    }

    private Bson getFilter(String operator, String value) {
        Bson result = null;
        if (operator.equals("="))
            return eq("_id", value);
        else if (operator.equals(">"))
            return gt("_id", value);
        else if (operator.equals(">="))
            return gte("_id", value);
        else if (operator.equals("<"))
            return lt("_id", value);
        else if (operator.equals("<="))
            return lte("_id", value);
        else if (operator.equals("!="))
            return ne("_id", value);
        else
            return result;
    }
}
