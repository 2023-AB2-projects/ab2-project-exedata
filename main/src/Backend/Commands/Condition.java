package Backend.Commands;

import Backend.Databases.Table;
import Backend.Parser;

import java.util.List;

import static Backend.SocketServer.Server.databases;

public class Condition {
    private String leftSide;
    private String operator;
    private String rightSide;
    private String leftSideWhichTable;
    private String rightSideWhichTable;

    public Condition(String command, List<String> from, List<String> fromAS) {
        String[] temp;
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
        this.leftSide = leftSide;
        this.operator = operator;
        this.rightSide = rightSide;
        whichTable(from, fromAS);
    }

    private void whichTable(List<String> from, List<String> fromAS) {
        leftSideWhichTable = null;
        rightSideWhichTable = null;
        if(leftSide.contains(".")){
            String 
        }
        for (int i = 0; i < from.size(); i++) {
            Table table= databases.getDatabase(Parser.currentDatabaseName).getTable(from.get(i));
            if(table.checkAttributeExists())
        }
    }

    public String getLeftSide() {
        return leftSide;
    }

    public void setLeftSide(String leftSide) {
        this.leftSide = leftSide;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getRightSide() {
        return rightSide;
    }

    public void setRightSide(String rightSide) {
        this.rightSide = rightSide;
    }
}
