package Backend;

import Backend.Databases.Attribute;
import Backend.Databases.Database;
import Backend.Databases.Databases;
import Backend.Databases.Table;
import Backend.SaveLoadJSON.LoadJSON;

import java.util.List;

public class RandomGenerator {
    public static void main(String[] args) {
        new RandomGenerator("test", "alma", "", 10);
    }

    private final String databaseName;
    private final String tableName;
    private final String fieldName;
    private final int numberOfRows;

    public RandomGenerator(String databaseName, String tableName, String fieldName, int numberOfRows) {
        this.databaseName = databaseName;
        this.tableName = tableName;
        this.fieldName = fieldName;
        this.numberOfRows = numberOfRows;
        generate();
    }

    private void generate() {
        /*
        create database test;
        use test;
        create table alma(
	    id int primary key,
	    name varchar unique,
	    email varchar,
	    tel int unique,
	    age int
        );
         */
        Databases databases = LoadJSON.load("databases.json");
        assert databases != null;
        Database database = databases.getDatabase(databaseName);
        Table table = database.getTable(tableName);
        List<Attribute> attributeList = table.getStructure();
        List<String> primaryKeyList = table.getPrimaryKey();
        List<String> uniqueList = table.getUniqueKeys();
    }
}
