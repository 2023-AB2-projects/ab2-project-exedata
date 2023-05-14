package Backend.Commands;

import Backend.Databases.Attribute;
import Backend.Databases.Databases;
import Backend.Databases.IndexFile;
import Backend.Databases.Table;
import Backend.MongoDBManagement.MongoDB;
import Backend.Parser;
import Backend.SaveLoadJSON.LoadJSON;
import Backend.SocketServer.ErrorClient;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.PrintWriter;
import java.util.*;

import static com.mongodb.client.model.Filters.*;

public class Select implements Command {
    private final String command;
    private Databases databases;
    private MongoDB mongoDB;
    private final PrintWriter writer;

    public Select(String command, PrintWriter w) {
        this.command = command;
        writer = w;
    }

    @Override
    public void performAction() throws ParserConfigurationException, TransformerException {
        //SELECT * FROM disciplines WHERE CreditNr > 4 AND DName=Databases1;
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
        System.out.println(result);
        List<String> projectionResult = projection(result, selectManager);
        sendData(projectionResult);
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

    private List<Document> processing(SelectManager selectManager) {
        //megnezni es vegig jarni a where-t, hogy van-e index az adott mezon
        List<Condition> restWhere = new ArrayList<>();
        Set<String> primaryKeySet = null;
        Set<String> tempPrimaryKeySet;
        mongoDB.createDatabaseOrUse(Parser.currentDatabaseName);
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
        List<Document> result = new ArrayList<>();
        if (primaryKeySet != null) {
            Bson filter = Filters.in("_id", primaryKeySet);
            /////Ez nem altalanos!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            for (Document j : mongoDB.getDocuments(selectManager.getFrom().get(0)).find(filter)) {
                if (conditionOfWhere(j, restWhere, selectManager.getFrom().get(0))) {
                    result.add(j);
                }
            }
        }
        return result;
    }

    private boolean conditionOfWhere(Document document, List<Condition> where, String tableName) {
        Table table = databases.getDatabase(Parser.currentDatabaseName).getTable(tableName);
        List<String> value = getValueOfDocument(document, table);
        for (Condition i : where) {
            if (!checkCondition(i, value, table))
                return false;
        }
        return true;
    }

    private boolean checkCondition(Condition condition, List<String> value, Table table) {
        String leftColumnName;
        String rightColumnName;
        if (condition.getLeftSide().contains(".")) {
            leftColumnName = condition.getLeftSide().split("\\.")[1];
        } else
            leftColumnName = condition.getLeftSide();
        if (condition.getRightSide().contains(".")) {
            rightColumnName = condition.getRightSide().split("\\.")[1];
        } else
            rightColumnName = condition.getRightSide();
        Attribute leftAttribute = table.getAttribute(leftColumnName);
        Attribute rightAttribute = table.getAttribute(rightColumnName);
        int leftIndex = table.getStructure().indexOf(leftAttribute);
        int rightIndex = table.getStructure().indexOf(rightAttribute);
        try {
            if (leftIndex >= 0 && rightIndex >= 0) {
                if (!Objects.equals(leftAttribute.getType(), rightAttribute.getType()))
                    return false;
                if (isNumeric(leftAttribute.getType())) {
                    return checkNumeric(Double.valueOf(value.get(leftIndex)),
                            Double.valueOf(value.get(rightIndex)), condition.getOperator());
                } else
                    return checkString(value.get(leftIndex), value.get(rightIndex), condition.getOperator());
            } else if (leftIndex >= 0) {
                if (isNumeric(leftAttribute.getType())) {
                    return checkNumeric(Double.valueOf(value.get(leftIndex)),
                            Double.valueOf(rightColumnName), condition.getOperator());
                } else
                    return checkString(value.get(leftIndex), rightColumnName, condition.getOperator());
            } else if (rightIndex >= 0) {
                if (isNumeric(rightAttribute.getType())) {
                    return checkNumeric(Double.valueOf(leftColumnName),
                            Double.valueOf(value.get(rightIndex)), condition.getOperator());
                } else
                    return checkString(leftColumnName, value.get(rightIndex), condition.getOperator());
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isNumeric(String value) {
        return value.equalsIgnoreCase("INT") ||
                value.equalsIgnoreCase("FLOAT") ||
                value.equalsIgnoreCase("BIT");
    }

    private boolean checkString(String value1, String value2, String operator) {
        return switch (operator) {
            case "=" -> Objects.equals(value1, value2);
            case "!=" -> !Objects.equals(value1, value2);
            default -> false;
        };
    }

    private boolean checkNumeric(Double value1, Double value2, String operator) {
        return switch (operator) {
            case "=" -> Objects.equals(value1, value2);
            case ">" -> value1 > value2;
            case ">=" -> value1 >= value2;
            case "<" -> value1 < value2;
            case "<=" -> value1 <= value2;
            case "!=" -> !Objects.equals(value1, value2);
            default -> false;
        };
    }

    private List<String> getValueOfDocument(Document document, Table table) {
        List<String> result = new ArrayList<>();
        String[] idParts = ((String) document.get("_id")).split("#");
        String[] valueParts = ((String) document.get("Value")).split("#");
        int idNext = 0;
        int valueNext = 0;
        for (Attribute i : table.getStructure()) {
            if (table.isPrimaryKey(i.getName()))
                result.add(idParts[idNext++]);
            else
                result.add(valueParts[valueNext++]);
        }
        return result;
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
        Set<String> result = null;
        if (indexFile1 != null && indexFile2 == null) {
            result = new HashSet<>();
            Bson filter = getFilter(i.getOperator(), i.getRightSide());
            for (org.bson.Document j : mongoDB.getDocuments(indexFile1.getIndexName()).find(filter)) {
                result.addAll(Arrays.asList(((String) j.get("Value")).split("&")));
            }
        } else if (indexFile1 == null && indexFile2 != null) {
            result = new HashSet<>();
            Bson filter = getFilter(i.getOperator(), i.getRightSide());
            for (org.bson.Document j : mongoDB.getDocuments(indexFile2.getIndexName()).find(filter)) {
                result.addAll(Arrays.asList(((String) j.get("Value")).split("&")));
            }
        }
        return result;
    }

    private Bson getFilter(String operator, String value) {
        return switch (operator) {
            case "=" -> eq("_id", value);
            case ">" -> gt("_id", value);
            case ">=" -> gte("_id", value);
            case "<" -> lt("_id", value);
            case "<=" -> lte("_id", value);
            case "!=" -> ne("_id", value);
            default -> null;
        };
    }
}
