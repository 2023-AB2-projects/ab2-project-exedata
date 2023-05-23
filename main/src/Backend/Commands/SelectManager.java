package Backend.Commands;

import Backend.Databases.Attribute;
import Backend.Databases.Databases;
import Backend.Databases.Table;
import Backend.Parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SelectManager {
    private String command;
    private final Databases databases;

    private List<String> select;
    private List<String> selectAS;
    private List<String> function;
    private List<Integer> functionIndexInSelect;
    private List<String> from;

    private List<Condition> join;
    private List<String> fromAS;
    private List<Condition> where;

    public SelectManager(String command, Databases databases) {
        this.command = command;
        this.databases = databases;
        //separate select parts
        separate();
        //replace alias to tableName
        replaceAlias();
        //SELECT
        //FROM
        //WHERE
        //GROUP BY
        //HAVING
        //ORDER BY
    }

    public String check() {
        for (String i : from) {
            if (!databases.getDatabase(Parser.currentDatabaseName).checkTableExists(i)) {
                return "Table " + i + " doesn't exists!";
            }
        }
        String temp;
        String temp2;
        for (String i : select) {
            temp = existsInDatabase(i);
            if (temp != null)
                return temp;
        }
        for (Condition i : where) {
            temp = existsInDatabase(i.getLeftSide());
            temp2 = existsInDatabase(i.getRightSide());
            if (temp != null && temp2 != null) {
                return temp;
            }
        }
        return null;
    }

    private String existsInDatabase(String field) {
        if (field.contains(".")) {
            String[] temp = field.split("\\.");
            if (!databases.getDatabase(Parser.currentDatabaseName).checkTableExists(temp[0]))
                return "Table " + temp[0] + " doesn't exists!";
            if (!databases.getDatabase(Parser.currentDatabaseName).getTable(temp[0]).checkAttributeExists(temp[1]))
                return "Table " + temp[0] + " attribute " + temp[1] + " doesn't exists!";
        } else {
            boolean exists = false;
            for (Table j : databases.getDatabase(Parser.currentDatabaseName).getTables()) {
                if (j.checkAttributeExists(field))
                    exists = true;
            }
            if (!exists)
                return "The " + field + " doesn't exists!";
        }
        return null;
    }

    private void replaceAlias() {
        int index;
        String[] temp;
        ///select
        for (int i = 0; i < select.size(); i++) {
            if (select.get(i).contains(".")) {
                temp = select.get(i).split("\\.");
                index = fromAS.indexOf(temp[0]);
                if (index >= 0) {
                    select.set(i, from.get(index) + "." + temp[1]);
                }
            }
        }
        //where
        String left;
        String right;
        for (int i = 0; i < where.size(); i++) {
            left = where.get(i).getLeftSide();
            right = where.get(i).getRightSide();
            if (left.contains(".")) {
                temp = left.split("\\.");
                index = fromAS.indexOf(temp[0]);
                if (index >= 0) {
                    left = from.get(index) + "." + temp[1];
                }
            }
            if (right.contains(".")) {
                temp = right.split("\\.");
                index = fromAS.indexOf(temp[0]);
                if (index >= 0) {
                    right = from.get(index) + "." + temp[1];
                }
            }
            where.set(i, new Condition(left, where.get(i).getOperator(), right, from, fromAS));
        }
    }

    private void separate() {
        if (command.charAt(command.length() - 1) == ';') {
            command = command.substring(0, command.length() - 1);
        }
        //select min(a.korte), max(a.alma) as as, aasc from alma a inner join korte k on a.asd=k.sa

        String selectPart = selectPartParser();
        boolean hasStar = Objects.equals(selectPart, "*");
        String fromPart = fromPartParser();
        fromSeparate(fromPart);
        if (hasStar) {
            replaceStar();
        }else{
            selectSeparate(selectPart);
        }
        String wherePart = wherePartParser();
        whereSeparate(wherePart);

        String groupByPart = groupByPartParser();

        String havingPart = havingPartParser();

        String orderByPart = orderByPartParser();

        replaceStarInSelectAfterThePoint();
    }

    private void replaceStarInSelectAfterThePoint() {
        List<String> temp = new ArrayList<>();
        List<String> tempAS = new ArrayList<>();
        String tableName;
        for (int i = 0; i < select.size(); i++) {
            if (select.get(i).contains(".*")) {
                tableName = select.get(i).split("\\.")[0];
                Table table = databases.getDatabase(Parser.currentDatabaseName).getTable(tableName);
                if (table != null) {
                    List<Attribute> attributeList = table.getStructure();
                    for (Attribute j : attributeList) {
                        temp.add(table.getName() + "." + j.getName());
                        tempAS.add(null);
                    }
                }
            } else {
                temp.add(select.get(i));
                tempAS.add(selectAS.get(i));
            }
        }
        select = temp;
        selectAS = tempAS;
    }

    private void replaceStar() {
        select = new ArrayList<>();
        selectAS = new ArrayList<>();
        function=new ArrayList<>();
        functionIndexInSelect=new ArrayList<>();
        for (String i : from) {
            Table table = databases.getDatabase(Parser.currentDatabaseName).getTable(i);
            if (table != null) {
                List<Attribute> attributeList = table.getStructure();
                for (Attribute j : attributeList) {
                    select.add(table.getName() + "." + j.getName());
                    selectAS.add(null);
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
                Condition condition=new Condition(i,from,fromAS);
//                if(condition.getRightSide()==)
                where.add(condition);

            }
        }
    }

    private void fromSeparate(String fromPart) {
        fromPart = fromPart.replaceAll(",\\s*", ",");
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
                from.add(words[0]);
                if (words.length == 2)
                    fromAS.add(words[1]);
                else
                    fromAS.add(null);
                join.add(new Condition(parts[1],from,fromAS));
            } else {
                words = parts[0].split(" ");
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
        function = new ArrayList<>();
        functionIndexInSelect = new ArrayList<>();
        selectPart = selectPart.replaceAll(",\\s*", ",");
        Pattern pattern = Pattern.compile("\\s*(.+)\\s+AS\\s+(.+)\\s*", Pattern.CASE_INSENSITIVE);
        Matcher matcher;
        Pattern patternFunction = Pattern.compile("\\(.+\\)");
        Matcher matcherFunction;
        for (String i : selectPart.split(",")) {
            matcher = pattern.matcher(i);
            if (matcher.find()) {
                select.add(matcher.group(1));
                selectAS.add(matcher.group(2));
            } else {
                select.add(i);
                selectAS.add(null);
            }
            matcherFunction = patternFunction.matcher(select.get(select.size() - 1));
            if (matcherFunction.find()) {
                function.add(select.get(select.size() - 1));
                functionIndexInSelect.add(select.size() - 1);
            }
        }
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
                groupByPart = command;
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

    public List<String> getFrom() {
        return from;
    }

    public List<String> getFromAS() {
        return fromAS;
    }

    public List<Condition> getWhere() {
        return where;
    }
}
