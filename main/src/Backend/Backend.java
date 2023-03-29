package Backend;

import Backend.Databases.*;
import Backend.HttpServer.Server;
import Backend.SaveLoadJSON.LoadJSON;
import Backend.SaveLoadJSON.SaveJSON;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Backend {
    public static void main(String[] args) throws IOException {
        Server server = new Server(12000);
        //proba
//        ObjectMapper objectMapper = new ObjectMapper();
//        List<Attribute> attributeList = new ArrayList<Attribute>();
//        attributeList.add(new Attribute("alma", "int", "0"));
//        attributeList.add(new Attribute("korte", "int", "0"));
//
//        List<ForeignKey> foreignKeyList = new ArrayList<ForeignKey>();
//
//        List<IndexFile> indexFileList = new ArrayList<IndexFile>();
//
//        List<String> primaryKeyList = new ArrayList<String>();
//        primaryKeyList.add(new String("alma"));
//        primaryKeyList.add(new String("korte"));
//
//        List<String> uniqueKeysList = new ArrayList<String>();
//
//        Table table = new Table("testTable", attributeList, primaryKeyList, foreignKeyList, uniqueKeysList, indexFileList);
//        Table table2 = new Table("testTable", attributeList, primaryKeyList, foreignKeyList, uniqueKeysList, indexFileList);
//
//        List<Table> tableList = new ArrayList<Table>();
//        tableList.add(table2);
//        tableList.add(table);
//        Database database1 = new Database("db1", tableList);
//        Database database2 = new Database("db2", tableList);
//        List<Database> databaseList = new ArrayList<Database>();
//        databaseList.add(database1);
//        databaseList.add(database2);
//        Databases databases = new Databases(databaseList);
//        SaveJSON.save(databases,"test.json");
        //tables.add(table);
        //tables.add(table2);
        //objectMapper.writeValue(new File("test.json"), tables);
        //databases = LoadJSON.load("test.json");

    }
}
