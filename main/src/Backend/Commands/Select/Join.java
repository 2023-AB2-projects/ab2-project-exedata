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
    private List<String> tableNames;
    private Selection tableSelectResultsSeparately;
    private List<Condition> joinConditions;
    private List<List<String>> indexArray; // attributeNames of a table (ex. indexArray.get(0) => [DiscID, DName, CreditNr])
    private List<String> joinResult;

    public Join(SelectManager selectManager) {
        this.selectManager = selectManager;
        tableNames = selectManager.getFrom();
        tableSelectResultsSeparately = new Selection(selectManager);
        joinConditions = selectManager.getJoin();
        indexArray = new ArrayList<>();

        List<String> table1 = tableSelectResultsSeparately.processing(0);
        //List<String> table2 = tableSelectResultsSeparately.processing(1);
        indexArray.add(getAttributeNames(tableNames.get(0)));
//        System.out.println(indexArray.get(0));
//        System.out.println(getValueOfAttributeName(joinResult.get(0), 0, "DiscID"));
        joinResult = new ArrayList<>();

        joinResult.add(indexArrayToString(indexArray.get(0)));

        List<String> attributeNames = indexArray.get(0);

        for (int i=0; i<table1.size(); i++) {
            for (int j=0; j<joinConditions.size(); j++) {

            }
            StringBuilder stringBuilder = new StringBuilder();
            for (int j=0; j<attributeNames.size(); j++) {
                stringBuilder.append(getAttributeValueByName(table1.get(i), 0, attributeNames.get(j))).append("#");
            }
            stringBuilder = new StringBuilder(stringBuilder.substring(0, stringBuilder.length()-1));
            joinResult.add(stringBuilder.toString());
        }
    }

    public List<String> getJoinResult() {
        return joinResult;
    }

    private String indexArrayToString(List<String> list) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String i : list) {
            stringBuilder.append(i).append("#");
        }
        return (stringBuilder.substring(0, stringBuilder.length()-1));
    }

    private String getAttributeValueByName(String record, int indexOfTable, String attributeName) {
        return record.split("#")[indexArray.get(indexOfTable).indexOf(attributeName)];
    }

    private List<String> getAttributeNames(String tableName) {
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
