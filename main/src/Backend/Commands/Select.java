package Backend.Commands;

import Backend.Databases.Databases;
import Backend.SaveLoadJSON.LoadJSON;
import Backend.SocketServer.ErrorClient;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

public class Select implements Command {
    private final String command;
    private Databases databases;

    public Select(String command) {
        this.command = command;
    }

    @Override
    public void performAction() throws ParserConfigurationException, TransformerException {
        databases = LoadJSON.load("databases.json");
        SelectManager selectManager = new SelectManager(command, databases);
        String massage=selectManager.check();
        if(massage!=null) {
            ErrorClient.send(massage);
            System.out.println(massage);
        }
    }
}
