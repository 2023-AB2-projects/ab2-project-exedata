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

    public SelectQuery() {
        databases = LoadJSON.load("databases.json");
        if (databases == null || databases.getDatabaseList().size() == 0) {

        } else {
            this.currentDatabaseName = Parser.currentDatabaseName;
            this.currentTableName = Parser.currentTableName;
            this.setLayout(new GridBagLayout());

            // Init constraints to GridBagLayout
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1;
            gbc.weighty = 0.05;
            gbc.fill = GridBagConstraints.BOTH;

            GridBagConstraints gbc2 = new GridBagConstraints();
            gbc2.gridx = 0;
            gbc2.gridy = 1;
            gbc2.weightx = 1;
            gbc2.weighty = 0.95;
            gbc2.fill = GridBagConstraints.BOTH;

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

            this.add(centerUp, gbc);

            // Center
            center.setLayout(null);
            tableBoxes = new ArrayList<>();
            this.add(center, gbc2);
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
