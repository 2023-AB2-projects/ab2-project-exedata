package Backend.SaveLoadJSON;

import Backend.Databases.Databases;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;

public class LoadJSON {
    public static Databases load(String jsonFileName) {  //return with null, if file doesn't exists or something went wrong
        ObjectMapper objectMapper = new ObjectMapper();
        Databases databases;
        try{
            databases = objectMapper.readValue(new File(jsonFileName), Databases.class);
        }catch (Exception e){
            return null;
        }
        return databases;
    }
}
