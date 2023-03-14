package Backend.Commands;
import java.io.FileWriter;
import java.io.IOException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class CreateDatabase implements Command {
    // create a database with name in json file

    @Override
    public void performAction() {
        JSONObject database = new JSONObject();
        database.put("Databases", "Tables");

        JSONArray databaseList = new JSONArray();
        databaseList.add(database);

        try (FileWriter file = new FileWriter("databases.json")) {
            file.write(database.toJSONString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
