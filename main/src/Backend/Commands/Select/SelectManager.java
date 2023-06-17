package Backend.Commands.Select;

import Backend.Commands.Condition;
import Backend.Databases.Attribute;
import Backend.Databases.Databases;
import Backend.Databases.Table;
import Backend.Parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SelectManager {
    private String command;
    private final Databases databases;
    private List<String> select;
    private List<String> selectAS;
    private List<String> tableNameOfSelectAttribute;
    private List<String> function;
    private List<Integer> functionIndexInSelect;
    private List<String> from;

    private List<Condition> join;
    private List<String> fromAS;
    private List<Condition> where;
    private List<String> groupBy;
    private List<String> tableNameOfGroupByAttribute;
    private String errorMassage;

    public SelectManager(String command, Databases databases) {
        errorMassage = null;
        this.command = command;
        this.databases = databases;
    }

    public String processing() {
        //separate select parts
        separate();
        if (errorMassage != null)
            return errorMassage;
        //SELECT
        //FROM
        //WHERE
        //GROUP BY
        //HAVING
        //ORDER BY
        return null;
    }

    private void separate() {
        if (command.charAt(command.length() - 1) == ';') {
            command = command.substring(0, command.length() - 1);
        }

        String selectPart = selectPartParser();
        boolean hasStar = Objects.equals(selectPart, "*");

        String fromPart = fromPartParser();
        fromSeparate(fromPart);
        if (errorMassage != null)
            return;

        if (hasStar) {
            replaceStar();
        } else {
            selectSeparate(selectPart);
            replaceStarInSelectAfterThePoint();
        }
        if (errorMassage != null)
            return;

        String wherePart = wherePartParser();
        whereSeparate(wherePart);
        if (errorMassage != null)
            return;

        String groupByPart = groupByPartParser();
        groupBySeparate(groupByPart);
        if (errorMassage != null)
            return;

        String havingPart = havingPartParser();

        String orderByPart = orderByPartParser();
    }

    private void replaceStarInSelectAfterThePoint() {
        List<String> temp = new ArrayList<>();
        List<String> tempAS = new ArrayList<>();
        List<String> tempTableName = new ArrayList<>();
        String tableName;
        for (int i = 0; i < select.size(); i++) {
            if (select.get(i).contains(".*")) {
                tableName = select.get(i).split("\\.")[0];
                List<Attribute> attributeList = databases.getDatabase(Parser.currentDatabaseName).getTable(tableName).getStructure();
                for (Attribute j : attributeList) {
                    temp.add(j.getName());
                    tempAS.add(null);
                    tempTableName.add(tableName);
                }
            } else {
                temp.add(select.get(i));
                tempAS.add(selectAS.get(i));
                tempTableName.add(tableNameOfSelectAttribute.get(i));
            }
        }
        select = temp;
        selectAS = tempAS;
        tableNameOfSelectAttribute = tempTableName;
    }

    private void replaceStar() {
        select = new ArrayList<>();
        selectAS = new ArrayList<>();
        tableNameOfSelectAttribute = new ArrayList<>();
        function = new ArrayList<>();
        functionIndexInSelect = new ArrayList<>();
        for (String i : from) {
            Table table = databases.getDatabase(Parser.currentDatabaseName).getTable(i);
            List<Attribute> attributeList = table.getStructure();
            for (Attribute j : attributeList) {
                select.add(j.getName());
                selectAS.add(null);
                tableNameOfSelectAttribute.add(table.getName());
            }
        }
    }

    private void groupBySeparate(String groupByPart) {
        groupBy = new ArrayList<>();
        tableNameOfGroupByAttribute = new ArrayList<>();
        String tableName;
        if (!Objects.equals(groupByPart, "")) {
            groupByPart = groupByPart.replaceAll("\\s*,\\s*", ",");
            for (String i : groupByPart.split(",")) {
                tableName = checkSelectAttributeIsExists(i);
                if (tableName == null) {
                    return;
                }
                if (i.contains("."))
                    groupBy.add(i.split("\\.")[1]);
                else
                    groupBy.add(i);
                tableNameOfGroupByAttribute.add(tableName);
            }
            for (String i : select) {
                if (!groupBy.contains(i) && !i.contains(")")) {
                    errorMassage = "The " + i + " doesn't contains the GROUP BY clause";
                    return;
                }
            }
        }
    }

    private void whereSeparate(String wherePart) {
        where = new ArrayList<>();
        if (!Objects.equals(wherePart, "")) {
            wherePart = wherePart.replaceAll("\\s+(?i)AND\\s+", " AND ");
            for (String i : wherePart.split("\\s+(?i)AND\\s+")) {
                if (i.charAt(0) == '(') {
                    i = i.substring(1);
                }
                if (i.charAt(i.length() - 1) == ')') {
                    i = i.substring(0, i.length() - 1);
                }
                Condition condition = new Condition(i, from, fromAS);
                if (condition.getErrorMassage() != null) {
                    errorMassage = condition.getErrorMassage();
                    return;
                }
                if (condition.getLeftSideTableName() != null && condition.getRightSideTableName() != null &&
                        !Objects.equals(condition.getLeftSideTableName(), condition.getRightSideTableName()))
                    join.add(condition);
                else
                    where.add(condition);
            }
        }
    }

    private void fromSeparate(String fromPart) {
        fromPart = fromPart.replaceAll("\\s*,\\s*", ",");
        fromPart = fromPart.replaceAll("\\s+", " ");
        String[] fromPartSeparateByJoin = fromPart.split("(?i) INNER JOIN ");
        from = new ArrayList<>();
        fromAS = new ArrayList<>();
        join = new ArrayList<>();
        String[] parts;
        String[] words;

        for (String i : fromPartSeparateByJoin) {
            parts = i.split("(?i) ON ");
            if (parts.length == 2) {
                words = parts[0].split(" ");
                if (!databases.getDatabase(Parser.currentDatabaseName).checkTableExists(words[0])) {
                    errorMassage = "The table: " + words[0] + " doesn't exists!";
                    return;
                }
                from.add(words[0]);

                if (words.length == 2)
                    fromAS.add(words[1]);
                else
                    fromAS.add(null);

                Condition condition = new Condition(parts[1], from, fromAS);
                if (condition.getErrorMassage() != null) {
                    errorMassage = condition.getErrorMassage();
                    return;
                }
                join.add(condition);

            } else {
                words = parts[0].split(" ");
                if (!databases.getDatabase(Parser.currentDatabaseName).checkTableExists(words[0])) {
                    errorMassage = "The table: " + words[0] + " doesn't exists!";
                    return;
                }
                from.add(words[0]);
                if (words.length == 2)
                    fromAS.add(words[1]);
                else
                    fromAS.add(null);
            }
        }
    }

    private void selectSeparate(String selectPart) {
        select = new ArrayList<>();
        selectAS = new ArrayList<>();
        tableNameOfSelectAttribute = new ArrayList<>();
        function = new ArrayList<>();
        functionIndexInSelect = new ArrayList<>();
        String tableName;
        selectPart = selectPart.replaceAll("\\s*,\\s*", ",");
        Pattern pattern = Pattern.compile("\\s*(.+)\\s+AS\\s+(.+)\\s*", Pattern.CASE_INSENSITIVE);
        Matcher matcher;
        Pattern patternFunction = Pattern.compile("\\(.+\\)");
        Matcher matcherFunction;
        for (String i : selectPart.split(",")) {
            if (!i.contains(".*")) {
                matcher = pattern.matcher(i);
                if (matcher.find()) {
                    tableName = checkSelectAttributeIsExists(matcher.group(1));
                    if (tableName == null) {
                        if (!i.contains("*"))
                            return;
                        else {
                            Pattern pattern1 = Pattern.compile("\\s*COUNT\\(\\*\\)\\s*", Pattern.CASE_INSENSITIVE);
                            Matcher matcher1 = pattern1.matcher(matcher.group(1));
                            if (matcher1.find())
                                errorMassage = null;
                            else
                                errorMassage = "Don't use * in " + matcher.group(1);
                        }
                    }
                    if (matcher.group(1).contains(".") && !i.contains(")"))
                        select.add(matcher.group(1).split("\\.")[1]);
                    else
                        select.add(matcher.group(1));
                    selectAS.add(matcher.group(2));
                    tableNameOfSelectAttribute.add(tableName);
                } else {
                    tableName = checkSelectAttributeIsExists(i);
                    if (tableName == null) {
                        if (!i.contains("*"))
                            return;
                        else {
                            Pattern pattern1 = Pattern.compile("\\s*COUNT\\(\\*\\)\\s*", Pattern.CASE_INSENSITIVE);
                            Matcher matcher1 = pattern1.matcher(i);
                            if (matcher1.find())
                                errorMassage = null;
                            else
                                errorMassage = "Don't use * in " + i;
                        }
                    }

                    if (i.contains(".") && !i.contains(")"))
                        select.add(i.split("\\.")[1]);
                    else
                        select.add(i);
                    selectAS.add(null);
                    tableNameOfSelectAttribute.add(tableName);
                }
                matcherFunction = patternFunction.matcher(select.get(select.size() - 1));
                if (matcherFunction.find()) {
                    function.add(select.get(select.size() - 1));
                    functionIndexInSelect.add(select.size() - 1);
                }
            } else {
                select.add(i);
                selectAS.add(null);
                tableNameOfSelectAttribute.add((null));
            }
        }
    }

    private String checkSelectAttributeIsExists(String selectPart) {
        Pattern pattern = Pattern.compile("\\s*\\((.+)\\)\\s*");
        Matcher matcher = pattern.matcher(selectPart);
        String tableName = null;
        if (matcher.find()) {
            selectPart = matcher.group(1);
        }
        if (selectPart.contains(".")) {
            tableName = selectPart.split("\\.")[0];
            String attributeName = selectPart.split("\\.")[1];
            int index = fromAS.indexOf(tableName);
            if (index >= 0)
                tableName = from.get(index);
            if (!databases.getDatabase(Parser.currentDatabaseName).checkTableExists(tableName)) {
                errorMassage = "The table: " + tableName + " doesn't exists!";
                return null;
            }
            if (!from.contains(tableName)) {
                errorMassage = "The table: " + tableName + " doesn't exists in from!";
                return null;
            }
            if (!databases.getDatabase(Parser.currentDatabaseName).getTable(tableName).checkAttributeExists(attributeName)) {
                errorMassage = "The attribute: " + attributeName + " doesn't exists!";
                return null;
            }
        } else {
            int howManyAttribute = 0;
            for (String i : from) {
                Table table = databases.getDatabase(Parser.currentDatabaseName).getTable(i);
                if (table.checkAttributeExists(selectPart)) {
                    howManyAttribute++;
                    tableName = table.getName();
                }
            }
            if (howManyAttribute > 1)
                errorMassage = "The attribute " + selectPart + " is already exists in two different table!";
            else if (howManyAttribute < 1)
                errorMassage = "The attribute " + selectPart + " doesn't exists!";
        }
        return tableName;
    }

    private String selectPartParser() {
        String selectPart = "";
        Pattern selectPattern = Pattern.compile("^SELECT\\s+(.+)\\s+FROM\\s+", Pattern.CASE_INSENSITIVE);
        Matcher selectMatcher = selectPattern.matcher(command);
        if (selectMatcher.find()) {
            selectPart = selectMatcher.group(1);
            command = " " + command.substring(selectMatcher.end());
        }
        return selectPart;
    }

    private String fromPartParser() {
        String fromPart;
        Pattern fromPattern = Pattern.compile("^\\s+(.+)\\s+WHERE\\s+", Pattern.CASE_INSENSITIVE);
        Matcher fromMatcher = fromPattern.matcher(command);
        if (fromMatcher.find()) {
            fromPart = fromMatcher.group(1);
            command = "WHERE " + command.substring(fromMatcher.end());
        } else {
            fromPattern = Pattern.compile("^\\s+(.+)\\s+GROUP BY\\s+", Pattern.CASE_INSENSITIVE);
            fromMatcher = fromPattern.matcher(command);
            if (fromMatcher.find()) {
                fromPart = fromMatcher.group(1);
                command = "GROUP BY " + command.substring(fromMatcher.end());
            } else {
                fromPattern = Pattern.compile("^\\s+(.+)\\s+ORDER BY\\s+", Pattern.CASE_INSENSITIVE);
                fromMatcher = fromPattern.matcher(command);
                if (fromMatcher.find()) {
                    fromPart = fromMatcher.group(1);
                    command = "ORDER BY " + command.substring(fromMatcher.end());
                } else {
                    fromPart = command.substring(1);
                    command = "";
                }
            }
        }
        return fromPart;
    }

    private String wherePartParser() {
        String wherePart;
        Pattern wherePattern = Pattern.compile("^WHERE\\s+(.+)\\s+GROUP BY\\s+", Pattern.CASE_INSENSITIVE);
        Matcher whereMatcher = wherePattern.matcher(command);
        if (whereMatcher.find()) {
            wherePart = whereMatcher.group(1);
            command = "GROUP BY " + command.substring(whereMatcher.end());
        } else {
            wherePattern = Pattern.compile("^WHERE\\s+(.+)\\s+ORDER BY\\s+", Pattern.CASE_INSENSITIVE);
            whereMatcher = wherePattern.matcher(command);
            if (whereMatcher.find()) {
                wherePart = whereMatcher.group(1);
                command = "ORDER BY " + command.substring(whereMatcher.end());
            } else {
                wherePattern = Pattern.compile("^WHERE\\s+(.+)\\s*", Pattern.CASE_INSENSITIVE);
                whereMatcher = wherePattern.matcher(command);
                if (whereMatcher.find()) {
                    wherePart = whereMatcher.group(1);
                } else {
                    wherePart = "";
                }
            }
        }
        return wherePart;
    }

    private String groupByPartParser() {
        String groupByPart;
        Pattern groupByPattern = Pattern.compile("^GROUP BY\\s+(.+)\\s+HAVING\\s+", Pattern.CASE_INSENSITIVE);
        Matcher groupByMatcher = groupByPattern.matcher(command);
        if (groupByMatcher.find()) {
            groupByPart = groupByMatcher.group(1);
            command = "HAVING " + command.substring(groupByMatcher.end());
        } else {
            groupByPattern = Pattern.compile("^GROUP BY\\s+(.+)\\s+ORDER BY\\s+", Pattern.CASE_INSENSITIVE);
            groupByMatcher = groupByPattern.matcher(command);
            if (groupByMatcher.find()) {
                groupByPart = groupByMatcher.group(1);
                command = "ORDER BY " + command.substring(groupByMatcher.end());
            } else {
                groupByPattern = Pattern.compile("^GROUP BY\\s+(.+)\\s*", Pattern.CASE_INSENSITIVE);
                groupByMatcher = groupByPattern.matcher(command);
                if (groupByMatcher.find())
                    groupByPart = groupByMatcher.group(1);
                else
                    groupByPart = "";
            }
        }
        return groupByPart;
    }

    private String havingPartParser() {
        String havingPart = "";
        Pattern havingPattern = Pattern.compile("^HAVING\\s+(.+)\\s+ORDER BY\\s+", Pattern.CASE_INSENSITIVE);
        Matcher havingMatcher = havingPattern.matcher(command);
        if (havingMatcher.find()) {
            havingPart = havingMatcher.group(1);
            command = "ORDER BY " + command.substring(havingMatcher.end());
        }
        return havingPart;
    }

    private String orderByPartParser() {
        String orderByPart;
        Pattern orderByPattern = Pattern.compile("^ORDER BY\\s+(.+)", Pattern.CASE_INSENSITIVE);
        Matcher orderByMatcher = orderByPattern.matcher(command);
        if (orderByMatcher.find()) {
            orderByPart = orderByMatcher.group(1);
            command = command.substring(orderByMatcher.end());
        } else {
            orderByPart = command;
        }
        return orderByPart;
    }

    public List<String> getSelect() {
        return select;
    }

    public List<String> getSelectAS() {
        return selectAS;
    }

    public List<String> getTableNameOfSelectAttribute() {
        return tableNameOfSelectAttribute;
    }

    public List<String> getFunction() {
        return function;
    }

    public List<Integer> getFunctionIndexInSelect() {
        return functionIndexInSelect;
    }

    public List<String> getFrom() {
        return from;
    }

    public List<Condition> getJoin() {
        return join;
    }

    public List<String> getFromAS() {
        return fromAS;
    }

    public List<Condition> getWhere() {
        return where;
    }

    public List<String> getGroupBy() {
        return groupBy;
    }

    public List<String> getTableNameOfGroupByAttribute() {
        return tableNameOfGroupByAttribute;
    }

    public String getErrorMassage() {
        return errorMassage;
    }
}
