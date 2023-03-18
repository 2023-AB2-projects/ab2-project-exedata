package Backend.Commands;

import Backend.Parser;
import Backend.SaveLoadJSON.LoadJSON;
import Backend.SaveLoadJSON.SaveJSON;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Arrays;
import java.util.Objects;

public class CreateTable implements Command {
    // create table in a certain database with primary key, foreign key, attributes, null value, default value, constraints, ...
    private String command;
    private JSONObject table;
    private JSONArray structure;
    private JSONArray primaryKey;
    private JSONArray foreignKeys;
    private JSONArray uniqueKeys;
    private JSONArray indexFiles;
    private final String[] keyWords;
    private final String[] type;
    private boolean syntaxError;
    private String databaseName;

    public CreateTable(String command) {
        databaseName = Parser.currentDatabaseName;
        System.out.println(databaseName);
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
        table = new JSONObject();
        structure = new JSONArray();
        primaryKey = new JSONArray();
        foreignKeys = new JSONArray();
        uniqueKeys = new JSONArray();
        indexFiles = new JSONArray();
        //System.out.println(command);

        String[] rows = command.split("\\(", 2);
        String currentTableName = rows[0].split(" ")[2];

        //vegig megyek es keresem az osszes attributomot
        //System.out.println(rows[1]);
        syntaxError = false;
        checkStructure(rows[1]);
        checkConstraint(rows[1]);
        if (!syntaxError) {
            table.put("IndexFiles", indexFiles);
            table.put("uniqueKeys", uniqueKeys);
            table.put("foreignKeys", foreignKeys);
            table.put("primaryKey", primaryKey);
            table.put("Structure", structure);
            table.put("tableName", currentTableName);
        } else
            System.out.println("Syntax Error!");
//        CREATE TABLE Alkalmazottak (
//                SzemSzam VARCHAR PRIMARY KEY,
//                Nev VARCHAR UNIQUE, PRIMARY KEY(Nev, asd, ItalID), PRIMARY KEY(KocsmaID),
//                RészlegID INT FOREIGN KEY REFERENCES Részlegek(RészlegID),
//                first_name VARCHAR NoT NULL,
//                FOREIGN KEY (store_id) REFERENCES sales.stores (store_id),
//                Fizetes INT);
        //PRIMARY KEY(KocsmaID, ItalID),
        //SzemSzám VARCHAR PRIMARY KEY,
        //Név VARCHAR UNIQUE,
        //RészlegID INT REFERENCES Részlegek (RészlegID),
        //Fizetés INT,
        //first_name VARCHAR NOT NULL,
        //FOREIGN KEY (store_id) REFERENCES sales.stores (store_id),
        //CONSTRAINT fk_customer FOREIGN KEY (customer_id) REFERENCES customers (customer_id),
        //CONSTRAINT pk_order_details PRIMARY KEY (order_id, customer_id),
        //System.out.println(table);
        JSONObject databases = LoadJSON.load("databases.json");
        if (databases == null) {
            System.out.println("Doesn't exists JSONFile!");
        } else {
            boolean exists = false;
            //databaseName
            JSONArray jsonArray = (JSONArray) databases.get("Databases"); //main name
            for (Object object : jsonArray) {
                JSONObject jsonObjectSearch = (JSONObject) object;
                String name = (String) jsonObjectSearch.get("databaseName");
                if (name != null && name.equals(databaseName)) { //searched databasename

                    JSONArray tables = (JSONArray) jsonObjectSearch.get("Tabels");
                    for (Object i : tables) {
                        JSONObject table = (JSONObject) i;
                        String tableName = (String) table.get("tableName");
                        if (tableName.equals(currentTableName)) {
                            System.out.println("Table is exists!");
                            exists = true;
                        }
                    }
                    if (!exists) {
                        tables.add(table);
                        SaveJSON.save(databases, "databases.json");
                    }
                    break;
                }
            }

        }
    }

    private void checkConstraint(String line) {
        String[] words = line.split(" ");
        String[] words2;
        String word;
        int i = 0, startIndex = 0;
        int numberOfOpenBrackets = 0;
        int numberOfCloseBrackets = 0;
        int numberOfKeyWords = 0;
        JSONObject reserve;
        if (words[i].equals("")) {
            i++;
            startIndex = i;
        }
        for (; i < words.length; i++) {
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
                    if (words[i].toUpperCase().equals(keyWords[2] + ',') && existsInStructure(words[startIndex])) {//Név VARCHAR UNIQUE,
                        JSONObject uniqueAttribute = new JSONObject();
                        uniqueAttribute.put("UniqueAttribute", words[startIndex]);
                        uniqueKeys.add(uniqueAttribute);
                    } else if (words[startIndex].toUpperCase().equals(keyWords[0])) {//PRIMARY KEY(KocsmaID, asd, ItalID),
                        //PRIMARY KEY(KocsmaID),
                        words2 = words[startIndex + 1].split("\\(");
                        if (words2[0].toUpperCase().equals(keyWords[1]) && existsInStructure(withoutComma(words2[1]))) {
                            reserve = new JSONObject();
                            reserve.put("pkAttribute", withoutComma(words2[1]));
                            primaryKey.add(reserve);
                            for (int j = startIndex + 2; j <= i; j++) {
                                if (existsInStructure(withoutComma(words[j]))) {
                                    reserve = new JSONObject();
                                    reserve.put("pkAttribute", withoutComma(words[j]));
                                    primaryKey.add(reserve);
                                } else {
                                    //System.out.println("Syntax error!");
                                    syntaxError = true;
                                    break;
                                }
                            }
                        }
                    }
                } else if (numberOfKeyWords == 2) {
                    //SzemSzám VARCHAR PRIMARY KEY,
                    //first_name VARCHAR NoT NULL,
                    if (words[i - 1].toUpperCase().equals(keyWords[0]) && existsInStructure(words[startIndex])
                            && words[i].toUpperCase().equals(keyWords[1] + ',')) {
                        reserve = new JSONObject();
                        reserve.put("pkAttribute", withoutComma(words[startIndex]));
                        primaryKey.add(reserve);
                    }
                } else if (numberOfKeyWords == 3) {
                    if (words[startIndex + 2].toUpperCase().equals(keyWords[5])
                            && words[startIndex + 3].toUpperCase().equals(keyWords[1])
                            && words[startIndex + 4].toUpperCase().equals(keyWords[3])
                    ) {//RészlegID INT FOREIGN KEY REFERENCES Részlegek(RészlegID),
                        String foreignTableName = commandToRightFromat(i, startIndex, words[i], words[i - 1], 6);
                        String foreignAttributeName = foreignTableName.split(" ")[1];
                        foreignTableName = foreignTableName.split(" ")[0];
//                        foreignTableName=foreignTableName.split(" ")[0];
//                        System.out.println(words[startIndex]+" "+foreignTableName+" "+foreignAttributeName);
//                        System.out.println(existsInStructure(words[startIndex])
//                                +" "+existsAttributeInTable(foreignTableName, foreignAttributeName));
                        if (existsInStructure(words[startIndex])
                                && existsAttributeInTable(foreignTableName, foreignAttributeName)) {
                            addForeignKeyToJsonFile(words[startIndex], foreignTableName, foreignAttributeName);
                        }
                    } else if (words[startIndex].toUpperCase().equals(keyWords[5])
                            && words[startIndex + 1].toUpperCase().equals(keyWords[1])
                            && words[startIndex + 3].toUpperCase().equals(keyWords[3])
                    ) {//FOREIGN KEY (store_id) REFERENCES sales.stores (store_id),
                        String foreignTableName = commandToRightFromat(i, startIndex, words[i], words[i - 1], 5);
                        String foreignAttributeName = foreignTableName.split(" ")[1];
                        foreignTableName = foreignTableName.split(" ")[0];
                        if (existsInStructure(withoutAnyBrackets(words[startIndex]))
                                && existsAttributeInTable(foreignTableName, foreignAttributeName)) {
                            addForeignKeyToJsonFile(withoutAnyBrackets(words[startIndex]), foreignTableName, foreignAttributeName);
                        }
                    }
                }
                //else{
                //CONSTRAINT pk_order_details PRIMARY KEY (order_id, customer_id),
                //CONSTRAINT fk_customer FOREIGN KEY (customer_id) REFERENCES customers (customer_id),
                //Need???????????
                //}
                numberOfKeyWords = 0;
                startIndex = i + 1;
            }
        }
    }

    private void addForeignKeyToJsonFile(String attribute, String foreignTableName, String foreignAttributeName) {
        JSONArray jsonArray = new JSONArray();
        JSONObject tableName = new JSONObject();
        tableName.put("refTable", foreignTableName);

        JSONObject attributeName = new JSONObject();
        attributeName.put("refAttribute", foreignAttributeName);

        jsonArray.add(tableName);
        jsonArray.add(attributeName);

        JSONObject foreignkey = new JSONObject();
        foreignkey.put("fkAttribute", attribute);
        foreignkey.put("references", jsonArray);
        foreignKeys.add(foreignkey);
    }

    private String commandToRightFromat(int i, int startIndex, String word2, String word1, int x) {
        String s;
        if (startIndex + x == i) {
            s = word1;
            s = s + " " + word2;
        } else {
            s = word2.split("\\(")[0];
            s = s + " " + withoutComma(word2.split("\\(")[1]);
        }
        return s;
    }

    private boolean existsAttributeInTable(String tableName, String attributeName) {
        JSONObject jsonFile = LoadJSON.load("databases.json");
        JSONArray reserve = (JSONArray) jsonFile.get("Databases");
        JSONObject reserve2;
        for (Object i : reserve) {
            reserve2 = (JSONObject) i;
            if (reserve2.get("databaseName").equals(databaseName)) {
                JSONArray reserve3 = (JSONArray) reserve2.get("Tabels");
                for (Object j : reserve3) {
                    JSONObject reserve4 = (JSONObject) j;
                    if (reserve4.get("tableName").equals(tableName)) {
                        JSONArray reserve5 = (JSONArray) reserve4.get("Structure");
                        for (Object k : reserve5) {
                            JSONObject reserve6 = (JSONObject) k;
                            if (reserve6.get("attributeName").equals(attributeName)) {
                                return true;
                            }
                        }
                        return false;
                    }
                    return false;
                }
                return false;
            }
        }
        return false;
    }

    private String withoutAnyBrackets(String word) {
        //(abc) -> abc
        return word.split("\\(")[0].split("\\)")[0];
    }

    private String withoutComma(String word) {
        if (word.charAt(word.length() - 1) == ',') {
            word = word.substring(0, word.length() - 1);
        }
        if (word.charAt(word.length() - 1) == ')') {
            word = word.substring(0, word.length() - 1);
        }
        return word;
    }

    private boolean existsInStructure(String word) {
        JSONObject jsonObject;
        for (Object i : structure) {
            jsonObject = (JSONObject) i;
            if (jsonObject.get("attributeName").equals(word))
                return true;
        }
        return false;
    }

    private void checkStructure(String line) {
        if (line.charAt(line.length() - 1) == ';') {
            line = line.substring(0, line.length() - 1);
        }
        if (line.charAt(line.length() - 1) == ')') {
            line = line.substring(0, line.length() - 1) + ',';
        }
        String[] words = line.split(" ");
        boolean hasKeyWord = false;
        boolean hasType = false;
        int startIndex = 0;
        JSONObject attribute;
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
                    attribute = new JSONObject();
                    attribute.put("attributeName", words[startIndex]);
                    attribute.put("type", words[startIndex + 1]);
                    attribute.put("isnull", 1);
                    structure.add(attribute);
                } else if (hasKeyWord && hasType) {
                    attribute = new JSONObject();
                    attribute.put("attributeName", words[startIndex]);
                    attribute.put("type", words[startIndex + 1]);
                    if (startIndex + 3 == i && words[startIndex + 2].toUpperCase().equals(keyWords[6]) && words[startIndex + 3].toUpperCase().equals(keyWords[7] + ','))
                        attribute.put("isnull", 0);
                    else
                        attribute.put("isnull", 1);
                    structure.add(attribute);
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