package Backend.Commands;

import Backend.Databases.Databases;
import Backend.Parser;
import Backend.SaveLoadJSON.LoadJSON;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static Backend.SocketServer.Server.errorClient;

public class ValidateInsertData {
    public static boolean checkInsertData(String tableName, String[] column, String[] values) {
        Databases databases = LoadJSON.load("databases.json");
        if (databases == null) {
            System.out.println("JSON file doesn't exists!");
            errorClient.send("JSON file doesn't exists!");
            return false;
        }
        if (!databases.checkDatabaseExists(Parser.currentDatabaseName)) {
            System.out.println("Database doesn't exists this database!");
            errorClient.send("Database doesn't exists this database!");
            return false;
        }
        if (!databases.getDatabase(Parser.currentDatabaseName).checkTableExists(tableName)) {
            System.out.println("Table doesn't exists!");
            errorClient.send("Table doesn't exists!");
            return false;
        }
        if (!databases.getDatabase(Parser.currentDatabaseName).getTable(tableName).checkInsertColumn(column)) {
            System.out.println("Syntax error!");
            errorClient.send("Syntax error!");
            return false;
        }
        for (int i=0;i<values.length;i++) {
            if (!checkType(values[i], databases.getDatabase(Parser.currentDatabaseName).getTable(tableName).getAttribute(column[i]).getType())) {
                System.out.println("The " + values[i] + " type isn't correct!");
                errorClient.send("The " + values[i] + " type isn't correct!");
                return false;
            }
        }
        return true;
    }

    public static boolean checkType(String value, String recommendedType) {
        switch (recommendedType) {
            case "INT":
                Pattern pattern1 = Pattern.compile("[0-9]+");
                Matcher matcher1 = pattern1.matcher(value);
                return matcher1.matches();
            case "FLOAT":
                Pattern pattern2 = Pattern.compile("[0-9]+\\.?[0-9]*");
                Matcher matcher2 = pattern2.matcher(value);
                return matcher2.matches();
            case "BIT":
                Pattern pattern3 = Pattern.compile("[0-1]");
                Matcher matcher3 = pattern3.matcher(value);
                return matcher3.matches();
            case "DATE":
                //YYYY-MM-DD
                Pattern pattern4 = Pattern.compile("[0-9]{4}-[0-1][0-9]-[0-3][0-9]");
                Matcher matcher4 = pattern4.matcher(value);
                return matcher4.matches();
            case "DATETIME":
                //YYYY-MM-DD hh:mm:ss
                Pattern pattern5 = Pattern.compile("[0-9]{4}-[0-1][0-9]-[0-3][0-9] [0-2][0-9]:[0-6][0-9]:[0-6][0-9]");
                Matcher matcher5 = pattern5.matcher(value);
                return matcher5.matches();
            case "VARCHAR":
                return true;
            default:
                return false;
        }
    }
}
