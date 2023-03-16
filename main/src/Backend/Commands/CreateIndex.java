package Backend.Commands;
import java.io.FileWriter;
import java.io.IOException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class CreateIndex implements Command {

    private String command;

    public CreateIndex(String command) {
        this.command = command;
    }

    @Override
    public void performAction() {
        

    }
}
