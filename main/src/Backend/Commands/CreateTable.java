package Backend.Commands;

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
    private JSONArray IndexFiles;
    private final String[] keyWords;
    private final String[] type;

    public CreateTable(String command) {
        this.command = command;
        keyWords = new String[10];
        keyWords[0] = "PRIMARY";
        keyWords[1] = "KEY";
        keyWords[2] = "UNIQUE";
        keyWords[3] = "REFERENCES";
        keyWords[4] = "CONSTRAINT";
        keyWords[5] = "FOREIGN";
        keyWords[6] = "DEFAULT";
        keyWords[7] = "CHECK";
        keyWords[8] = "NOT";
        keyWords[9] = "NULL";
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
        IndexFiles = new JSONArray();
        System.out.println(command);

        String[] rows = command.split("\\(", 2);
        String currentDatabaseName = rows[0].split(" ")[2];
        //rows[1]

        //vegig megyek es keresem az osszes attributomot
        System.out.println(rows[1]);
        checkStructure(rows[1]);
    }

    private void checkStructure(String line) {
        System.out.println(line);
        if (line.charAt(line.length() - 1) == ';') {
            line = line.substring(0, line.length() - 1);
        }
        if (line.charAt(line.length() - 1) == ')') {
            line = line.substring(0, line.length() - 1)+',';
        }
        System.out.println(line);
        String[] words = line.split(" ");
        int numberOfComma = 0;
        int i;
        boolean hasKeyWord = false;
        boolean hasType = false;
        int startIndex = 0;
        //PRIMARY KEY(KocsmaID, ItalID),
        //SzemSzám VARCHAR(13) PRIMARY KEY,
        //Név VARCHAR(30) UNIQUE,
        //RészlegID INT REFERENCES Részlegek (RészlegID),
        //Fizetés INT,
        //first_name VARCHAR NOT NULL,
        //FOREIGN KEY (store_id) REFERENCES sales.stores (store_id),
        //order_date DATE DEFAULT CURRENT_DATE,
        //status VARCHAR(20) CHECK (status IN ('New', 'Processing', 'Shipped', 'Delivered')),
        //CONSTRAINT fk_customer FOREIGN KEY (customer_id) REFERENCES customers (customer_id),
        //CONSTRAINT pk_order_details PRIMARY KEY (order_id, customer_id)

        //CREATE TABLE sales.visits (
        //    visit_id INT PRIMARY KEY,
        //    first_name VARCHAR (50) NOT NULL,
        //    last_name VARCHAR (50) NOT NULL,
        //    visited_at DATETIME,
        //    phone VARCHAR(20),
        //    store_id INT NOT NULL,
        //    FOREIGN KEY (store_id) REFERENCES sales.stores (store_id)
        //);

        //CREATE TABLE orders (
        //   order_id INT PRIMARY KEY,
        //   customer_id INT,
        //   order_date DATE DEFAULT CURRENT_DATE,
        //   total_amount DECIMAL(10, 2),
        //   status VARCHAR(20) CHECK (status IN ('New', 'Processing', 'Shipped', 'Delivered')),
        //   CONSTRAINT fk_customer FOREIGN KEY (customer_id) REFERENCES customers (customer_id),
        //   CONSTRAINT pk_order_details PRIMARY KEY (order_id, customer_id)
        //);

        for (i = 0; i < words.length; i++) {
            if (words[i].equals("")) {
                i++;
                startIndex=i;
            }
            System.out.println(words[i]);
            if (isKeyWords(words[i]))
                hasKeyWord = true;
            if (isType(words[i]))
                hasType = true;

            if (words[i].indexOf(',') != -1) {
                numberOfComma += 1;
                if (numberOfComma == 1 && !hasKeyWord && hasType) {
                    words[startIndex + 1]=words[startIndex + 1].substring(0, words[startIndex + 1].length() - 1);
                    System.out.println(words[startIndex] + " " + words[startIndex + 1]);
                    hasType=false;
                    numberOfComma=0;
                    startIndex=i+1;
                } else if (numberOfComma == 1 && hasKeyWord && hasType) {
                    System.out.println(words[startIndex] + " " + words[startIndex + 1]);
                    hasKeyWord=false;
                    hasType=false;
                    numberOfComma=0;
                    startIndex=i+1;
                } else if (numberOfComma>=1 && !hasType) {
                    hasKeyWord=false;
                    hasType=false;
                    numberOfComma=0;
                    startIndex=i+1;
                }
            }
        }
    }

    private boolean isKeyWords(String word) {
        word=word.toUpperCase();
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
        word=word.toUpperCase();
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