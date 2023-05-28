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
//        Projection projection = new Projection();
//        List<String> projectionResults = projection.projectionProcessing(join.getJoinResult(), selectManager);


        List<String> currentResults = join.getJoinResult();

        List<String> groupBy = selectManager.getGroupBy();
        if (groupBy.size() != 0) {
            GroupBy group = new GroupBy(selectManager, currentResults);
            sendData(group.getFinalResults());
        } else {
            boolean basic = true;
            for (int i = 0; i < selectManager.getSelect().size(); i++) {
                if (selectManager.getSelect().get(i).contains("(")) {  // aggregation without group by
                    AggregationWithoutGroupBy aggregationWithoutGroupBy = new AggregationWithoutGroupBy(selectManager, currentResults);
                    sendData(aggregationWithoutGroupBy.getFinalResults());
                    basic = false;
                }
            }
            if (basic) {
                sendData(currentResults);
            }
        }
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
