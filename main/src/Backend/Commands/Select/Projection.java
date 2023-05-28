package Backend.Commands.Select;

import Backend.Databases.Attribute;
import Backend.Parser;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

import static Backend.SocketServer.Server.databases;

public class Projection {
    public Projection() {
    }

    public List<String> projectionProcessing(List<String> values, SelectManager selectManager) {
        List<String> result = new ArrayList<>();
        StringBuilder columns = new StringBuilder();
        List<String> select = selectManager.getSelect();
        List<String> selectAS = selectManager.getSelectAS();
        for (int i = 0; i < select.size(); i++) {
            if (selectAS.get(i) != null)
                columns.append(selectAS.get(i)).append("#");
            else
                columns.append(select.get(i)).append("#");
        }
        result.add(columns.substring(0, columns.length() - 1));
        int[] indexArray = createIndexArray(select, selectManager.getFrom().get(0));
        //id nev email
        //id nev kor email
        //-1 0 2
        for (int i=1; i<values.size(); i++) {
            result.add(getAttributeByIndex(values.get(i)));
        }
        return result;
    }

    private String getAttributeByIndex(String value) {
        List<String> attributes = List.of(value.split("#"));
        for (int i=0; i<attributes.size(); i++) {
            System.out.println(attributes.get(i));
        }
        return value;

    }

    private String getSelectedAttribute(Document document, int[] indexArray) {
        //List<String> values = List.of(document.split("#"));
        String[] primaryKey = ((String) document.get("_id")).split("#");
        String[] attribute = ((String) document.get("Value")).split("#");
        StringBuilder result = new StringBuilder();
        for (int i : indexArray) {
            if (i < 0) {
                result.append(primaryKey[(i + 1) * (-1)]).append("#");
            } else {
                result.append(attribute[i]).append("#");
            }
        }
        return result.substring(0, result.length() - 1);
    }

    private int[] createIndexArray(List<String> select, String tableName) {
        int[] result = new int[select.size()];
        List<Attribute> attributeList = databases.getDatabase(Parser.currentDatabaseName).getTable(tableName).getStructure();
        List<String> primaryKey = databases.getDatabase(Parser.currentDatabaseName).getTable(tableName).getPrimaryKey();
        //id nev email
        //id nev kor email
        //-1 0 2
        String column;
        int primaryKeyIndex;
        int attributeIndex;
        boolean isPrimaryKey;
        for (int i = 0; i < select.size(); i++) {
            if (select.get(i).contains(".")) {
                column = select.get(i).split("\\.")[1];
            } else {
                column = select.get(i);
            }
            isPrimaryKey = primaryKey.contains(column);
            primaryKeyIndex = 0;
            attributeIndex = 0;
            for (Attribute attribute : attributeList) {
                if (primaryKey.contains(attribute.getName())) {
                    primaryKeyIndex--;
                } else {
                    attributeIndex++;
                }
                if (attribute.getName().equals(column)) {
                    break;
                }
            }
            if (isPrimaryKey) {
                result[i] = primaryKeyIndex;
            } else {
                result[i] = attributeIndex - 1;
            }
        }
        return result;
    }
}
