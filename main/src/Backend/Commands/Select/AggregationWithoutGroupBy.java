package Backend.Commands.Select;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AggregationWithoutGroupBy {
    private SelectManager selectManager;
    private List<String> finalResults;
    List<String> allAttributes;

    public AggregationWithoutGroupBy(SelectManager selectManager, List<String> currentResults) {
        this.selectManager = selectManager;
        allAttributes = List.of(currentResults.get(0).split("#"));
        // guaranteed that only one aggregation function is here
        String selectAttribute = selectManager.getSelect().get(0);
        Pattern pattern = Pattern.compile("\\((.*?)\\)");
        Matcher matcher = pattern.matcher(selectAttribute);

        currentResults.remove(0);

        finalResults = new ArrayList<>();

        if (selectManager.getSelectAS().get(0) == null) {
            finalResults.add(selectManager.getSelect().get(0));
        } else {
            finalResults.add(selectManager.getSelectAS().get(0) + "." + selectManager.getSelect().get(0));
        }

        if (matcher.find()) {
            String tableAndAttributeName = matcher.group(1);
            int aggregationAttributePosition = getAttributePosition(tableAndAttributeName);

            double avg = 0;
            double sum = 0;
            int count = 0;
            double min = Double.MAX_VALUE;
            double max = Double.MIN_VALUE;
            if (selectAttribute.toUpperCase().contains("AVG")) {
                for (String item : currentResults) {
                    String[] columns = item.split("#");
                    count++;
                    avg += Double.parseDouble(columns[aggregationAttributePosition]);
                }
                finalResults.add(String.valueOf(avg / count));
            } else if (selectAttribute.toUpperCase().contains("SUM")) {
                for (String item : currentResults) {
                    String[] columns = item.split("#");
                    sum += Double.parseDouble(columns[aggregationAttributePosition]);
                }
                finalResults.add(String.valueOf(sum));
            } else if (selectAttribute.toUpperCase().contains("COUNT")) {
                finalResults.add(String.valueOf(currentResults.size()));
            } else if (selectAttribute.toUpperCase().contains("MIN")) {
                for (String item : currentResults) {
                    String[] columns = item.split("#");
                    double value = Double.parseDouble(columns[aggregationAttributePosition]);
                    if (value < min) {
                        min = value;
                    }
                }
                finalResults.add(String.valueOf(min));
            } else if (selectAttribute.toUpperCase().contains("MAX")) {
                for (String item : currentResults) {
                    String[] columns = item.split("#");
                    double value = Double.parseDouble(columns[aggregationAttributePosition]);
                    if (value > max) {
                        max = value;
                    }
                }
                finalResults.add(String.valueOf(max));
            }
        }
    }

    private int getAttributePosition(String attribute) {
        String[] splitGroupByAttribute = attribute.split("\\.", 2);
        for (int i = 0; i < allAttributes.size(); i++) {
            String[] splitAttribute = allAttributes.get(i).split("\\.", 2);
            //splitGroupByAttribute[0].equals(splitAttribute[0]) &&
            if (!attribute.contains(".")) {
                if (splitGroupByAttribute[0].equals(splitAttribute[1])) {
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

    public List<String> getFinalResults() {
        return finalResults;
    }
}
