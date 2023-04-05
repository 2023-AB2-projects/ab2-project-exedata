package Frontend;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import javax.swing.table.*;
import java.awt.*;

public class ScrollableJTable extends JPanel {
    private JTable table;
    private JScrollPane pane;

    public ScrollableJTable() {

        setLayout(new BorderLayout());

        table = new JTable(200, 5);

        // Turn off JTable's auto resize so that JScrollPane will show a horizontal
        // scroll bar.
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        pane = new JScrollPane(table);
        add(pane, BorderLayout.CENTER);
    }

    public void setHeader(String[] attributes) {
        JTableHeader jTableHeader = new JTableHeader(table.getColumnModel());
        for (int i = 0; i < jTableHeader.getColumnModel().getColumnCount(); i++) {
            jTableHeader.getColumnModel().getColumn(i).setHeaderValue(attributes[i]);
        }
        table.setTableHeader(jTableHeader);
    }

    public Object getValueAt(int row, int column) {
        return table.getValueAt(row, column);
    }

    public int getColumnCount() {
        return table.getColumnCount();
    }

    public void setModel(DefaultTableModel defaultTableModel) {
        table.setModel(defaultTableModel);

    }

    public TableModel getModel() {
        return table.getModel();
    }

    public void setValueAt(String value, int row, int column) {
        table.setValueAt(value, row, column);
    }

    public void setBorder(MatteBorder matteBorder) {
        table.setBorder(matteBorder);
    }
}
