package Backend.Commands;

public class Condition {
    private String leftSide;
    private String operator;
    private String rightSide;

    public Condition(String command) {
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
    }

    public Condition(String leftSide, String operator, String rightSide) {
        this.leftSide = leftSide;
        this.operator = operator;
        this.rightSide = rightSide;
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
