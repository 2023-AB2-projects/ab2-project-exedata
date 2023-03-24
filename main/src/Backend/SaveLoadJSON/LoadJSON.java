package Backend.SaveLoadJSON;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class LoadJSON {
    public static JSONObject load(String jsonFileName) {  //return with null, if file doesn't exists or something went wrong
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = null;
        String content;
        try {
            content = Files.readString(Path.of(jsonFileName));
        } catch (IOException e) {
            System.out.println("JSON file doesnt exists!");
            return null;
        }
        try {
            jsonObject = (JSONObject) parser.parse(content);
        } catch (ParseException e) {
            System.out.println("Read from JSON file ERROR!");
        }
        return jsonObject;
    }
}
