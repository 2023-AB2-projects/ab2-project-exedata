package Backend;

import Backend.Commands.*;

import java.util.regex.Pattern;

public class Parser {
    // Create instances of the matched type
    // Perform action on the type
    // Add to the response
    private static final Pattern createDatabase = Pattern.compile("^\s*CREATE\sDATABASE\s[A-Za-z0-9_]+;?", Pattern.CASE_INSENSITIVE);
    private static final Pattern createTable = Pattern.compile("^\s*CREATE\s*TABLE\s*[A-Za-z0-9_]+\s*\\(.*\\);?", Pattern.CASE_INSENSITIVE);
    private static final Pattern dropTable = Pattern.compile("^\s*DROP\s*TABLE\s*[A-Za-z0-9_]+;?", Pattern.CASE_INSENSITIVE);
    private static final Pattern dropDatabase = Pattern.compile("^\s*DROP\s*DATABASE\s*[A-Za-z0-9_]+;?", Pattern.CASE_INSENSITIVE);
    private static final Pattern createIndex = Pattern.compile("^\s*CREATE\s*INDEX\s*[A-Za-z0-9_]+\s*ON\s[A-Za-z0-9_]+\s*\\(.*\\);?", Pattern.CASE_INSENSITIVE);
    private static final Pattern dropIndex = Pattern.compile("^\s*DROP\s*INDEX\s*[A-Za-z0-9_.]+\s*ON\s[A-Za-z0-9_]+;?", Pattern.CASE_INSENSITIVE);
    private static final Pattern use = Pattern.compile("^\s*USE\s*[A-Za-z0-9_]+;?", Pattern.CASE_INSENSITIVE);
    public static String currentDatabaseName;

    public static Command commandType(String command) {
        while (command.charAt(0) == ' ') {
            command = command.substring(1);
        }
        if (use.matcher(command).find()) {
            currentDatabaseName = command.split(" ")[1];
            if (currentDatabaseName.charAt(currentDatabaseName.length() - 1) == ';') {
                currentDatabaseName = currentDatabaseName.substring(0, command.length() - 1);
            }
            return null;
        } else if (createDatabase.matcher(command).find()) {
            return new CreateDatabase(command);
        } else if (createTable.matcher(command).find()) {
            return new CreateTable(command);
        } else if (dropDatabase.matcher(command).find()) {
            return new DropDatabase(command);
        } else if (dropTable.matcher(command).find()) {
            return new DropTable(command);
        } else if (createIndex.matcher(command).find()) {
            return new CreateIndex(command);
        } else if (dropIndex.matcher(command).find()) {
            return new DropIndex(command);
        }
        System.out.println("Wrong command!");
        return null;
    }
}
