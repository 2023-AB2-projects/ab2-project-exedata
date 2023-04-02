package Frontend;

import Backend.Backend;
import Backend.Databases.Attribute;
import Backend.Databases.Databases;
import Backend.Databases.Table;
import Backend.Parser;
import Backend.SaveLoadJSON.LoadJSON;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class InsertDeleteQuery extends JPanel {
    private JPanel header;
    private JPanel center;
    private JTable table;
    public InsertDeleteQuery() {
//        this.setLayout(new BorderLayout());
//        header = new JPanel();
//        center = new JPanel();
//
//        Parser.currentDatabaseName = "University";
//        List<Table> tables = getAllTables();
//        Object[][] data = {
//                {tables.get(0).getName(), "2", "3"},
//                {"4", "5", "6"},
//                {"7", "8", "9"}
//        };
//        String[] columnHeaders = {"Column 1", "Column 2", "Column 3"};
//        JTable table = new JTable(data, columnHeaders);
//
//        header.add(new JLabel("szia"));
//        center.add(table);
//        this.add(header, BorderLayout.NORTH);
//        this.add(center, BorderLayout.CENTER);
    }

    public List<Table> getAllTables() {
        Databases databases = LoadJSON.load("databases.json");
        assert databases != null;
        return databases.getDatabase(Parser.currentDatabaseName).getTables();
    }

}
