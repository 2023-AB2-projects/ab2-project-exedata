package Backend.Commands;

import org.json.simple.JSONObject;

public class JSONFile {
    private JSONObject databases;

    public JSONFile() {
        databases = null;
    }

    public JSONObject getDatabases() {
        return databases;
    }

    public void setDatabases(JSONObject databases) {
        this.databases = databases;
    }
}
