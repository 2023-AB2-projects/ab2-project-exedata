package Frontend.SelectPanel;

import javax.swing.*;
import java.awt.*;

public class SelectQuery extends JPanel {
    private TableBox tableBox;
    protected int width = this.getWidth();
    protected int height = this.getWidth();
    public SelectQuery() {
        this.setLayout(null);
        tableBox = new TableBox();
        this.add(tableBox);
    }

}
