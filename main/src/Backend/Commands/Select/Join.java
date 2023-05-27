package Backend.Commands.Select;

import Backend.Commands.Condition;
import Backend.Databases.Attribute;
import Backend.Parser;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

import static Backend.SocketServer.Server.databases;

public class Join {
    private SelectManager selectManager;
    private List<String> tables;
    private Selection tableSelectResultsSeparately;
    private List<Condition> joinConditions;
    private List<List<String>> indexArray;
    private List<String> joinResult;

    public Join(SelectManager selectManager) {
        this.selectManager = selectManager;
        tables = selectManager.getFrom();
        tableSelectResultsSeparately = new Selection(selectManager);
        joinConditions = selectManager.getJoin();
        indexArray = new ArrayList<>();
        joinResult = tableSelectResultsSeparately.processing(0);
        indexArray.add(getAttributes(tables.get(0)));
        System.out.println(joinResult);
        System.out.println(joinResult.get(0).split("#")[(indexArray.get(0).indexOf("DiscID"))]);
    }

    private String getValueOfAttributeName(String record, int indexOfTable, String attributeName) {
        return record.split("#")[indexArray.get(indexOfTable).indexOf(attributeName)];
    }

    private List<String> getAttributes(String tableName) {
        List<String> result = databases.getDatabase(Parser.currentDatabaseName).getTable(tableName).getPrimaryKey();
        List<String> primaryKey = databases.getDatabase(Parser.currentDatabaseName).getTable(tableName).getPrimaryKey();
        for (Attribute i : databases.getDatabase(Parser.currentDatabaseName).getTable(tableName).getStructure()) {
            if (!primaryKey.contains(i.getName())) {
                result.add(i.getName());
            }
        }
        return result;
    }
}
