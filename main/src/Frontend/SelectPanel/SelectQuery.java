package Frontend.SelectPanel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class SelectQuery extends JPanel {
    private ArrayList<TableBox> tableBox;
    protected int width = this.getWidth();
    protected int height = this.getWidth();
    public SelectQuery() {
        this.setLayout(null);
        tableBox = new ArrayList<>();
        tableBox.add(new TableBox());
        this.add(tableBox.get(0));

    }

}
