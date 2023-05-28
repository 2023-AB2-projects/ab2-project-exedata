package Backend.Commands.Select;

import Backend.Commands.Condition;
import Backend.Databases.Attribute;
import Backend.Databases.IndexFile;
import Backend.Databases.Table;
import Backend.Parser;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.*;

import static Backend.SocketServer.Server.databases;
import static Backend.SocketServer.Server.mongoDB;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Filters.ne;

public class Selection {
    private final SelectManager selectManager;

    public Selection(SelectManager selectManager) {
        this.selectManager = selectManager;
    }

    public List<String> processing(int whichTable) {
        //megnezni es vegig jarni a where-t, hogy van-e index az adott mezon
        List<Condition> restWhere = new ArrayList<>();
        Set<String> primaryKeySet = null;
        Set<String> tempPrimaryKeySet;
        mongoDB.createDatabaseOrUse(Parser.currentDatabaseName);
        for (Condition i : selectManager.getWhere()) {
            //if we have index file in condition
            if (Objects.equals(i.getLeftSideTableName(), selectManager.getFrom().get(whichTable)) ||
                    Objects.equals(i.getRightSideTableName(), selectManager.getFrom().get(whichTable))) {
                tempPrimaryKeySet = getPrimaryKeySetIfHaveIndexFile(i);
                if (tempPrimaryKeySet == null) {
                    restWhere.add(i);//not yet used
                } else {
                    if (primaryKeySet == null)
                        primaryKeySet = tempPrimaryKeySet;
                    else
                        primaryKeySet.retainAll(tempPrimaryKeySet);
                }
            }
        }
        List<String> result = new ArrayList<>();

        FindIterable<Document> documents;
        if (primaryKeySet != null) {
            Bson filter = Filters.in("_id", primaryKeySet);
            documents = mongoDB.getDocuments(selectManager.getFrom().get(whichTable)).find(filter);
        } else {
            documents = mongoDB.getDocuments(selectManager.getFrom().get(whichTable)).find();
        }
        for (Document j : documents) {
            if (conditionOfWhere(j, restWhere, selectManager.getFrom().get(whichTable))) {
                result.add(convertToString(j));
            }
        }
        return result;
    }

    private String convertToString(Document document) {
        String pk = (String) document.get("_id");
        String value = (String) document.get("Value");
        StringBuilder result = new StringBuilder(pk);
        result.append("#");
        for (String i : value.split("#")) {
            result.append(i).append("#");
        }
        result = new StringBuilder(result.substring(0, result.length() - 1));
        return result.toString();
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
        String leftColumnName = condition.getLeftSideAttributeName();
        String rightColumnName = condition.getRightSideAttributeName();

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
        IndexFile indexFile1 = null;
        IndexFile indexFile2 = null;
        if (i.getLeftSideTableName() != null)
            indexFile1 = databases.getDatabase(Parser.currentDatabaseName).getTable(i.getLeftSideTableName()).getIndexFileIfKnowTheAttributes(new String[]{i.getLeftSideAttributeName()});
        if (i.getRightSideTableName() != null)
            indexFile2 = databases.getDatabase(Parser.currentDatabaseName).getTable(i.getRightSideTableName()).getIndexFileIfKnowTheAttributes(new String[]{i.getRightSideAttributeName()});
        Set<String> result = null;
        if (indexFile1 != null && indexFile2 == null) {
            result = new HashSet<>();
            Bson filter = getFilter(i.getOperator(), i.getRightSideAttributeName());
            for (org.bson.Document j : mongoDB.getDocuments(indexFile1.getIndexName()).find(filter)) {
                result.addAll(Arrays.asList(((String) j.get("Value")).split("&")));
            }
        } else if (indexFile1 == null && indexFile2 != null) {
            result = new HashSet<>();
            Bson filter = getFilter(i.getOperator(), i.getLeftSideAttributeName());
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
