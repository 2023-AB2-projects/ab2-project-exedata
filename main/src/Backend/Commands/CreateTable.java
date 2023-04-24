package Backend.Commands;

import Backend.Parser;
import Backend.SaveLoadJSON.LoadJSON;
import Backend.SaveLoadJSON.SaveJSON;

import java.util.ArrayList;
import java.util.List;

import Backend.Databases.*;
import Backend.SocketServer.ErrorClient;
import Backend.MongoDBManagement.MongoDB;

import static Backend.Commands.FormatCommand.formatWords;

public class CreateTable implements Command {
    // create table in a certain database with primary key, foreign key, attributes, null value, default value, constraints, ...
    private String command;
    private Table table;
    private final String[] keyWords;
    private final String[] type;
    private boolean syntaxError;
    private final String databaseName;
    private Databases databases;

    public CreateTable(String command) {
        databaseName = Parser.currentDatabaseName;
        this.command = command;
        keyWords = new String[8];
        keyWords[0] = "PRIMARY";
        keyWords[1] = "KEY";
        keyWords[2] = "UNIQUE";
        keyWords[3] = "REFERENCES";
        keyWords[4] = "CONSTRAINT";
        keyWords[5] = "FOREIGN";
        keyWords[6] = "NOT";
        keyWords[7] = "NULL";
        //keyWords[8] = "CHECK";
        //keyWords[9] = "DEFAULT";
        type = new String[6];
        type[0] = "INT";
        type[1] = "FLOAT";
        type[2] = "BIT";
        type[3] = "DATE";
        type[4] = "DATETIME";
        type[5] = "VARCHAR";
    }

    @Override
    public void performAction() {
        List<Attribute> attributeList = new ArrayList<>();
        List<String> primaryKeyList = new ArrayList<>();
        List<ForeignKey> foreignKeysList = new ArrayList<>();
        List<String> uniqueKeysList = new ArrayList<>();
        List<IndexFile> indexFilesList = new ArrayList<>();

        command = command + ',';
        System.out.println(command);

        String[] beforeAndAfterTheFirstOpenBracket = command.split("\\(", 2);
        String currentTableName = beforeAndAfterTheFirstOpenBracket[0].split(" ")[2];
        table = new Table(currentTableName, attributeList, primaryKeyList, foreignKeysList, uniqueKeysList, indexFilesList);

        databases = LoadJSON.load("databases.json");
        if (databases == null) {
            System.out.println("Doesn't exists JSONFile!");
            ErrorClient.send("Doesn't exists JSONFile!");
            return;
        }

        syntaxError = false;
        getItemFromStructure(beforeAndAfterTheFirstOpenBracket[1]);
        fillJSONArrayByConstraint(beforeAndAfterTheFirstOpenBracket[1]);
        //createPrimaryKeyDefaultIndex(currentTableName);

        if (syntaxError) {
            System.out.println("Syntax Error!");
            ErrorClient.send("Syntax Error!");
            return;
        }
        if (databases == null) {
            System.out.println("Doesn't exists JSONFile!");
            ErrorClient.send("Doesn't exists JSONFile!");
        } else {
            if (databases.checkDatabaseExists(databaseName)) {
                if (!databases.getDatabase(databaseName).checkTableExists(table.getName())) {
                    databases.getDatabase(databaseName).addTable(table);
                    SaveJSON.save(databases, "databases.json");
                    MongoDB mongoDB = new MongoDB();
                    mongoDB.createDatabaseOrUse(databaseName);
                    mongoDB.createCollection(table.getName());
                    mongoDB.disconnectFromLocalhost();
                    ErrorClient.send("Table " + currentTableName + " created!");
                } else {
                    System.out.println("Table is exists!");
                    ErrorClient.send("Table is exists!");
                }
            } else {
                System.out.println("Doesn't exists this database!");
                ErrorClient.send("Doesn't exists this database!");
            }
        }
    }

    private void createPrimaryKeyDefaultIndex(String currentTableName) {
        //indexFiles
        //primaryKey
        List<String> attributeName = table.getPrimaryKey();
        table.addIndexFile(new IndexFile(currentTableName, attributeName, "1"));
        //createEmptyIndexFile(currentTableName + ".ind");
    }

    private void fillJSONArrayByConstraint(String line) {
        String[] words = line.split(" ");
        String[] words2;
        int i = 0, startIndex = 0;
        int numberOfOpenBrackets = 0;
        int numberOfCloseBrackets = 0;
        int numberOfKeyWords = 0;
        if (words[i].equals("")) {
            i++;
            startIndex = i;
        }
        for (; i < words.length; i++) {
            //System.out.println(words[i]);
            if (words[i].indexOf('(') != -1) {
                numberOfOpenBrackets += 1;
            }
            if (words[i].indexOf(')') != -1) {
                numberOfCloseBrackets += 1;
            }
            if (isKeyWords(words[i]))
                numberOfKeyWords += 1;
            //System.out.println(words[i]+" "+numberOfOpenBrackets+" "+numberOfCloseBrackets);
            if (words[i].indexOf(',') != -1 && numberOfOpenBrackets == numberOfCloseBrackets) {
                //System.out.println(words[i] + " " + numberOfKeyWords);
                if (numberOfKeyWords == 1) {
                    //System.out.println("d");
                    if (words[i].toUpperCase().equals(keyWords[2] + ',') && table.checkAttributeExists(words[startIndex])) {//Név VARCHAR UNIQUE,
                        table.addUnique(words[startIndex]);
                    } else if (words[startIndex].toUpperCase().equals(keyWords[0])) {//PRIMARY KEY(KocsmaID, asd, ItalID),
                        //PRIMARY KEY(KocsmaID),
                        words2 = words[startIndex + 1].split("\\(");
                        if (words2[0].toUpperCase().equals(keyWords[1]) && table.checkAttributeExists(formatWords(words2[1]))) {
                            table.addPrimaryKey(formatWords(words2[1]));
                            for (int j = startIndex + 2; j <= i; j++) {
                                if (table.checkAttributeExists(formatWords(words[j]))) {
                                    table.addPrimaryKey(formatWords(words[j]));
                                } else {
                                    //System.out.println("Syntax error!");
                                    syntaxError = true;
                                    break;
                                }
                            }
                        }
                    } else if (words[startIndex + 2].toUpperCase().equals(keyWords[3])) {
                        //SpecID varchar REFERENCES specialization (SpecID)
                        String foreignTableName = commandToRightFormat(i, startIndex, words[i], words[i - 1], 4);
                        String foreignAttributeName = formatWords(foreignTableName.split(" ")[1]);
                        foreignTableName = foreignTableName.split(" ")[0];
                        if (table.checkAttributeExists(words[startIndex])
                                && existsAttributeForeignInTable(foreignTableName, foreignAttributeName)) {
                            addForeignKeyToJsonFile(words[startIndex], foreignTableName, foreignAttributeName);
                        }
                    }
                } else if (numberOfKeyWords == 2) {
                    //PRIMARY KEY (StudID,DiscID)
                    if (words[startIndex].toUpperCase().equals(keyWords[0])
                            && words[startIndex + 1].toUpperCase().equals(keyWords[1])) {
                        words2 = formatWords(words[i]).split(",");
                        for (String j : words2) {
                            if (!j.equals("")) {
                                if (table.checkAttributeExists(formatWords(j))) {
                                    table.addPrimaryKey(formatWords(j));
                                } else {
//                                    System.out.println("Syntax error!");
                                    syntaxError = true;
                                    break;
                                }
                            }
                        }
                    } else {
                        //SzemSzám VARCHAR PRIMARY KEY,
                        //first_name VARCHAR NoT NULL,
                        if (words[i - 1].toUpperCase().equals(keyWords[0]) && table.checkAttributeExists(words[startIndex])
                                && words[i].toUpperCase().equals(keyWords[1] + ',')) {
                            table.addPrimaryKey(formatWords(words[startIndex]));
                        }
                    }
                } else if (numberOfKeyWords == 3) {
                    if (words[startIndex + 2].toUpperCase().equals(keyWords[5])
                            && words[startIndex + 3].toUpperCase().equals(keyWords[1])
                            && words[startIndex + 4].toUpperCase().equals(keyWords[3])
                    ) {//RészlegID INT FOREIGN KEY REFERENCES Részlegek(RészlegID),
                        String foreignTableName = commandToRightFormat(i, startIndex, words[i], words[i - 1], 6);
                        String foreignAttributeName = foreignTableName.split(" ")[1];
                        foreignTableName = foreignTableName.split(" ")[0];
                        if (table.checkAttributeExists(words[startIndex])
                                && existsAttributeForeignInTable(foreignTableName, foreignAttributeName)) {
                            addForeignKeyToJsonFile(words[startIndex], foreignTableName, foreignAttributeName);
                        }
                    } else if (words[startIndex].toUpperCase().equals(keyWords[5])
                            && words[startIndex + 1].toUpperCase().equals(keyWords[1])
                            && words[startIndex + 3].toUpperCase().equals(keyWords[3])
                    ) {//FOREIGN KEY (store_id) REFERENCES sales.stores (store_id),
                        String foreignTableName = commandToRightFormat(i, startIndex, words[i], words[i - 1], 5);
                        String foreignAttributeName = foreignTableName.split(" ")[1];
                        foreignTableName = foreignTableName.split(" ")[0];
                        if (table.checkAttributeExists(formatWords(words[startIndex]))
                                && existsAttributeForeignInTable(foreignTableName, foreignAttributeName)) {
                            addForeignKeyToJsonFile(formatWords(words[startIndex]), foreignTableName, foreignAttributeName);
                        }
                    }
                }
                numberOfKeyWords = 0;
                startIndex = i + 1;
            }
        }
    }

    private void addForeignKeyToJsonFile(String attribute, String foreignTableName, String foreignAttributeName) {
        ForeignKey foreignKey = new ForeignKey(attribute, foreignTableName, foreignAttributeName);
        table.addForeignKey(foreignKey);
    }

    private String commandToRightFormat(int i, int startIndex, String word2, String word1, int x) {
        String s;
        if (startIndex + x == i) {
            s = formatWords(word1);
            s = s + " " + formatWords(word2);
        } else {
            s = word2.split("\\(")[0];
            s = s + " " + formatWords(word2.split("\\(")[1]);
        }
        return s;
    }

    private boolean existsAttributeForeignInTable(String tableName, String attributeName) {
        if (databases.getDatabase(databaseName) != null) {
            if (databases.getDatabase(databaseName).getTable(tableName) != null) {
                return databases.getDatabase(databaseName).getTable(tableName).checkAttributeExists(attributeName);
            } else {
                System.out.println("Table doesn't exists!");
                ErrorClient.send("Table doesn't exists!");
            }
        } else {
            System.out.println("Database doesn't exists!!!");
            ErrorClient.send("Database doesn't exists!!!");
        }
        return true;
    }

    private void getItemFromStructure(String line) {
        String[] words = line.split(" ");
        boolean hasKeyWord = false;
        boolean hasType = false;
        int startIndex = 0;
        Attribute attribute;
        for (int i = 0; i < words.length; i++) {
            if (words[i].equals("")) {
                i++;
                startIndex = i;
            }
            //System.out.println(words[i]);
            if (isKeyWords(words[i]))
                hasKeyWord = true;
            else if (isType(words[i]))
                hasType = true;
            if (words[i].indexOf(',') != -1) {
                if (!hasKeyWord && hasType) {
                    words[startIndex + 1] = words[startIndex + 1].substring(0, words[startIndex + 1].length() - 1);
                    attribute = new Attribute(words[startIndex], words[startIndex + 1].toUpperCase(), "1");
                    table.addAttribute(attribute);
                } else if (hasKeyWord && hasType) {
                    String isnull;
                    if (startIndex + 3 == i && words[startIndex + 2].toUpperCase().equals(keyWords[6]) && words[startIndex + 3].toUpperCase().equals(keyWords[7] + ','))
                        isnull = "0";
                    else
                        isnull = "1";
                    attribute = new Attribute(words[startIndex], words[startIndex + 1].toUpperCase(), isnull);
                    table.addAttribute(attribute);
                }
                hasKeyWord = false;
                hasType = false;
                startIndex = i + 1;
            }
        }
    }

    private boolean isKeyWords(String word) {
        word = word.toUpperCase();
        if (word.charAt(word.length() - 1) == ',') {
            word = word.substring(0, word.length() - 1);
        }
        for (String i : keyWords) {
            if (i.equals(word))
                return true;
        }
        return false;
    }

    private boolean isType(String word) {
        word = word.toUpperCase();
        if (word.charAt(word.length() - 1) == ',') {
            word = word.substring(0, word.length() - 1);
        }
        for (String i : type) {
            if (i.equals(word))
                return true;
        }
        return false;
    }
}