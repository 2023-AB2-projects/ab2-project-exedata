package Backend.Commands.Select;

import Backend.Databases.Attribute;
import Backend.Parser;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static Backend.SocketServer.Server.databases;

public class Projection {
    private final SelectManager selectManager;

    public Projection(SelectManager selectManager) {
        this.selectManager = selectManager;
    }

    public List<String> getResult(List<String> values) {
        List<String> result = new ArrayList<>();
        StringBuilder columns = new StringBuilder();
        StringBuilder withoutAliasColumns = new StringBuilder();
        List<String> select = selectManager.getSelect();
        List<String> selectAS = selectManager.getSelectAS();
        for (int i = 0; i < select.size(); i++) {
            if (selectAS.get(i) != null) {
                columns.append(selectAS.get(i)).append("#");
                withoutAliasColumns.append(selectManager.getTableNameOfSelectAttribute().get(i)).append(".").append(select.get(i)).append("#");
            } else {
                columns.append(selectManager.getTableNameOfSelectAttribute().get(i)).append(".").append(select.get(i)).append("#");
                withoutAliasColumns.append(selectManager.getTableNameOfSelectAttribute().get(i)).append(".").append(select.get(i)).append("#");
            }
        }
        result.add(columns.substring(0, columns.length() - 1));
        int[] indexArray = createIndexArray(withoutAliasColumns.substring(0, withoutAliasColumns.length() - 1), values.get(0));
        //id nev email
        //id nev kor email
        //-1 0 2
        System.out.println(Arrays.toString(indexArray));
        for (int i = 1; i < values.size(); i++) {
            result.add(getSelectedAttribute(values.get(i), indexArray));
        }
        return result;
    }

    private String getSelectedAttribute(String value, int[] indexArray) {
        List<String> values = List.of(value.split("#"));
        StringBuilder result = new StringBuilder();
        for (int i : indexArray) {
            result.append(values.get(i)).append("#");
        }
        return result.substring(0, result.length() - 1);
    }

    private int[] createIndexArray(String columns, String allColumns) {
        List<String> columnsList = List.of(columns.split("#"));
        List<String> allColumnsList = List.of(allColumns.split("#"));
        int[] result = new int[columnsList.size()];
        //id nev email
        //id nev kor email
        //-1 0 2
        int index;
        for (int i = 0; i < columnsList.size(); i++) {
            index = allColumnsList.indexOf(columnsList.get(i));
            if (index != -1) {
                result[i] = index;
            }
        }
        return result;
    }
}
