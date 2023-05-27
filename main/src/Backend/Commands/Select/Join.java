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
    private List<List<String>> tableSelectResults;
    private List<Condition> joinConditions;
    private List<List<String>> attributeNamesTableIndex; // attributeNames of a table (ex. indexArray.get(0) => [DiscID, DName, CreditNr])
    private List<String> joinResult;

    public Join(SelectManager selectManager) {
        this.selectManager = selectManager;
        tableNames = selectManager.getFrom();
        tableSelectResultsSeparately = new Selection(selectManager);
        joinConditions = selectManager.getJoin();
        attributeNamesTableIndex = new ArrayList<>();
        tableSelectResults = new ArrayList<>();

        List<String> allAttributeNames = new ArrayList<>();
        for (int i=0; i<tableNames.size(); i++) {
            List<String> selectResults = tableSelectResultsSeparately.processing(i);
            List<String> attributeNames = getAttributeNames(tableNames.get(i));
            allAttributeNames.addAll(attributeNames);
        }
        System.out.println(allAttributeNames);
        List<String> table1 = tableSelectResultsSeparately.processing(0);
        joinResult = new ArrayList<>();
        joinResult.add(attributeNamesTableIndexToString(allAttributeNames));

        for (int i=0; i<table1.size(); i++) {
            StringBuilder stringBuilder = new StringBuilder();
            for (int j=0; j<getAttributeNames(tableNames.get(0)).size(); j++) {
                stringBuilder.append(getAttributeValueByName(table1.get(i), 0, getAttributeNames(tableNames.get(0)).get(j))).append("#");
            }
            stringBuilder = new StringBuilder(stringBuilder.substring(0, stringBuilder.length()-1));
            joinResult.add(stringBuilder.toString());
        }
    }

    public List<String> getJoinResult() {
        return joinResult;
    }

    private String attributeNamesTableIndexToString(List<String> list) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String i : list) {
            stringBuilder.append(i).append("#");
        }
        return (stringBuilder.substring(0, stringBuilder.length()-1));
    }

    private String getAttributeValueByName(String record, int indexOfTable, String attributeName) {
        return record.split("#")[attributeNamesTableIndex.get(indexOfTable).indexOf(attributeName)];
    }

    private List<String> getAttributeNames(String tableName) {
        List<String> result = new ArrayList<>();
        for (Attribute i : databases.getDatabase(Parser.currentDatabaseName).getTable(tableName).getStructure()) {
            result.add(i.getName());
        }
        return result;
    }
}
