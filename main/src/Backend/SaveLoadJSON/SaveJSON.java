package Backend.SaveLoadJSON;

import Backend.Databases.Databases;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;

public class SaveJSON {
    public static void save(Databases databases, String jsonFileName) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(new File(jsonFileName), databases);
        } catch(Exception e){
            System.out.println(e);
        }
    }
}
