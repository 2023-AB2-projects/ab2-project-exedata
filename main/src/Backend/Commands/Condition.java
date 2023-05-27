package Backend.Commands;

import Backend.Databases.Table;
import Backend.Parser;

import java.util.List;

import static Backend.SocketServer.Server.databases;

public class Condition {
    private String leftSide;
    private String operator;
    private String rightSide;
    private String leftSideTableName;
    private String leftSideAttributeName;
    private String rightSideTableName;
    private String rightSideAttributeName;
    private String errorMassage;

    public Condition(String command, List<String> from, List<String> fromAS) {
        String[] temp;
        errorMassage = null;
        command = command.replaceAll("\\s*", "");
//        >, >=, <, <=, =, !=
        if (command.contains(">=")) {
            operator = ">=";
            temp = command.split(">=");
            leftSide = temp[0];
            rightSide = temp[1];
        } else if (command.contains(">")) {
            operator = ">";
            temp = command.split(">");
            leftSide = temp[0];
            rightSide = temp[1];
        } else if (command.contains("<=")) {
            operator = "<=";
            temp = command.split("<=");
            leftSide = temp[0];
            rightSide = temp[1];
        } else if (command.contains("<")) {
            operator = "<";
            temp = command.split("<");
            leftSide = temp[0];
            rightSide = temp[1];
        } else if (command.contains("!=")) {
            operator = "!=";
            temp = command.split("!=");
            leftSide = temp[0];
            rightSide = temp[1];
        } else if (command.contains("=")) {
            operator = "=";
            temp = command.split("=");
            leftSide = temp[0];
            rightSide = temp[1];
        }
        whichTable(from, fromAS);
    }

    public Condition(String leftSide, String operator, String rightSide, List<String> from, List<String> fromAS) {
        errorMassage = null;
        this.leftSide = leftSide;
        this.operator = operator;
        this.rightSide = rightSide;
        whichTable(from, fromAS);
    }

    private void whichTable(List<String> from, List<String> fromAS) {
        leftSideTableName = null;
        rightSideTableName = null;
        leftSideAttributeName = leftSide;
        rightSideAttributeName = rightSide;
        checkPointReferences(from, fromAS);
        if (!checkTableAndAttributeExists(from))
            return;
        if (leftSideTableName == null && rightSideTableName == null) {
            int howManyLeftAttribute = 0;
            int howManyRightAttribute = 0;
            for (String i : from) {
                Table table = databases.getDatabase(Parser.currentDatabaseName).getTable(i);
                if (table.checkAttributeExists(leftSideAttributeName)) {
                    howManyLeftAttribute++;
                    leftSideTableName = i;
                }
                if (table.checkAttributeExists(rightSideAttributeName)) {
                    howManyRightAttribute++;
                    rightSideTableName = i;
                }
            }
            if (howManyLeftAttribute > 1)
                errorMassage = "The attribute " + leftSideAttributeName + " is already exists in two different table!";
            else if (leftSideTableName == null && howManyLeftAttribute < 1)
                errorMassage = "The attribute " + leftSideAttributeName + " doesn't exists!";
            if (howManyRightAttribute > 1)
                errorMassage = "The attribute " + rightSideAttributeName + " is already exists in two different table!";
            else if (rightSideTableName == null && howManyRightAttribute < 1)
                errorMassage = "The attribute " + rightSideAttributeName + " doesn't exists!";
        }
        if (leftSideTableName == null && rightSideTableName == null)
            errorMassage = "Error in the condition: " + leftSide + " " + operator + " " + rightSide;
    }

    private boolean checkTableAndAttributeExists(List<String> from) {
        if (leftSideTableName != null) {
            if (!databases.getDatabase(Parser.currentDatabaseName).checkTableExists(leftSideTableName) ||
                    !databases.getDatabase(Parser.currentDatabaseName)
                            .getTable(leftSideTableName).checkAttributeExists(leftSideAttributeName)) {
                errorMassage = "Table: " + leftSideTableName + " or attribute in table: " + leftSideAttributeName + " doesn't exists!";
                return false;
            }
            if (!from.contains(leftSideTableName)) {
                errorMassage = "The from doesn't contains " + leftSideTableName + " table!";
                return false;
            }
        }
        if (rightSideTableName != null) {
            if (!databases.getDatabase(Parser.currentDatabaseName).checkTableExists(rightSideTableName) ||
                    !databases.getDatabase(Parser.currentDatabaseName)
                            .getTable(rightSideTableName).checkAttributeExists(rightSideAttributeName)) {
                errorMassage = "Table: " + rightSideTableName + " or attribute in table: " + rightSideAttributeName + " doesn't exists!";
                return false;
            }
            if (!from.contains(rightSideTableName)) {
                errorMassage = "The from doesn't contains " + rightSideTableName + " table!";
                return false;
            }
        }
        return true;
    }

    private void checkPointReferences(List<String> from, List<String> fromAS) {
        if (leftSide.contains(".")) {
            leftSideTableName = leftSide.split("\\.")[0];
            int index = fromAS.indexOf(leftSideTableName);
            if (index >= 0)
                leftSideTableName = from.get(index);
            leftSideAttributeName = leftSide.split("\\.")[1];
        }
        if (rightSide.contains(".")) {
            rightSideTableName = rightSide.split("\\.")[0];
            int index = fromAS.indexOf(rightSideTableName);
            if (index >= 0)
                rightSideTableName = from.get(index);
            rightSideAttributeName = rightSide.split("\\.")[1];
        }
    }

    public String getOperator() {
        return operator;
    }

    public String getErrorMassage() {
        return errorMassage;
    }

    public String getLeftSideTableName() {
        return leftSideTableName;
    }

    public String getLeftSideAttributeName() {
        return leftSideAttributeName;
    }

    public String getRightSideTableName() {
        return rightSideTableName;
    }

    public String getRightSideAttributeName() {
        return rightSideAttributeName;
    }
}
