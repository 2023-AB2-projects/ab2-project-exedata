package Backend.Commands.Select;

import Backend.Commands.Condition;
import org.bson.Document;

import java.util.List;

public class Join {
    private SelectManager selectManager;
    private List<String> tables;
    private Selection tableSelectResultsSeparately;
    private List<Condition> joinConditions;
    private List<org.bson.Document> joinResult;
    public Join(SelectManager selectManager) {
        this.selectManager = selectManager;
        tables = selectManager.getFrom();
        tableSelectResultsSeparately = new Selection(selectManager);
        joinConditions = selectManager.getJoin();
        joinResult = tableSelectResultsSeparately.processing(0);
        System.out.println(joinResult);


    }

    public List<Document> getJoinResult() {
        return joinResult;
    }
}
