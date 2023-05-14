package Frontend.SelectPanel;

import Backend.Databases.Attribute;
import Backend.Databases.Database;
import Backend.Databases.Databases;
import Backend.Databases.Table;
import Backend.Parser;
import Backend.SaveLoadJSON.LoadJSON;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SelectQuery extends JPanel {
    private ArrayList<TableBox> tableBoxes;
    private final Databases databases;
    private JPanel centerUp;
    private JPanel center;
    private JPanel centerDown;
    private JTextPane selectCommandText;
    private JButton runButton;
    private JButton sendButton;
    private JComboBox databaseComboBox;
    private JComboBox tableComboBox = new JComboBox();
    private String[] allDatabases;
    private String[] allTables;
    private String[] allAttributes;
    private ArrayList<String> leftJoins;
    private ArrayList<String> innerJoins;
    private ArrayList<String> rightJoins;
    protected int width = this.getWidth();
    protected int height = this.getWidth();
    private String currentDatabaseName;
    private String currentTableName;
    private JTable table;
    private JPanel panel1;
    private JPanel panelButtons;
    private JPanel panel2;

    public SelectQuery() {
        databases = LoadJSON.load("databases.json");
        if (databases == null || databases.getDatabaseList().size() == 0) {

        } else {
            this.currentDatabaseName = Parser.currentDatabaseName;
            this.currentTableName = Parser.currentTableName;
            this.setLayout(new GridBagLayout());

            this.setLayout(new GridLayout(2, 1));
            panel1 = new JPanel();
            panel2 = new JPanel();
            panel1.setLayout(new GridLayout(4, 1));
            panel2.setLayout(new GridLayout(1, 1));

            // CenterUp
            allDatabases = getAllDatabases();
            allTables = getAllTables();
            allAttributes = getAllAttributes();
            centerUp = new JPanel();
            center = new JPanel();
            centerDown = new JPanel();

            centerUp.setLayout(new GridLayout(2, 2));
            JLabel label1 = new JLabel("Select your database:");
            JLabel label2 = new JLabel("Select your table:");
            label1.setBorder(new MatteBorder(2, 2, 2, 2, Color.black));
            label2.setBorder(new MatteBorder(2, 2, 2, 2, Color.black));
            label1.setFont(new Font("Courier New", Font.BOLD, 20));
            label2.setFont(new Font("Courier New", Font.BOLD, 20));

            centerUp.add(label1);
            centerUp.setBackground(new Color(153, 153, 0));
            centerUp.setBorder(new MatteBorder(2, 2, 2, 2, Color.black));
            databaseComboBox = new JComboBox(allDatabases);
            centerUp.add(databaseComboBox);
            centerUp.add(label2);
            tableComboBox = new JComboBox(allTables);
            centerUp.add(tableComboBox);

            panel1.add(centerUp);

            // Center
            Object[] columnNames = {"Column", "Alias", "Table", "Sort Type", "Sort Order", "Filter", "OR"};
            Object[][] data = {};
            DefaultTableModel model = new DefaultTableModel(data, columnNames) {
                //                @Override
//                public Class<?> getColumnClass(int column) {
//                    if (column == 3) {
//                        return Boolean.class;
//                    }
//                    return super.getColumnClass(column);
//                }
                @Override
                public boolean isCellEditable(int row, int column) {
                    return column != 0 && column != 2;
                }
            };

            table = new JTable(model);

            // Sort Type
            int columnIndex = table.getColumnModel().getColumnIndex("Sort Type");
            TableColumn comboBoxColumn = table.getColumnModel().getColumn(columnIndex);

            comboBoxColumn.setCellEditor(new DefaultCellEditor(new JComboBox(new Object[]{"Ascending", "Descending", "Unsorted"})));

            // Sort Order
            columnIndex = table.getColumnModel().getColumnIndex("Sort Order");
            comboBoxColumn = table.getColumnModel().getColumn(columnIndex);

            Object[] numbers = new Object[101];
            numbers[0] = "Unsorted";
            for (int i = 1; i <= 100; i++) {
                numbers[i] = String.valueOf(i);
            }
            comboBoxColumn.setCellEditor(new DefaultCellEditor(new JComboBox(numbers)));

            // JTable to JScrollPane
            JScrollPane scrollPane = new JScrollPane(table);

            center.setLayout(new GridLayout(1, 1));
            center.add(scrollPane);

            panel1.add(center);

            // selectCommandPanel
            selectCommandText = new JTextPane();
            selectCommandText.setFont(new Font("Arial", Font.BOLD, 12));
            JScrollPane jScrollPane = new JScrollPane(selectCommandText);
            panel1.add(jScrollPane);

            // run button
            runButton = new JButton("Generate query");
            runButton.setFont(new Font("Arial", Font.BOLD, 20));
            runButton.setBackground(Color.GREEN);
            runButton.setForeground(Color.BLACK);
            runButton.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.black));

            // send button
            sendButton = new JButton("Send query (to command line)");
            sendButton.setFont(new Font("Arial", Font.BOLD, 20));
            sendButton.setBackground(new Color(67, 142, 238));
            sendButton.setForeground(Color.BLACK);
            sendButton.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.black));

            panelButtons = new JPanel();
            panelButtons.setLayout(new GridLayout(1, 2));
            panelButtons.add(runButton);
            panelButtons.add(sendButton);

            panel1.add(panelButtons);

            // Center down
            centerDown.setLayout(null);
            tableBoxes = new ArrayList<>();

            leftJoins = new ArrayList<>();
            innerJoins = new ArrayList<>();
            rightJoins = new ArrayList<>();

            panel2.add(centerDown);

            this.add(panel1);
            this.add(panel2);
        }
    }

    public String[] getAllDatabases() {
        java.util.List<Database> databaseList = databases.getDatabaseList();
        String[] list = new String[databaseList.size()];
        for (int i = 0; i < databaseList.size(); i++) {
            list[i] = databaseList.get(i).getName();
        }
        return list;
    }

    public String[] getAllTables() {
        List<Table> tables = databases.getDatabase(currentDatabaseName).getTables();
        String[] list = new String[tables.size()];
        for (int i = 0; i < tables.size(); i++) {
            list[i] = tables.get(i).getName();
        }
        return list;
    }

    public String[] getAllAttributes() {
        List<Attribute> attributes = databases.getDatabase(currentDatabaseName).getTable(currentTableName).getStructure();
        String[] list = new String[attributes.size()];
        for (int i = 0; i < attributes.size(); i++) {
            list[i] = attributes.get(i).getName();
        }
        return list;
    }

    public JPanel getCenter() {
        return center;
    }

    public JPanel getCenterDown() {
        return centerDown;
    }

    public JTextPane getSelectCommandText() {
        return selectCommandText;
    }

    public JButton getRunButton() {
        return runButton;
    }

    public JButton getSendButton() {
        return sendButton;
    }

    public ArrayList<TableBox> getTableBoxes() {
        return tableBoxes;
    }

    public JComboBox getDatabaseComboBox() {
        return databaseComboBox;
    }

    public JComboBox getTableComboBox() {
        return tableComboBox;
    }

    public String getCurrentDatabaseName() {
        return currentDatabaseName;
    }

    public String getCurrentTableName() {
        return currentTableName;
    }

    public void setCurrentDatabaseName(String currentDatabaseName) {
        this.currentDatabaseName = currentDatabaseName;
    }

    public void setCurrentTableName(String currentTableName) {
        this.currentTableName = currentTableName;
    }

    public JTable getTable() {
        return table;
    }

    public ArrayList<String> getLeftJoins() {
        return leftJoins;
    }

    public ArrayList<String> getInnerJoins() {
        return innerJoins;
    }

    public ArrayList<String> getRightJoins() {
        return rightJoins;
    }

    public void setLeftJoins(ArrayList<String> leftJoins) {
        this.leftJoins = leftJoins;
    }

    public void setInnerJoins(ArrayList<String> innerJoins) {
        this.innerJoins = innerJoins;
    }

    public void setRightJoins(ArrayList<String> rightJoins) {
        this.rightJoins = rightJoins;
    }
}
