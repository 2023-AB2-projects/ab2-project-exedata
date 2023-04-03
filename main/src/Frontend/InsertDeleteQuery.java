package Frontend;

import Backend.Backend;
import Backend.Databases.Attribute;
import Backend.Databases.Database;
import Backend.Databases.Databases;
import Backend.Databases.Table;
import Backend.Parser;
import Backend.SaveLoadJSON.LoadJSON;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.List;

public class InsertDeleteQuery extends JPanel {
    private JPanel header;
    private JPanel center;
    private JPanel centerUp;
    private JComboBox databaseComboBox;
    private JComboBox tableComboBox;
    private JPanel centerDown;
    private JTable table;
    private JButton insertButton;
    private JButton deleteButton;
    private String[] allDatabases;
    private String[] allTables;
    public InsertDeleteQuery() {
        this.setLayout(new BorderLayout());
        header = new JPanel();
        center = new JPanel();
        allDatabases = getAllDatabases();
        allTables = getAllTables();

        Parser.currentDatabaseName = "University";
        String[] tables = getAllTables();
        Object[][] data = {
                {tables[0], "2", "3"},
        };
        String[] columnHeaders = {"Column 1", "Column 2", "Column 3"};
        JTable table = new JTable(data, columnHeaders);
        table.setBorder(new MatteBorder(1, 1, 1, 1, Color.black));
        table.setBackground(new Color(171, 173, 182));

        insertButton = new JButton("Insert");
        insertButton.setBackground(new Color(255, 62, 63));
        insertButton.setForeground(Color.white);
        insertButton.setBorder(new MatteBorder(3, 3, 3, 3, Color.yellow));
        deleteButton = new JButton("Delete");
        deleteButton.setBackground(new Color(0, 170, 0));
        deleteButton.setForeground(Color.white);
        deleteButton.setBorder(new MatteBorder(3, 3, 3, 3, Color.yellow));

        header.setLayout(new GridLayout(1, 2));
        header.add(insertButton);
        header.add(deleteButton);

        //center
        //=====================================================================================================
        center.setLayout(new GridLayout(2, 1));

        centerUp = new JPanel();
        centerDown = new JPanel();

        centerUp.setLayout(new GridLayout(2, 2));
        centerUp.add(new JLabel("Select your database:"));
        databaseComboBox = new JComboBox(allDatabases);
        centerUp.add(databaseComboBox);
        centerUp.add(new JLabel("Select your table:"));
        tableComboBox = new JComboBox(allTables);
        centerUp.add(tableComboBox);

        center.add(centerUp);

        centerDown.add(table);
        center.add(centerDown, BorderLayout.SOUTH);

        this.add(header, BorderLayout.NORTH);
        this.add(center, BorderLayout.CENTER);
    }

    public String[] getAllTables() {
        Databases databases = LoadJSON.load("databases.json");
        assert databases != null;
        List<Table> list = databases.getDatabase(Parser.currentDatabaseName).getTables();
        String[] array = new String[list.size()];
        for (int i=0; i<list.size(); i++) {
            array[i] = list.get(i).getName();
        }
        return array;
    }

    public String[] getAllDatabases() {
        Databases databases = LoadJSON.load("databases.json");
        assert databases != null;
        List<Database> list = databases.getDatabaseList();
        String[] array = new String[list.size()];
        Parser.currentDatabaseName = list.get(0).getName();
        for (int i=0; i<list.size(); i++) {
            array[i] = list.get(i).getName();
        }
        return array;
    }

}
