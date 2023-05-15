package Backend;

import Backend.Commands.*;
import Backend.Databases.Databases;
import Backend.SocketServer.ErrorClient;
import java.io.PrintWriter;
import java.util.regex.Pattern;
import static Backend.Commands.FormatCommand.formatCommand;
import static Backend.SocketServer.Server.databases;

public class Parser {
    // Create instances of the matched type
    // Perform action on the type
    // Add to the response
    private static final Pattern createDatabase = Pattern.compile("^\\s*CREATE\\s+DATABASE\\s+[A-Za-z0-9_]+;?", Pattern.CASE_INSENSITIVE);
    private static final Pattern createTable = Pattern.compile("^\\s*CREATE\\s+TABLE\\s+[A-Za-z0-9_]+\\s*\\(.*\\);?", Pattern.CASE_INSENSITIVE);
    private static final Pattern dropTable = Pattern.compile("^\\s*DROP\\s+TABLE\\s+[A-Za-z0-9_]+;?", Pattern.CASE_INSENSITIVE);
    private static final Pattern dropDatabase = Pattern.compile("^\\s*DROP\\s+DATABASE\\s+[A-Za-z0-9_]+;?", Pattern.CASE_INSENSITIVE);
    private static final Pattern createIndex = Pattern.compile("^\\s*CREATE\\s+INDEX\\s+[A-Za-z0-9_]+\\s+ON\\s+[A-Za-z0-9_]+\\s*\\(.*\\);?", Pattern.CASE_INSENSITIVE);
    private static final Pattern dropIndex = Pattern.compile("^\\s*DROP\\s+INDEX\\s+[A-Za-z0-9_.]+\\s+ON\\s[A-Za-z0-9_]+;?", Pattern.CASE_INSENSITIVE);
    private static final Pattern insert = Pattern.compile("^\\s*INSERT\\s+INTO\\s+([A-Za-z0-9]+)\\s+\\((.*)\\)\\s+VALUES\\s+\\((.*)\\);?", Pattern.CASE_INSENSITIVE);
    private static final Pattern delete = Pattern.compile("^\\s*DELETE\\s+FROM\\s+[A-Za-z0-9]+\\s+WHERE\\s+[^ ]*\\s*=\\s*[^ ]*\\s*;?", Pattern.CASE_INSENSITIVE);
    private static final Pattern deleteAll = Pattern.compile("^\\s*DELETE\\s+FROM\\s+[A-Za-z0-9]+\\s*;?", Pattern.CASE_INSENSITIVE);
    private static final Pattern deleteMultiplePK = Pattern.compile("^\\s*DELETE\\s+FROM\\s+[A-Za-z0-9]+\\s+WHERE\\s+[^ ]*\\s*=\\s*[^ ]*\\s+AND\\s+.*\\s*;?", Pattern.CASE_INSENSITIVE);
    private static final Pattern select = Pattern.compile("^\\s*SELECT\\s.*", Pattern.CASE_INSENSITIVE);
    private static final Pattern use = Pattern.compile("^\\s*USE\\s+[A-Za-z0-9_]+;?", Pattern.CASE_INSENSITIVE);

    public static String currentDatabaseName;
    public static String currentTableName;

    public static Command commandType(String command, PrintWriter writer) {
        //System.out.println(command);
        if(command.equals("END")){
            Backend.end = -1;
            return null;
        } else if (use.matcher(command).find()) {
            currentDatabaseName = returnTheDatabaseName(formatCommand(command));
            return null;
        } else if (createDatabase.matcher(command).find()) {
            return new CreateDatabase(formatCommand(command));
        } else if (createTable.matcher(command).find()) {
            return new CreateTable(formatCommand(command));
        } else if (dropDatabase.matcher(command).find()) {
            return new DropDatabase(formatCommand(command));
        } else if (dropTable.matcher(command).find()) {
            return new DropTable(formatCommand(command));
        } else if (createIndex.matcher(command).find()) {
            return new CreateIndex(command);
        } else if (dropIndex.matcher(command).find()) {
            return new DropIndex(formatCommand(command));
        } else if (insert.matcher(command).find()) {
            return new Insert(command);
        } else if (delete.matcher(command).find() || deleteAll.matcher(command).find() || deleteMultiplePK.matcher(command).find()) {
            return new Delete(command);
        } else if (select.matcher(command).find()) {
            return new Select(command, writer);
        }
        ErrorClient.send("Wrong command!");
        System.out.println("Wrong command!");
        return null;
    }

    private static String returnTheDatabaseName(String command) {
        String databaseName = command.split(" ")[1];
        if (databases == null) {
            System.out.println("Doesn't exists JSON file!");
            ErrorClient.send("Doesn't exists JSON file!");
            return null;
        }
        if (databases.checkDatabaseExists(databaseName))
            return databaseName;
        else {
            System.out.println("Doesn't exists this database!");
            ErrorClient.send("Doesn't exists this database!");
            return null;
        }
    }
}
