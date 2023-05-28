package Backend.Commands.Select;

import Backend.Commands.Command;
import Backend.SocketServer.ErrorClient;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.PrintWriter;
import java.util.*;

import static Backend.SocketServer.Server.databases;

public class Select implements Command {
    private final String command;
    private final PrintWriter writer;

    public Select(String command, PrintWriter w) {
        this.command = command;
        writer = w;
    }

    @Override
    public void performAction() throws ParserConfigurationException, TransformerException {
        //SELECT * FROM disciplines WHERE CreditNr > 4 AND DName=Databases1;
        SelectManager selectManager = new SelectManager(command, databases);
        String massage = selectManager.processing();
        if (massage != null) {
            ErrorClient.send(massage);
            System.out.println(massage);
            sendData(new ArrayList<>());
            return;
        }

        Join join = new Join(selectManager); // 0 pos = attributeNames (if has alias, alias)
        List<String> currentResults = join.getJoinResult();
        System.out.println(currentResults.size());

        // sendData(currentResults);
        // here I need to do group by (so I need a format like join.getJoinResults()
        List<String> groupBy = selectManager.getGroupBy();
        if (groupBy.size() != 0) {
            GroupBy group = new GroupBy(selectManager, currentResults);
            sendData(group.getResults());
        } else {
            sendData(currentResults);
        }
    }

    private void sendData(List<String> result) {
        System.out.println("Sending " + (result.size() - 1) + " records...");
        for (String i : result) {
            writer.println(i);
        }
        writer.println("null");
        System.out.println("Finished!");
    }
}
