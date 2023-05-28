package Backend.Commands.Select;

import Backend.Databases.Attribute;
import Backend.Parser;

import java.sql.SQLOutput;
import java.util.*;

import static Backend.SocketServer.Server.databases;

public class GroupBy {
    private SelectManager selectManager;
    List<String> results;
    public GroupBy(SelectManager selectManager, List<String> currentResults) {
        this.selectManager = selectManager;
        List<String> groupByAttributes = selectManager.getGroupBy();
        List<String> selectAttributes = selectManager.getSelect();
        List<String> selectASAttributes = selectManager.getSelectAS();
        List<String> tableNameOfGroupByAttributes = selectManager.getTableNameOfGroupByAttribute();
        List<String> tableNameOfSelectByAttributes = selectManager.getTableNameOfSelectAttribute();

        Selection selection = new Selection(selectManager);
        List<String> tableData = currentResults;

//        SELECT d.DName, AVG(d.CreditNr)
//        FROM disciplines d
//        GROUP BY d.DName

//        SELECT marks.DiscID
//        FROM disciplines INNER JOIN marks ON disciplines.DiscID=marks.DiscID
//        GROUP BY marks.DiscID
        results = new ArrayList<>();
        StringBuilder attributeResults = new StringBuilder();
        for (int i=0; i<selectAttributes.size(); i++) {
            if (selectASAttributes.get(i)!=null) {
                attributeResults.append(tableNameOfSelectByAttributes.get(i)).append(".").append(selectASAttributes.get(i)).append("#");
            } else {
                attributeResults.append(tableNameOfSelectByAttributes.get(i)).append(".").append(selectAttributes.get(i)).append("#");
            }
        }
        attributeResults = new StringBuilder(attributeResults.substring(0, attributeResults.length()-1));
        results.add(attributeResults.toString());

        // go through table elements and apply group by condition
        String allAttributes = currentResults.get(0);
        String groupByAttribute = results.get(0);
        System.out.println(allAttributes);
        System.out.println("============================================");

        for (int i=1; i<currentResults.size(); i++) {
            System.out.println(currentResults.get(i));
        }

//        int groupByAttributePos = getGroupByAttributePosition(attributeNames,
//                tableNameOfGroupByAttributes.get(0), groupByAttributes.get(0));
//
//        Map<String, Double> countMap = new HashMap<>();
//        Map<String, Double> sumMap = new HashMap<>();
//        Map<String, Double> avgMap = new HashMap<>();
//
////        List<Integer> avgColumnPositions = getAvgColumnPositions(selectAttributes);
////        for (int i=0; i<avgColumnPositions.size(); i++) {
////            System.out.println(avgColumnPositions.get(i));
////        }
//
//        Set<String> uniqueStrings = new HashSet<>();
//
//        for (String item : tableData) {
//            String[] columns = item.split("#");
//
//            String columnAttribute = columns[groupByAttributePos];
//            uniqueStrings.add(columnAttribute);
//        }
//
//        for (String uniqueString : uniqueStrings) {
//            results.add(uniqueString);
//        }
//
//        System.out.println(results);




    }

    private List<String> getAttributeNames(String tableName) {
        List<String> pk = databases.getDatabase(Parser.currentDatabaseName).getTable(tableName).getPrimaryKey();
        List<String> result = new ArrayList<>();
        for (String i : pk) {
            result.add(tableName + "." + i);
        }
        for (Attribute i : databases.getDatabase(Parser.currentDatabaseName).getTable(tableName).getStructure()) {
            if (!pk.contains(i.getName()))
                result.add(tableName + "." + i.getName());
        }
        return result;
    }

    private int getGroupByAttributePosition(List<String> attributeNames, String tableNameOfGroupByAttribute,
                                            String groupByAttribute) {
        for (int i=0; i<attributeNames.size(); i++) {
            List<String> splitted = List.of(attributeNames.get(i).split("\\."));
            if (splitted.get(0).equals(tableNameOfGroupByAttribute) && splitted.get(1).equals(groupByAttribute)) {
                return i;
            }
        }
        return -1;
    }

    private List<Integer> getAvgColumnPositions(List<String> selectAttributes) {
        List<Integer> avgColumnPositions = new ArrayList<>();
        for (int i=0; i<selectAttributes.size(); i++) {
            if (selectAttributes.get(i).contains("AVG")) {
                avgColumnPositions.add(i);
            }
        }
        return avgColumnPositions;
    }

    public List<String> getResults() {
        return results;
    }
}
