package Backend.Commands;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import Backend.Databases.Databases;
import Backend.Databases.IndexFile;
import Backend.Parser;
import Backend.SaveLoadJSON.LoadJSON;
import Backend.SaveLoadJSON.SaveJSON;
import Backend.SocketServer.ErrorClient;

import static Backend.Commands.FormatCommand.formatWords;

public class CreateIndex implements Command {

    private final String command;
    private Databases databases;

    public CreateIndex(String command) {
        this.command = command;
    }

    @Override
    public void performAction() {
        //CREATE INDEX index_name
        //ON table_name (column1, column2, ...);
        databases = LoadJSON.load("databases.json");
        if (databases == null) {
            System.out.println("Databases doesn't exists!");
            ErrorClient.send("Databases doesn't exists!");
            return;
        }
        String[] commandWords = command.split(" ");
        String currentTableName = commandWords[4];
        String indexName = commandWords[2];
        if (databases.getDatabase(Parser.currentDatabaseName) != null) {
            if (databases.getDatabase(Parser.currentDatabaseName).checkTableExists(currentTableName)) {
                if (createIndex(commandWords, indexName, currentTableName)) {
                    createEmptyIndexFile(indexName + ".ind");
                    SaveJSON.save(databases, "databases.json");
                } else
                    System.out.println("Syntax error!");
                ErrorClient.send("Syntax error!");
            } else {
                System.out.println("Table doesn't exists!");
                ErrorClient.send("Table doesn't exists!");
            }
        } else {
            System.out.println("Database doesn't exists!");
            ErrorClient.send("Databases doesn't exists!");
        }
    }

    private boolean createIndex(String[] commandWords, String IndexName, String currentTableName) {
        //table.addIndexFile(new IndexFile(currentTableName, currentTableName + ".ind", attributeName));
        String indexFileName = IndexName + ".ind";
        String column;
        List<String> indexAttributes = new ArrayList<>();

        for (int i = 5; i < commandWords.length; i++) {
            column = formatWords(commandWords[i]);
            if (databases.getDatabase(Parser.currentDatabaseName).getTable(currentTableName).checkAttributeExists(column)) {
                indexAttributes.add(column);
            } else {
                System.out.println("Column doesn't exists!");
                ErrorClient.send("Column doesn't exists!");
                return false;
            }
        }
        databases.getDatabase(Parser.currentDatabaseName).getTable(currentTableName).addIndexFile(new IndexFile(IndexName, indexAttributes));
        return true;
    }

    protected static void createEmptyIndexFile(String indexFileName) {
        try (FileWriter fileWriter = new FileWriter(indexFileName)) {
            fileWriter.write("");
            fileWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
