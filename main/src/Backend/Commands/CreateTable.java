package Backend.Commands;
import java.io.FileWriter;
import java.io.IOException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class CreateTable implements Command {
    // create table in a certain database with primary key, foreign key, attributes, null value, default value, constraints, ...
    private String command;

    public CreateTable(String command) {
        this.command = command;
    }

    @Override
    public void performAction() {

    }
}
