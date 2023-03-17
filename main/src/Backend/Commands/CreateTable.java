package Backend.Commands;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Arrays;
import java.util.Objects;

public class CreateTable implements Command {
    // create table in a certain database with primary key, foreign key, attributes, null value, default value, constraints, ...
    private final String command;
    private JSONObject table;
    private JSONArray structure;
    private JSONArray primaryKey;
    private JSONArray foreignKeys;
    private JSONArray uniqueKeys;
    private JSONArray IndexFiles;

    public CreateTable(String command) {
        this.command = command;
    }

    @Override
    public void performAction() {
//        int, float, bit, date, datetime, varchar
        //CREATE TABLE Alkalmazottak ( SzemSzám VARCHAR(13) PRIMARY KEY, Név VARCHAR(30) UNIQUE, RészlegID INT REFERENCES Részlegek (RészlegID), Fizetés INT);
        System.out.println(command);
        String[] rows = command.split("\\(", 2);
        String currentDatabaseName = rows[0].split(" ")[2];
        rows = rows[1].split(",");

        table = new JSONObject();
        structure = new JSONArray();
        primaryKey = new JSONArray();
        foreignKeys = new JSONArray();
        uniqueKeys = new JSONArray();
        IndexFiles = new JSONArray();

        String[] words;
        for (String i : rows) {
            System.out.println("asd");
            System.out.println(i);
            words=i.split(" ");
            if(Objects.equals(words[0], "")){
                checkLines(Arrays.copyOfRange(words, 1, words.length));
            }else{
                checkLines(words);
            }
        }
    }
    private void checkLines(String[] words){
        System.out.println(words);
        if(Objects.equals(words[0].toUpperCase(),"PRIMARY")){
            //PRIMARY KEY(KocsmaID, ItalID)

            if(Objects.equals(words[1].toUpperCase(),"KEY")){

            }else{
                System.out.println("Syntax error!");
            }
        }
    }
}