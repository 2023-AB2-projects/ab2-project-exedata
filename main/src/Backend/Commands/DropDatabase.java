package Backend.Commands;
import java.io.FileWriter;
import java.io.IOException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class DropDatabase implements Command {
    // Drop database from json file
    private String command;

    public DropDatabase(String command) {
        this.command = command;
    }

    @Override
    public void performAction() {

    }
}
