package Backend;

import Backend.Databases.Attribute;
import org.bson.Document;

import java.util.List;

public class Common {
    private static boolean isPrimaryKey(String fieldName, List<String> primaryKeyList) {
        for (String primaryKey : primaryKeyList) {
            if (primaryKey.equals(fieldName)) {
                return true;
            }
        }
        return false;
    }

    public static String getValueByAttributeName(Document document, String fieldName,
                                           List<String> primaryKeyList, List<Attribute> attributeList) {
        String[] primaryKeys = ((String) document.get("_id")).split("#");
        String[] values = ((String) document.get("Value")).split("#");
        if (isPrimaryKey(fieldName, primaryKeyList)) {
            for (int i=0; i<primaryKeyList.size(); i++) {
                if (primaryKeyList.get(i).equals(fieldName)) {
                    return primaryKeys[i];
                }
            }
        } else {
            for (int i=primaryKeys.length; i<attributeList.size(); i++) {
                if (attributeList.get(i).getName().equals(fieldName)) {
                    return values[i - primaryKeys.length];
                }
            }
        }
        return null;
    }
}
