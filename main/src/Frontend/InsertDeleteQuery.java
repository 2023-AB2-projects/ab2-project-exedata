package Frontend;

import Backend.Databases.Attribute;
import Backend.Databases.Database;
import Backend.Databases.Databases;
import Backend.Databases.Table;
import Backend.Parser;
import Backend.SaveLoadJSON.LoadJSON;
import Backend.SocketServer.ErrorClient;
import org.bson.Document;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

import static Backend.Commands.FormatCommand.setMappingArray;
import static Backend.Commands.FormatCommand.separetaByHasthag;

public class InsertDeleteQuery extends JPanel {
    private JPanel header;
    private JPanel center;
    private JPanel centerUp;
    private JComboBox databaseComboBox;
    private JComboBox tableComboBox;
    private JPanel centerDown;
    private ScrollableJTable table;
    private JButton insertButton;
    private JButton deleteButton;
    private String[] allDatabases;
    private String[] allTables;
    private ClientConnection clientConnectionInsertDelete;
    private Databases databases;
    private int numberOfRows;

    public InsertDeleteQuery() {
        this.setLayout(new BorderLayout());
        header = new JPanel();
        center = new JPanel();

        databases = LoadJSON.load("databases.json");
        if (databases == null) {
        } else {
            clientConnectionInsertDelete = new ClientConnection(12002);
            Parser.currentDatabaseName = databases.getDatabaseList().get(0).getName();
            Parser.currentTableName = databases.getDatabase(Parser.currentDatabaseName).getTables().get(0).getName();
            sendUse();

            allDatabases = getAllDatabases();
            allTables = getAllTables();

            table = new ScrollableJTable();

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
            centerDown.setLayout(new GridLayout(1, 1));
            centerDown.add(table);
            centerDown.setBackground(new Color(102, 178, 255));
            center.add(centerDown, BorderLayout.SOUTH);

            this.add(header, BorderLayout.NORTH);
            this.add(center, BorderLayout.CENTER);

            databaseComboBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (databaseComboBox.getSelectedItem() != null) {
                        Parser.currentDatabaseName = (String) databaseComboBox.getSelectedItem();
                        Parser.currentTableName = databases.getDatabase(Parser.currentDatabaseName).getTables().get(0).getName();
                        sendUse();
                        try {
                            clientConnectionInsertDelete.send("USE " + Parser.currentDatabaseName);
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                        DefaultComboBoxModel<String> tableComboBoxModel = new DefaultComboBoxModel<>(getAllTables());
                        tableComboBox.setModel(tableComboBoxModel);
                        fillAttributesInTable();
                    }
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
                    try {
                        clientConnectionInsertDelete.send("INSERT INTO " + Parser.currentTableName + " (" + column + ") VALUES (" + contentOfRows + ");");
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }

                    fillAttributesInTable();
                }
            });

            deleteButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String[] condition = getDeleteCondition(table.getSelectedRows());
                    for (String i : condition) {
                        try {
                            clientConnectionInsertDelete.send("DELETE FROM " + Parser.currentTableName + " WHERE " + i + ";");
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                    ErrorClient.send(table.getSelectedRows().length + " row deleted!");
                    fillAttributesInTable();
                }
            });
        }
    }

    public String[] getDeleteCondition(int[] selectedRows) {
        String[] result = new String[selectedRows.length];
        int k = 0;
        for (int selectedRow : selectedRows) {
            String[] condition = new String[table.getColumnCount()];
            int j = 0;
            for (int i = 0; i < table.getColumnCount(); i++) {
                if (table.getValueAt(selectedRow, i) != null && !((String) table.getValueAt(selectedRow, i)).equals("")) {
                    condition[j] = table.getHeaderValue(i) + " = " + getDeleteAttribute(typeOfAttribute(table.getHeaderValue(i)), (String) table.getValueAt(selectedRow, i));
                    j++;
                }
            }
            StringBuilder subResult = new StringBuilder();

            for (int i = 0; i < j; i++) {
                subResult.append(" AND ").append(condition[i]);
            }
            result[k] = subResult.substring(5);
            k++;
        }
        return result;
    }

    public void refresh() {
        databases = LoadJSON.load("databases.json");
        if (databases == null) {
        } else {
            Parser.currentDatabaseName = databases.getDatabaseList().get(0).getName();
            Parser.currentTableName = databases.getDatabase(Parser.currentDatabaseName).getTables().get(0).getName();
            sendUse();

            allDatabases = getAllDatabases();
            allTables = getAllTables();
            databaseComboBox.setModel(new DefaultComboBoxModel<String>(allDatabases));
            tableComboBox.setModel(new DefaultComboBoxModel<String>(allTables));
            fillAttributesInTable();
        }
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
            if (table.getValueAt(numberOfRows, i) != null)
                content.append(", ").append((String) table.getValueAt(numberOfRows, i));
        }
        return content.substring(2);
    }

    public String convertColumnToSendFormat(String[] columns) {
        StringBuilder column = new StringBuilder();
        for (int i = 0; i < columns.length; i++) {
            if (table.getValueAt(numberOfRows, i) != null)
                column.append(", ").append(columns[i]);
        }
        return column.substring(2);
    }

    public String[] getAllDatabases() {
        List<Database> databaseList = databases.getDatabaseList();
        String[] list = new String[databaseList.size()];
        for (int i = 0; i < databaseList.size(); i++) {
            list[i] = databaseList.get(i).getName();
        }
        return list;
    }

    public String[] getAllTables() {
        List<Table> tables = databases.getDatabase(Parser.currentDatabaseName).getTables();
        String[] list = new String[tables.size()];
        for (int i = 0; i < tables.size(); i++) {
            list[i] = tables.get(i).getName();
        }
        return list;
    }

    public String[] getAllAttributes() {
        List<Attribute> attributes = databases.getDatabase(Parser.currentDatabaseName).getTable(Parser.currentTableName).getStructure();
        String[] list = new String[attributes.size()];
        for (int i = 0; i < attributes.size(); i++) {
            list[i] = attributes.get(i).getName();
        }
        return list;
    }

    public String typeOfAttribute(String attributeName) {
        return databases.getDatabase(Parser.currentDatabaseName).getTable(Parser.currentTableName).getAttribute(attributeName).getType();
    }

    public void fillAttributesInTable() {
        String[] attributes = getAllAttributes();

        List<Document> documents = clientConnectionInsertDelete.getData(Parser.currentDatabaseName, Parser.currentTableName);
        numberOfRows = 0;

        setMappingArray(databases.getDatabase(Parser.currentDatabaseName).getTable(Parser.currentTableName).getStructure(),
                databases.getDatabase(Parser.currentDatabaseName).getTable(Parser.currentTableName).getPrimaryKey());
        DefaultTableModel defaultTableModel = new DefaultTableModel(documents.size() + 1, attributes.length);
        table.setModel(defaultTableModel);
        table.setHeader(attributes);

        for (Document i : documents) {
            table.fillARowWithData(numberOfRows, separetaByHasthag(i, attributes.length));
            numberOfRows++;
        }
    }

    public void sendUse() {
        try {
            clientConnectionInsertDelete.send("USE " + Parser.currentDatabaseName + ";");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
