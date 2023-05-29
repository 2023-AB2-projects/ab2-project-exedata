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

        List<String> groupBy = selectManager.getGroupBy();
        if (groupBy.size() != 0) {
            GroupBy group = new GroupBy(selectManager, currentResults);
            ErrorClient.send("Select done!");
            System.out.println("Select done!");
            sendData(group.getFinalResults());
            return;
        }
        if (selectManager.getFunction().size() != 0) {
            AggregationWithoutGroupBy aggregationWithoutGroupBy = new AggregationWithoutGroupBy(selectManager, currentResults);
            ErrorClient.send("Select done!");
            System.out.println("Select done!");
            sendData(aggregationWithoutGroupBy.getFinalResults());
            return;
        }
        Projection projection = new Projection(selectManager);
        ErrorClient.send("Select done!");
        System.out.println("Select done!");
        sendData(projection.getResult(currentResults));
    }

    private void sendData(List<String> result) {
        System.out.println("Sending data...");
        for (String i : result) {
            writer.println(i);
        }
        writer.println("null");
        System.out.println("Finished!");
    }
}
