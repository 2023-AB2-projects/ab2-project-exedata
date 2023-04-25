package Frontend.SelectPanel;

import Backend.Databases.Attribute;
import Backend.Databases.Database;
import Backend.Databases.Databases;
import Backend.Databases.Table;
import Backend.Parser;
import Backend.SaveLoadJSON.LoadJSON;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SelectQuery extends JPanel {
    private ArrayList<TableBox> tableBoxes;
    private final Databases databases;
    private JPanel centerUp;
    private JPanel center;
    private JPanel centerDown;
    private JComboBox databaseComboBox;
    private JComboBox tableComboBox = new JComboBox();
    private String[] allDatabases;
    private String[] allTables;
    private String[] allAttributes;
    protected int width = this.getWidth();
    protected int height = this.getWidth();
    private String currentDatabaseName;
    private String currentTableName;
    private JPanel panel1;
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
            panel1.setLayout(new GridLayout(2, 1));
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
            String[] columnNames = {"Column", "Alias", "Table", "Output", "Sort type", "Sort order", "Filter", "OR"};
            String[][] data = {{"", "", "", "", "", "", "", ""}};
            JTable table = new JTable(data, columnNames);

            JScrollPane scrollPane = new JScrollPane(table);

            center.setLayout(new GridLayout(1, 1));
            center.add(scrollPane);

            panel1.add(center);

            // Center down
            centerDown.setLayout(null);
            tableBoxes = new ArrayList<>();
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
}
