package Backend.Commands;

import Backend.Databases.Databases;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SelectManager {
    private String command;
    private final Databases databases;

    private List<String> select;
    private List<String> selectAS;
    private List<String> from;
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
//        SELECT students.StudID,specialization.SpecID
//        FROM specialization INNER JOIN groups ON specialization.SpecID=groups.SpecID
//        INNER JOIN students ON groups.GroupId=students.GroupId
//        WHERE (students.StudID>0)
//        ORDER BY students.GroupId DESC,students.StudID
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
        System.out.println(select);
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
            where.set(i,new Condition(left,where.get(i).getOperator(),right));
        }
    }

    private void separate() {
        if (command.charAt(command.length() - 1) == ';') {
            command = command.substring(0, command.length() - 1);
        }

        String selectPart = selectPartParser();
        selectSeparate(selectPart);
        String fromPart = fromPartParser();
        fromSeparate(fromPart);
        String wherePart = wherePartParser();
        whereSeparate(wherePart);

        String groupByPart = groupByPartParser();

        String havingPart = havingPartParser();

        String orderByPart = orderByPartParser();

    }

    private void whereSeparate(String wherePart) {
        where = new ArrayList<>();
        wherePart = wherePart.replaceAll("\\s+(?i)AND\\s+", " AND ");
        for (String i : wherePart.split("\\s+(?i)AND\\s+")) {
            where.add(new Condition(i));
        }
    }

    private void fromSeparate(String fromPart) {
        fromPart = fromPart.replaceAll(",\\s*", ",");
        from = new ArrayList<>();
        fromAS = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\s*(.+)\\s+AS\\s+(.+)\\s*", Pattern.CASE_INSENSITIVE);
        Matcher matcher;
        matcher = pattern.matcher(fromPart);
        if (matcher.find()) {
            from.add(matcher.group(1));
            fromAS.add(matcher.group(2));
        } else {
            from.add(fromPart);
            fromAS.add(null);
        }
    }

    private void selectSeparate(String selectPart) {
        selectPart = selectPart.replaceAll(",\\s*", ",");
        select = new ArrayList<>();
        selectAS = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\s*(.+)\\s+AS\\s+(.+)\\s*", Pattern.CASE_INSENSITIVE);
        Matcher matcher;
        for (String i : selectPart.split(",")) {
            matcher = pattern.matcher(i);
            if (matcher.find()) {
                select.add(matcher.group(1));
                selectAS.add(matcher.group(2));
            } else {
                select.add(i);
                selectAS.add(null);
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
                    fromPart = command;
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
}
