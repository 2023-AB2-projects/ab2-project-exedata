package Frontend;

import Backend.Databases.Attribute;
import Backend.Databases.Database;
import Backend.Databases.Databases;
import Backend.Databases.Table;
import Backend.Parser;
import Backend.SaveLoadJSON.LoadJSON;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

public class InsertDeleteQuery extends JPanel {
    private JPanel header;
    private JPanel center;
    private JPanel centerUp;
    private JComboBox databaseComboBox;
    private JComboBox tableComboBox;
    private JPanel centerDown;
    private JTable table;
    // private DefaultTableModel defaultTableModel;
    private JButton insertButton;
    private JButton deleteButton;
    private String[] allDatabases;
    private String[] allTables;
    private String[] allAttributes;
    private ClientConnection clientConnectionInsertDelete;
    Databases d;

    public InsertDeleteQuery() {
        this.setLayout(new BorderLayout());
        header = new JPanel();
        center = new JPanel();

        Databases databases = LoadJSON.load("databases.json");
        if (databases == null) {

        } else {
            clientConnectionInsertDelete = new ClientConnection();
            d = LoadJSON.load("databases.json");
            Parser.currentDatabaseName = databases.getDatabaseList().get(0).getName();
            Parser.currentTableName = databases.getDatabase(Parser.currentDatabaseName).getTables().get(0).getName();
            sendUse();

            allDatabases = getAllDatabases();
            allTables = getAllTables();

            table = new JTable();

            table.setBorder(new MatteBorder(1, 1, 1, 1, Color.black));
            table.setBackground(new Color(171, 173, 182));

            fillAttributesInTable();

            insertButton = new JButton("Insert");
            insertButton.setBackground(new Color(0, 170, 0));
            insertButton.setForeground(Color.white);
            insertButton.setBorder(new MatteBorder(3, 3, 3, 3, Color.yellow));
            deleteButton = new JButton("Delete");
            deleteButton.setBackground(new Color(255, 62, 63));
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

            center.add(centerUp);

            //centerDown
            //=====================================================================================================
            centerDown.add(table);
            centerDown.setBackground(new Color(102, 178, 255));
            center.add(centerDown, BorderLayout.SOUTH);

            this.add(header, BorderLayout.NORTH);
            this.add(center, BorderLayout.CENTER);

            databaseComboBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Parser.currentDatabaseName = (String) databaseComboBox.getSelectedItem();
                    Parser.currentTableName = d.getDatabase(Parser.currentDatabaseName).getTables().get(0).getName();
                    sendUse();
                    try {
                        ClientConnection.send("USE " + Parser.currentDatabaseName);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    DefaultComboBoxModel<String> tableComboBoxModel = new DefaultComboBoxModel<>(getAllTables());
                    tableComboBox.setModel(tableComboBoxModel);
                    fillAttributesInTable();
                }
            });

            tableComboBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Parser.currentTableName = (String) tableComboBox.getSelectedItem();
                    fillAttributesInTable();
                }
            });

            insertButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String column = convertColumnToSendFormat(getAllAttributes());
                    String contentOfRows = getContentOfTable();
                    //System.out.println("INSERT INTO " + Parser.currentTableName + " (" + column + ") VALUES (" + contentOfRows + ");");
                    try {
                        ClientConnection.send("INSERT INTO " + Parser.currentTableName + " (" + column + ") VALUES (" + contentOfRows + ");");
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            });

            deleteButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    //delete from marks where StudID = 50 and DiscID = 'OOP';
                    String condition = getDeleteCondition();
//                    System.out.println(condition);
                    try {
                        ClientConnection.send("DELETE FROM " + Parser.currentTableName + " WHERE "+condition+";");
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            });
        }
    }

    public String getDeleteCondition() {
        String[] conditon = new String[table.getColumnCount()];
        int j = 0;
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (table.getValueAt(1, i) != null && !((String)table.getValueAt(1, i)).equals("")) {
                conditon[j] = (String) table.getValueAt(0, i) + " = " + getDeleteAttribute(typeOfAttribute((String) table.getValueAt(0, i)), (String) table.getValueAt(1, i));
                j++;
            }
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < j; i++) {
            result.append(" AND ").append(conditon[i]);
        }
        return result.substring(5);
    }

    public String getDeleteAttribute(String type, String value) {
        return switch (type) {
            case "INT", "FLOAT", "BIT" -> value;
            default -> "'" + value + "'";
        };
    }

    public String getContentOfTable() {
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (table.getValueAt(1, i) != null)
                content.append(", ").append((String) table.getValueAt(1, i));
        }
        return content.substring(2);
    }

    public String convertColumnToSendFormat(String[] columns) {
        StringBuilder column = new StringBuilder();
        for (int i = 0; i < columns.length; i++) {
            if (table.getValueAt(1, i) != null)
                column.append(", ").append(columns[i]);
        }
        return column.substring(2);
    }

    public String[] getAllDatabases() {
        List<Database> databases = d.getDatabaseList();
        String[] list = new String[databases.size()];
        for (int i = 0; i < databases.size(); i++) {
            list[i] = databases.get(i).getName();
        }
        return list;
    }

    public String[] getAllTables() {
        List<Table> tables = d.getDatabase(Parser.currentDatabaseName).getTables();
        String[] list = new String[tables.size()];
        for (int i = 0; i < tables.size(); i++) {
            list[i] = tables.get(i).getName();
        }
        return list;
    }

    public String[] getAllAttributes() {
        List<Attribute> attributes = d.getDatabase(Parser.currentDatabaseName).getTable(Parser.currentTableName).getStructure();
        String[] list = new String[attributes.size()];
        for (int i = 0; i < attributes.size(); i++) {
            list[i] = attributes.get(i).getName();
        }
        return list;
    }

    public String typeOfAttribute(String attributeName) {
        return d.getDatabase(Parser.currentDatabaseName).getTable(Parser.currentTableName).getAttribute(attributeName).getType();
    }

    public void fillAttributesInTable() {
        TableModel tableModel = table.getModel();
        String[] attributes = getAllAttributes();
        DefaultTableModel defaultTableModel = new DefaultTableModel(2, attributes.length);
        table.setModel(defaultTableModel);
        for (int i = 0; i < attributes.length; i++) {
            table.setValueAt(attributes[i], 0, i);
        }
    }

    public void sendUse() {
        try {
            ClientConnection.send("USE " + Parser.currentDatabaseName + ";");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
