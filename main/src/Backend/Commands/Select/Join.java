package Backend.Commands.Select;

import Backend.Commands.Condition;
import Backend.Databases.Attribute;
import Backend.Parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static Backend.SocketServer.Server.databases;

public class Join {
    private List<String> joinResult;

    public Join(SelectManager selectManager) {
        List<String> tableNames = selectManager.getFrom();
        Selection tableSelectionResultsSeparately = new Selection(selectManager);
        List<Condition> joinConditions = selectManager.getJoin();

        List<String> allAttributeNames = getAttributeNames(tableNames.get(0));
        joinResult = new ArrayList<>();
        joinResult.add("");
        joinResult.addAll(tableSelectionResultsSeparately.processing(0));

        for (int i = 1; i < tableNames.size(); i++) {
            List<String> subResult = tableSelectionResultsSeparately.processing(i);
            // attributeNames of a table (ex. indexArray.get(0) => [DiscID, DName, CreditNr])
            List<String> attributeNamesTableIndex = getAttributeNames(tableNames.get(i));
            ////check condition
            joinResult = joining(joinResult, allAttributeNames, subResult, attributeNamesTableIndex, joinConditions);
            allAttributeNames.addAll(attributeNamesTableIndex);
        }

        System.out.println(allAttributeNames);

        joinResult.set(0, attributeNamesTableIndexToString(allAttributeNames));
    }

    private List<String> joining(List<String> record1, List<String> attributeNames1, List<String> record2,
                                 List<String> attributeNames2, List<Condition> conditions) {
        List<String> result = new ArrayList<>();
        result.add("");
        String temp;
        for (int i = 1; i < record1.size(); i++) {
            for (int j = 0; j < record2.size(); j++) {
                temp = checkConditions(record1.get(i), attributeNames1, record2.get(j), attributeNames2, conditions);
                if (temp != null)
                    result.add(temp);
            }
        }
        return result;
    }

    private String checkConditions(String record1, List<String> attributeNames1, String record2,
                                   List<String> attributeNames2, List<Condition> conditions) {
        for (Condition i : conditions) {
            if (!check(record1, attributeNames1, record2, attributeNames2, i))
                return null;
        }
        return record1 + "#" + record2;
    }

    private boolean check(String record1, List<String> attributeNames1, String record2, List<String> attributeNames2, Condition c) {
        try {
            String attributeNameInCondition1 = c.getLeftSideTableName() + "." + c.getLeftSideAttributeName();
            String attributeNameInCondition2 = c.getRightSideTableName() + "." + c.getRightSideAttributeName();
            boolean numeric = isNumeric(databases.getDatabase(Parser.currentDatabaseName).getTable(c.getLeftSideTableName())
                    .getAttribute(c.getLeftSideAttributeName()).getType());
            String value1;
            String value2;
            if (attributeNames1.contains(attributeNameInCondition1) &&
                    attributeNames2.contains(attributeNameInCondition2)) {
                value1 = getAttributeValueByName(record1, attributeNameInCondition1, attributeNames1);
                value2 = getAttributeValueByName(record2, attributeNameInCondition2, attributeNames2);
                if (numeric)
                    return checkNumeric(Double.valueOf(value1), Double.valueOf(value2), c.getOperator());
                else
                    return checkString(value1, value2, c.getOperator());
            } else if (attributeNames1.contains(attributeNameInCondition2) &&
                    attributeNames2.contains(attributeNameInCondition1)) {
                value2 = getAttributeValueByName(record1, attributeNameInCondition1, attributeNames1);
                value1 = getAttributeValueByName(record2, attributeNameInCondition2, attributeNames2);
                if (numeric)
                    return checkNumeric(Double.valueOf(value1), Double.valueOf(value2), c.getOperator());
                else
                    return checkString(value1, value2, c.getOperator());
            }
        } catch (Exception e) {
            return false;
        }
        return true;
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

    private String attributeNamesTableIndexToString(List<String> list) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String i : list) {
            stringBuilder.append(i).append("#");
        }
        return (stringBuilder.substring(0, stringBuilder.length() - 1));
    }

    private String getAttributeValueByName(String record, String attributeName, List<String> attributeNamesTableIndex) {
        return record.split("#")[attributeNamesTableIndex.indexOf(attributeName)];
    }

    private List<String> getAttributeNames(String tableName) {
        List<String> pk = databases.getDatabase(Parser.currentDatabaseName).getTable(tableName).getPrimaryKey();
        List<String> result = new ArrayList<>();
        for (String i : pk) {
            result.add(tableName + "." + i);
        }
        for (Attribute i : databases.getDatabase(Parser.currentDatabaseName).getTable(tableName).getStructure()) {
            if (!pk.contains(i.getName()))
                result.add(tableName + "." + i.getName());
        }
        return result;
    }

    public List<String> getJoinResult() {
        return joinResult;
    }
}
