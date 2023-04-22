package Backend.Commands;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Backend.Databases.Databases;
import Backend.Databases.IndexFile;
import Backend.Parser;
import Backend.SaveLoadJSON.LoadJSON;
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
        Parser.currentDatabaseName = "University";
        databases = LoadJSON.load("databases.json");
        if (databases == null) {
            ErrorClient.send("Databases doesn't exists!");
            return;
        }
        Pattern pattern = Pattern.compile("^\\s*CREATE\\s+INDEX\\s+([A-Za-z0-9]+)\\s+ON\\s+([A-Za-z0-9]+)\\s+\\((.*)\\);?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(command);
        if (matcher.matches()) {
            String indexName = matcher.group(1);
            String tableName = matcher.group(2);
            String[] attributeNames = matcher.group(3).replaceAll(" ", "").split(",");
            System.out.println(attributeNames[0]);
        } else {
            ErrorClient.send("Wrong command!");
        }
//        if (databases.getDatabase(Parser.currentDatabaseName) != null) {
//            if (databases.getDatabase(Parser.currentDatabaseName).checkTableExists(tableName)) {
//                if (createIndex(commandWords, indexName, tableName)) {
//                    createEmptyIndexFile(indexName + ".ind");
//                    SaveJSON.save(databases, "databases.json");
//                } else {
//                    ErrorClient.send("Syntax error!");
//                }
//            } else {
//                ErrorClient.send("Table doesn't exists!");
//            }
//        } else {
//            ErrorClient.send("Databases doesn't exists!");
//        }
        //String attributes = commandWords[5].substring(1);
        //createIndexFileInMongoDB();
    }

    private boolean createIndex(String[] commandWords, String IndexName, String currentTableName) {
        //table.addIndexFile(new IndexFile(currentTableName, currentTableName + ".ind", attributeName));
        String indexFileName = IndexName + ".ind";
        String column;
        List<String> indexAttributes = new ArrayList<>();
        String isUnique = "0";
        for (int i = 5; i < commandWords.length; i++) {
            column = formatWords(commandWords[i]);
            if (databases.getDatabase(Parser.currentDatabaseName).getTable(currentTableName).checkAttributeExists(column)) {
                indexAttributes.add(column);
                if (databases.getDatabase(Parser.currentDatabaseName).getTable(currentTableName).isUnique(column))
                    isUnique="1";
            } else {
                ErrorClient.send("Column doesn't exists!");
                return false;
            }
        }
        databases.getDatabase(Parser.currentDatabaseName).getTable(currentTableName).addIndexFile(new IndexFile(IndexName, indexAttributes, isUnique));
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

    private void createIndexFileInMongoDB() {

    }
}
