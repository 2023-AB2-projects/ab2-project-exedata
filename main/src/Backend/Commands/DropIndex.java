package Backend.Commands;

import Backend.Databases.Databases;
import Backend.Parser;
import Backend.SaveLoadJSON.LoadJSON;
import Backend.SaveLoadJSON.SaveJSON;

import static Backend.Parser.currentDatabaseName;

public class DropIndex implements Command {
    private String command;

    public DropIndex(String command) {
        this.command = command;
    }

    @Override
    public void performAction() {
        //DROP INDEX index_name ON table_name;
        command = command.substring(0, command.length() - 1);
        Databases databases = LoadJSON.load("databases.json");
        if (databases == null) {
            System.out.println("Databases doesn't exists!");
            return;
        }
        String[] commandWords = command.split(" ");
        String currentTableName = commandWords[4];
        String indexName = commandWords[2];

        if (databases.getDatabase(Parser.currentDatabaseName) != null) {
            if (databases.getDatabase(Parser.currentDatabaseName).checkTableExists(currentTableName)) {
                databases.getDatabase(currentDatabaseName).getTable(currentTableName).dropIndex(indexName);
                SaveJSON.save(databases, "databases.json");
            } else {
                System.out.println("Table doesn't exists!");
            }
        } else {
            System.out.println("Database doesn't exists!");
        }
    }
}
