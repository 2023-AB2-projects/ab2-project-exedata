package Backend.Commands;
import java.io.FileWriter;
import java.io.IOException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class DropTable implements Command {
    // drop table from json file
    private String command;

    public DropTable(String command) {
        this.command = command;
    }

    @Override
    public void performAction() {

    }
}
