package Backend.Commands.Select;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GroupBy {
    private SelectManager selectManager;
    List<String> results;
    List<String> finalResults;
    List<String> allAttributes;

    public GroupBy(SelectManager selectManager, List<String> currentResults) {
        this.selectManager = selectManager;
        String groupByAttribute = selectManager.getTableNameOfGroupByAttribute().get(0) + "." + selectManager.getGroupBy().get(0);
        List<String> selectAttributes = selectManager.getSelect();
        List<String> selectASAttributes = selectManager.getSelectAS();
        List<String> tableNameOfGroupByAttributes = selectManager.getTableNameOfGroupByAttribute();
        List<String> tableNameOfSelectByAttributes = selectManager.getTableNameOfSelectAttribute();

        Selection selection = new Selection(selectManager);
        List<String> tableData = currentResults;
        allAttributes = List.of(currentResults.get(0).split("#"));

//        SELECT d.DName, AVG(d.CreditNr)
//        FROM disciplines d
//        GROUP BY d.DName
//        SELECT marks.DiscID, AVG(marks.Mark), SUM(marks.Mark)
//        FROM disciplines d INNER JOIN marks m ON d.DiscID=m.DiscID
//        GROUP BY m.DiscID
        results = new ArrayList<>();
        StringBuilder attributeResults = new StringBuilder();
        for (int i = 0; i < selectAttributes.size(); i++) {
            if (selectASAttributes.get(i) != null) {
                attributeResults.append(tableNameOfSelectByAttributes.get(i)).append(".").append(selectASAttributes.get(i)).append("#");
            } else {
                attributeResults.append(tableNameOfSelectByAttributes.get(i)).append(".").append(selectAttributes.get(i)).append("#");
            }
        }
        attributeResults = new StringBuilder(attributeResults.substring(0, attributeResults.length() - 1));

        // go through table elements and apply group by condition
        currentResults.remove(0);

        int groupByAttributePos = getAttributePosition(groupByAttribute);

        List<Integer> avgColumnPos = getColumnPositions(selectAttributes, "AVG");
        List<Integer> sumColumnPos = getColumnPositions(selectAttributes, "SUM");
        List<Integer> countColumnPos = getColumnPositions(selectAttributes, "COUNT");
        List<Integer> minColumnPos = getColumnPositions(selectAttributes, "MIN");
        List<Integer> maxColumnPos = getColumnPositions(selectAttributes, "MAX");

        Map<String, List<Integer>> countMap = new HashMap<>();
        Map<String, List<Integer>> countMapAvg = new HashMap<>();
        Map<String, List<Double>> sumMap = new HashMap<>();
        Map<String, List<Double>> avgMap = new HashMap<>();
        Map<String, List<Double>> minMap = new HashMap<>();
        Map<String, List<Double>> maxMap = new HashMap<>();

        Set<String> uniqueStrings = new HashSet<>();
        for (int i = 0; i < tableData.size(); i++) {
            String item = tableData.get(i);
            String[] columns = item.split("#");
            String columnAttributeValue = columns[groupByAttributePos];

            //===========================================================================================
            // sumMap
            if (!sumMap.containsKey(columnAttributeValue)) {
                sumMap.put(columnAttributeValue, new ArrayList<>(Collections.nCopies(sumColumnPos.size(), 0.0)));
            }
            List<Double> existingValuesSum = sumMap.get(columnAttributeValue);
            for (int j = 0; j < sumColumnPos.size(); j++) {
                double columnValue = Double.parseDouble(columns[sumColumnPos.get(j)]);
                double updatedValue = existingValuesSum.get(j) + columnValue;
                existingValuesSum.set(j, updatedValue);
            }

            //===========================================================================================
            // minMap
            if (!minMap.containsKey(columnAttributeValue)) {
                minMap.put(columnAttributeValue, new ArrayList<>(Collections.nCopies(minColumnPos.size(), Double.MAX_VALUE)));
            }
            List<Double> existingValuesMin = minMap.get(columnAttributeValue);
            for (int j = 0; j < minColumnPos.size(); j++) {
                double columnValue = Double.parseDouble(columns[minColumnPos.get(j)]);
                if (columnValue < existingValuesMin.get(j)) {
                    existingValuesMin.set(j, columnValue);
                }
            }

            //===========================================================================================
            // maxMap
            if (!maxMap.containsKey(columnAttributeValue)) {
                maxMap.put(columnAttributeValue, new ArrayList<>(Collections.nCopies(maxColumnPos.size(), Double.MIN_VALUE)));
            }
            List<Double> existingValuesMax = maxMap.get(columnAttributeValue);
            for (int j = 0; j < maxColumnPos.size(); j++) {
                double columnValue = Double.parseDouble(columns[maxColumnPos.get(j)]);
                if (columnValue > existingValuesMax.get(j)) {
                    existingValuesMax.set(j, columnValue);
                }
            }

            //===========================================================================================
            // countMap
            if (!countMap.containsKey(columnAttributeValue)) {
                countMap.put(columnAttributeValue, new ArrayList<>(Collections.nCopies(countColumnPos.size(), 0)));
            }
            List<Integer> existingValuesCount = countMap.get(columnAttributeValue);
            for (int j = 0; j < countColumnPos.size(); j++) {
                int updatedValue = existingValuesCount.get(j) + 1;
                existingValuesCount.set(j, updatedValue);
            }

            //===========================================================================================
            // avgMap
            if (!avgMap.containsKey(columnAttributeValue)) {
                avgMap.put(columnAttributeValue, new ArrayList<>(Collections.nCopies(avgColumnPos.size(), 0.0)));
            }
            if (!countMapAvg.containsKey(columnAttributeValue)) {
                countMapAvg.put(columnAttributeValue, new ArrayList<>(Collections.nCopies(avgColumnPos.size(), 0)));
            }
            List<Double> existingValuesAvg = avgMap.get(columnAttributeValue);
            List<Integer> existingValuesAvgCount = countMapAvg.get(columnAttributeValue);

            for (int j = 0; j < avgColumnPos.size(); j++) {
                double columnValue = Double.parseDouble(columns[avgColumnPos.get(j)]);
                double updatedSum = existingValuesAvg.get(j) + columnValue;
                existingValuesAvgCount.set(j, existingValuesAvgCount.get(j) + 1);
                existingValuesAvg.set(j, updatedSum);
            }

            uniqueStrings.add(columnAttributeValue);
        }

        for (String i : uniqueStrings) {
            List<Double> existingValuesAvg = avgMap.get(i);
            List<Integer> existingValuesAvgCount = countMapAvg.get(i);
            for (int j = 0; j < avgColumnPos.size(); j++) {
                existingValuesAvg.set(j, existingValuesAvg.get(j) / existingValuesAvgCount.get(j));
            }
        }

        results.addAll(uniqueStrings);

        // BUILDING RESPONSE
        int sumCount;
        int avgCount;
        int countCount;
        int minCount;
        int maxCount;

        finalResults = new ArrayList<>();
        finalResults.add(attributeResults.toString());
        for (String groupByAttrValue : results) {
            StringBuilder stringBuilder = new StringBuilder();
            sumCount = 0;
            avgCount = 0;
            countCount = 0;
            minCount = 0;
            maxCount = 0;
            for (int j = 0; j < selectAttributes.size(); j++) {
                if ((tableNameOfSelectByAttributes.get(j) + "." + selectAttributes.get(j)).contains(groupByAttribute)) {
                    stringBuilder.append(groupByAttrValue).append("#");
                } else if (selectAttributes.get(j).toUpperCase().contains("SUM")) {
                    stringBuilder.append(sumMap.get(groupByAttrValue).get(sumCount)).append("#");
                    sumCount++;
                } else if (selectAttributes.get(j).toUpperCase().contains("COUNT")) {
                    stringBuilder.append(countMap.get(groupByAttrValue).get(countCount)).append("#");
                    countCount++;
                } else if (selectAttributes.get(j).toUpperCase().contains("AVG")) {
                    stringBuilder.append(avgMap.get(groupByAttrValue).get(avgCount)).append("#");
                    avgCount++;
                } else if (selectAttributes.get(j).toUpperCase().contains("MIN")) {
                    stringBuilder.append(minMap.get(groupByAttrValue).get(minCount)).append("#");
                    minCount++;
                } else if (selectAttributes.get(j).toUpperCase().contains("MAX")) {
                    stringBuilder.append(maxMap.get(groupByAttrValue).get(maxCount)).append("#");
                    maxCount++;
                }
            }
            finalResults.add(stringBuilder.toString());
        }
    }

    private int getAttributePosition(String attribute) {
        String[] splitGroupByAttribute = attribute.split("\\.", 2);
        for (int i = 0; i < allAttributes.size(); i++) {
            String[] splitAttribute = allAttributes.get(i).split("\\.", 2);
            //splitGroupByAttribute[0].equals(splitAttribute[0]) &&
            if (!attribute.contains(".")) {
                if (splitGroupByAttribute[0].equals(splitAttribute[1]) || splitAttribute[1].equals("*")) {
                    return i;
                }
            } else {
                String splitGroupByAttributeTableName = splitGroupByAttribute[0];
                for (int j = 0; j < selectManager.getFromAS().size(); j++) {
                    if (selectManager.getFromAS().get(j) != null && selectManager.getFromAS().get(j).equals(splitGroupByAttribute[0])) {
                        splitGroupByAttributeTableName = selectManager.getFrom().get(j);
                    }
                }
                if (splitGroupByAttributeTableName.equals(splitAttribute[0]) && splitGroupByAttribute[1].equals(splitAttribute[1])) {
                    return i;
                }
            }
        }
        return -1;
    }

    private List<Integer> getColumnPositions(List<String> selectAttributes, String aggregationKeyWord) {
        List<Integer> avgColumnPositions = new ArrayList<>();
        for (int i = 0; i < selectAttributes.size(); i++) {
            if (selectAttributes.get(i).toUpperCase().contains(aggregationKeyWord)) {
                Pattern pattern = Pattern.compile("\\((.*?)\\)");
                Matcher matcher = pattern.matcher(selectAttributes.get(i));
                if (matcher.find()) {
                    String attributeName = matcher.group(1);
                    List<String> splitted = List.of(attributeName.split("\\."));
                    avgColumnPositions.add(getAttributePosition(attributeName));
                }
            }
        }
        return avgColumnPositions;
    }

    public List<String> getFinalResults() {
        return finalResults;
    }
}
