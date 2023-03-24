package Backend.SaveLoadJSON;

import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.io.IOException;

public class SaveJSON {
    public static void save(JSONObject a, String jsonFileName) {
        try (FileWriter fileWriter = new FileWriter(jsonFileName)) {
            fileWriter.write(a.toJSONString());
            fileWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
