package Frontend;

import javax.swing.*;
import java.awt.*;

public class ScrollableJTable extends JPanel {

    private void ScrollableJTable() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(500, 250));

        JTable table = new JTable(20, 20);

        // Turn off JTable's auto resize so that JScrollPane will show a horizontal
        // scroll bar.
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JScrollPane pane = new JScrollPane(table);
        add(pane, BorderLayout.CENTER);
    }
}
