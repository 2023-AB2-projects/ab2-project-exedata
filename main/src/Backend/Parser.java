package Backend;

import Backend.Commands.*;
import Backend.Exceptions.UnknownCommandException;

import java.util.regex.Pattern;

public class Parser {
    // Create instances of the matched type
    // Perform action on the type
    // Add to the response
    private static final Pattern createDatabase = Pattern.compile("^CREATE\sDATABASE\s[A-Za-z0-9_]*;?", Pattern.CASE_INSENSITIVE);
    private static final Pattern createTable = Pattern.compile("^CREATE\s*TABLE\s*[A-Za-z0-9_]*\s*\\(.*\\);?", Pattern.CASE_INSENSITIVE);
    private static final Pattern dropDatabase = Pattern.compile("^DROP\s*DATABASE\s*[A-Za-z0-9_]*;?", Pattern.CASE_INSENSITIVE);
    private static final Pattern createIndex = Pattern.compile("^CREATE\s*INDEX\s*[A-Za-z0-9_]*\s*ON\s[A-Za-z0-9_]*\s*\\(.*\\);?", Pattern.CASE_INSENSITIVE);
    private static final Pattern dropIndex = Pattern.compile("^DROP\s*INDEX\s*[A-Za-z0-9_]*\s*ON\s[A-Za-z0-9_]*;?", Pattern.CASE_INSENSITIVE);

    public static Command commandType(String command) throws UnknownCommandException {
        if (createDatabase.matcher(command).find()) {
            return new CreateDatabase();
        } else if (createTable.matcher(command).find()) {
            return new CreateTable();
        } else if (dropDatabase.matcher(command).find()) {
            return new DropDatabase();
        } else if (createIndex.matcher(command).find()) {
            return new CreateIndex();
        } else if (dropIndex.matcher(command).find()) {
            return new DropIndex();
        }
        throw new UnknownCommandException();
    }

    // Return the values to the http server
}
