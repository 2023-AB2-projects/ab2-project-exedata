package Backend.Commands;

import Backend.Databases.Attribute;
import org.bson.Document;

import java.util.List;

public class FormatCommand {
    private static int[] mappingArray;

    public static String formatCommand(String command) {
        //torolni az elejerol a " "
        //torolni a vegerol a " "
        //torolni a vegerol a ";"
        //torolni a vegerol a ")"

        while (command.charAt(0) == ' ') {
            command = command.substring(1);
        }

        if (command.charAt(command.length() - 1) == ';') {
            command = command.substring(0, command.length() - 1);
        }

        if (command.charAt(command.length() - 1) == ')') {
            command = command.substring(0, command.length() - 1);
        }

        while (command.charAt(command.length() - 1) == ' ') {
            command = command.substring(0, command.length() - 1);

            if (command.charAt(command.length() - 1) == ';') {
                command = command.substring(0, command.length() - 1);
            }

//            if (command.charAt(command.length() - 1) == ')') {
//                command = command.substring(0, command.length() - 1);
//            }
        }
        return command;
    }

    protected static String formatWords(String word) {
        String result = word;
        if (result.charAt(0) == '(') {
            result = result.substring(1);
        }
        if (result.charAt(result.length() - 1) == ';') {
            result = result.substring(0, result.length() - 1);
        }
        if (result.charAt(result.length() - 1) == ')') {
            result = result.substring(0, result.length() - 1);
        }
        if (result.charAt(result.length() - 1) == ',') {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    public static void setMappingArray(List<Attribute> allAttribute, List<String> primaryKeys) {
        mappingArray = new int[allAttribute.size()];
        for (int i = 0; i < allAttribute.size(); i++) {
            if (primaryKeys.contains(allAttribute.get(i).getName()))
                mappingArray[i] = 1;
            else
                mappingArray[i] = 0;
        }
    }

    public static String[] separetaByHasthag(Document document, int length) {
        String[] result = new String[length];
        String[] primaryKey = ((String) document.get("_id")).split("#");
        String[] attribute = ((String) document.get("Value")).split("#");
        int primaryKeyIndex=0;
        int attributeIndex=0;
        for (int i = 0; i < length; i++) {
            if(mappingArray[i]==0){
                result[i]=attribute[attributeIndex];
                attributeIndex++;
            }else{
                result[i]=primaryKey[primaryKeyIndex];
                primaryKeyIndex++;
            }
        }
        return result;
    }

    public static String getPrimaryKeysValuesSeparateByHash(List<String> primaryKeys, List<String> fieldName, List<String> value) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < fieldName.size(); i++) {
            if (primaryKeys.contains(fieldName.get(i))) {
                result.append(value.get(i)).append("#");
            }
        }
        result = new StringBuilder(result.substring(0, result.length() - 1));
        return result.toString();
    }
}
