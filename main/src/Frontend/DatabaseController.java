package Frontend;

import Backend.SocketServer.ErrorClient;
import Frontend.SelectPanel.TableBox;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

public class DatabaseController {
    private final DatabaseFrame databaseFrame;
    private final ClientConnection clientConnection;
    private String joinSelectedTable = "-";
    private String joinSelectedAttribute = "-";

    private final String[] keyWords = {"USE", "ADD", "ALL", "ALTER", "ALTER", "TABLE", "AND", "ANY", "AS", "ASC",
            "BACKUP", "BETWEEN", "CASE", "CHECK", "COLUMN", "CONSTRAINT", "CREATE", "DATABASE",
            "INDEX", "REPLACE", "VIEW", "PROCEDURE", "UNIQUE",
            "DEFAULT", "DELETE", "DESC", "DISTINCT", "DROP",
            "EXEC", "EXISTS", "FOREIGN", "KEY", "FROM", "FULL", "OUTER", "JOIN", "GROUP", "BY", "HAVING", "IN",
            "INNER", "INSERT", "INTO", "SELECT", "IS", "NULL", "NOT",
            "LEFT", "LIKE", "LIMIT", "OR", "ORDER", "PRIMARY",
            "PROCEDURE", "RIGHT", "ROWNUM", "TOP",
            "SET", "TRUNCATE", "UNION", "ALL", "UNIQUE", "UPDATE", "VALUES", "WHERE", "ON",
            "CHAR", "VARCHAR", "BIT", "BOOL", "BOOLEAN", "INT", "FLOAT", "DOUBLE", "DATE", "DATETIME", "REAL", "SMALLINT"
    };

    public DatabaseController() {
        clientConnection = new ClientConnection(12000);
        clientConnection.connect(12000);
        databaseFrame = new DatabaseFrame(clientConnection);
        databaseFrame.getPanelTop().getConnect().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clientConnection.connect(12000);
                databaseFrame.getPanelTop().getConnect().setEnabled(false);
                databaseFrame.getPanelTop().getDisconnect().setEnabled(true);
            }
        });
        databaseFrame.getPanelTop().getDisconnect().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clientConnection.disconnect();
                databaseFrame.getPanelTop().getConnect().setEnabled(true);
                databaseFrame.getPanelTop().getDisconnect().setEnabled(false);
            }
        });
        databaseFrame.getPanelCenter().getInputArea().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                JTextPane jTextPane = databaseFrame.getPanelCenter().getInputArea();

                SimpleAttributeSet keyWordStyle = new SimpleAttributeSet();
                StyleConstants.setForeground(keyWordStyle, new Color(0, 90, 170));
                SimpleAttributeSet defaultStyle = new SimpleAttributeSet();
                StyleConstants.setForeground(defaultStyle, Color.black);

                StyledDocument doc = jTextPane.getStyledDocument();

                keyMouseAction(jTextPane, doc, keyWordStyle, defaultStyle);
            }
        });
        databaseFrame.getPanelCenter().getInputArea().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if (e.getKeyCode() == 116) {
                    // elkuldes a szervernek
                    try {
                        String command = databaseFrame.getPanelCenter().getInputArea().getSelectedText();
                        if (command == null) {
                            command = databaseFrame.getPanelCenter().getInputArea().getText();
                        }
                        String[] commands = command.split(";" + System.lineSeparator());
                        //set error message chanel is empty
                        databaseFrame.getPanelDown().getErrorLabel().setText("");
                        Pattern pattern = Pattern.compile("^\\s*(?i)SELECT");
                        for (String s : commands) {
                            s=formatCommandInFrontend(s);
                            clientConnection.send(s);
                            if(pattern.matcher(s).find()){
                                //ha select
                                List<String> result = clientConnection.getSelectResult();
                                System.out.println(result);

                                Object[] columnNames = result.get(0).split("#");
                                JTable table = databaseFrame.getPanelCenter().getResultsTable();
                                DefaultTableModel model = new DefaultTableModel(columnNames, 0);
                                for (int i=1; i<result.size(); i++) {
                                    Object[] rowData = result.get(i).split("#");
                                    model.addRow(rowData);
                                }

                                table.setModel(model);
                            }
                        }
                        clientConnection.send("END");

                    } catch (IOException ex) {
                        ErrorClient.send("Connection ERROR to server on 12000 port!");
                    }
                } else {
                    JTextPane jTextPane = databaseFrame.getPanelCenter().getInputArea();

                    SimpleAttributeSet keyWordStyle = new SimpleAttributeSet();
                    StyleConstants.setForeground(keyWordStyle, new Color(0, 90, 170));
                    SimpleAttributeSet defaultStyle = new SimpleAttributeSet();
                    StyleConstants.setForeground(defaultStyle, Color.black);

                    StyledDocument doc = jTextPane.getStyledDocument();

                    if (e.getKeyCode() == 9) {
                        int startIndex, endIndex;
                        int i = 0;
                        while (i < doc.getLength()) {
                            try {
                                startIndex = Utilities.getWordStart(jTextPane, i);
                                endIndex = Utilities.getWordEnd(jTextPane, i);
                                String text = doc.getText(startIndex, endIndex - startIndex);
                                boolean ok = false;
                                for (String keyWord : keyWords) {
                                    text = text.toUpperCase();
                                    if (keyWord.equals(text)) {
                                        ok = true;
                                        doc.setCharacterAttributes(startIndex, endIndex, keyWordStyle, true);
                                    }
                                }
                                if (!ok) {
                                    doc.setCharacterAttributes(startIndex, endIndex, defaultStyle, true);
                                }
                            } catch (BadLocationException ex) {
                                throw new RuntimeException(ex);
                            }
                            i = endIndex + 1;
                        }
                    } else {
                        keyMouseAction(jTextPane, doc, keyWordStyle, defaultStyle);
                    }
                }
            }
        });
        databaseFrame.getPanelTop().getExit().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clientConnection.disconnect();
                databaseFrame.dispose();
                System.exit(0);
            }
        });
        databaseFrame.getPanelCenter().getButtonCommandLine().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                databaseFrame.getPanelCenter().getInputLabel().setText("Command line:");
                JPanel cards = databaseFrame.getPanelCenter().getCards();
                CardLayout cl = (CardLayout) (cards.getLayout());
                cl.show(cards, databaseFrame.getPanelCenter().getPanelCommandString());
            }
        });
        databaseFrame.getPanelCenter().getButtonInsertDeleteQuery().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                databaseFrame.getPanelCenter().getInputLabel().setText("Ins-Del Query:");
                databaseFrame.getPanelCenter().getInsertDeleteQuery().refresh();
                JPanel cards = databaseFrame.getPanelCenter().getCards();
                CardLayout cl = (CardLayout) (cards.getLayout());
                cl.show(cards, databaseFrame.getPanelCenter().getPanelInsDelQuery());
            }
        });
        databaseFrame.getPanelCenter().getButtonSelectQuery().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                databaseFrame.getPanelCenter().getInputLabel().setText("Select Query:");
                JPanel cards = databaseFrame.getPanelCenter().getCards();
                CardLayout cl = (CardLayout) (cards.getLayout());
                cl.show(cards, databaseFrame.getPanelCenter().getPanelSelectQuery());
            }
        });
        databaseFrame.getPanelCenter().getSelectQuery().getTableComboBox().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // center down
                ArrayList<TableBox> tableBoxes = databaseFrame.getPanelCenter().getSelectQuery().getTableBoxes();
                JPanel centerDown = databaseFrame.getPanelCenter().getSelectQuery().getCenterDown();
                String newTableName = databaseFrame.getPanelCenter().getSelectQuery().getTableComboBox().getSelectedItem().toString();
                databaseFrame.getPanelCenter().getSelectQuery().setCurrentTableName(newTableName);

                TableBox tableBox = new TableBox(databaseFrame.getPanelCenter().getSelectQuery(), newTableName);
                String[] allAttributes = databaseFrame.getPanelCenter().getSelectQuery().getAllAttributes();
                ArrayList<JCheckBox> checkBoxes = tableBox.getCheckBoxes();

                JComboBox leftJoinComboBox = tableBox.getLeftJoinComboBox();
                JComboBox innerJoinComboBox = tableBox.getInnerJoinComboBox();
                JComboBox rightJoinComboBox = tableBox.getRightJoinComboBox();

                tableBoxes.add(tableBox);
                int length = tableBoxes.size();

                tableBoxes.get(length - 1).getAttributesPanel().add(checkBoxes.get(0));
                for (int i = 0; i < allAttributes.length; i++) {
                    checkBoxes.add(new JCheckBox(allAttributes[i]));
                    tableBoxes.get(length - 1).getAttributesPanel().add(checkBoxes.get(i + 1));
                }

                centerDown.add(tableBoxes.get(length - 1));

                // add actionListener to each attribute
                for (int i = 1; i < checkBoxes.size(); i++) {
                    int finalI = i;
                    checkBoxes.get(i).addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            if (checkBoxes.get(finalI).isSelected()) {
                                checkBoxes.get(0).setSelected(false);
                            }
                        }
                    });
                }

                leftJoinComboBox.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (!Objects.equals(joinSelectedTable, "-")) {
                            if (!Objects.equals(joinSelectedTable, newTableName)) {
                                ArrayList<TableBox> tableBoxes = databaseFrame.getPanelCenter().getSelectQuery().getTableBoxes();
                                databaseFrame.getPanelCenter().getSelectQuery().getLeftJoins()
                                        .add(joinSelectedTable
                                                + " LEFT JOIN " + newTableName
                                                + " ON " + joinSelectedTable + "." + joinSelectedAttribute + "=" + newTableName + "." + leftJoinComboBox.getSelectedItem().toString());

                                for (int i = 0; i < tableBoxes.size(); i++) {
                                    if (tableBoxes.get(i).getTableName().equals(joinSelectedTable)) {
                                        tableBoxes.get(i).getLeftJoinComboBox().setSelectedIndex(0);
                                    }
                                    if (tableBoxes.get(i).getTableName().equals(newTableName)) {
                                        tableBoxes.get(i).getLeftJoinComboBox().setSelectedIndex(0);
                                    }
                                }
                                joinSelectedTable = "-";
                                joinSelectedAttribute = "-";
                            } else if (newTableName.equals("-")) {
                                joinSelectedTable = "-";
                                joinSelectedAttribute = "-";
                            }
                        } else {
                            joinSelectedTable = newTableName;
                            joinSelectedAttribute = leftJoinComboBox.getSelectedItem().toString();
                        }
                    }
                });
                innerJoinComboBox.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (!Objects.equals(joinSelectedTable, "-")) {
                            if (!Objects.equals(joinSelectedTable, newTableName)) {
                                ArrayList<TableBox> tableBoxes = databaseFrame.getPanelCenter().getSelectQuery().getTableBoxes();
                                databaseFrame.getPanelCenter().getSelectQuery().getInnerJoins()
                                        .add(joinSelectedTable
                                                + " INNER JOIN " + newTableName
                                                + " ON " + joinSelectedTable + "." + joinSelectedAttribute + "=" + newTableName + "." + innerJoinComboBox.getSelectedItem().toString());

                                for (int i = 0; i < tableBoxes.size(); i++) {
                                    if (tableBoxes.get(i).getTableName().equals(joinSelectedTable)) {
                                        tableBoxes.get(i).getInnerJoinComboBox().setSelectedIndex(0);
                                    }
                                    if (tableBoxes.get(i).getTableName().equals(newTableName)) {
                                        tableBoxes.get(i).getInnerJoinComboBox().setSelectedIndex(0);
                                    }
                                }
                                joinSelectedTable = "-";
                                joinSelectedAttribute = "-";
                            } else if (newTableName.equals("-")) {
                                joinSelectedTable = "-";
                                joinSelectedAttribute = "-";
                            }
                        } else {
                            joinSelectedTable = newTableName;
                            joinSelectedAttribute = innerJoinComboBox.getSelectedItem().toString();
                        }
                    }
                });
                rightJoinComboBox.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (!Objects.equals(joinSelectedTable, "-")) {
                            if (!Objects.equals(joinSelectedTable, newTableName)) {
                                ArrayList<TableBox> tableBoxes = databaseFrame.getPanelCenter().getSelectQuery().getTableBoxes();
                                databaseFrame.getPanelCenter().getSelectQuery().getRightJoins()
                                        .add(joinSelectedTable
                                                + " RIGHT JOIN " + newTableName
                                                + " ON " + joinSelectedTable + "." + joinSelectedAttribute + "=" + newTableName + "." + rightJoinComboBox.getSelectedItem().toString());

                                for (int i = 0; i < tableBoxes.size(); i++) {
                                    if (tableBoxes.get(i).getTableName().equals(joinSelectedTable)) {
                                        tableBoxes.get(i).getRightJoinComboBox().setSelectedIndex(0);
                                    }
                                    if (tableBoxes.get(i).getTableName().equals(newTableName)) {
                                        tableBoxes.get(i).getRightJoinComboBox().setSelectedIndex(0);
                                    }
                                }
                                joinSelectedTable = "-";
                                joinSelectedAttribute = "-";
                            } else if (newTableName.equals("-")) {
                                joinSelectedTable = "-";
                                joinSelectedAttribute = "-";
                            }
                        } else {
                            joinSelectedTable = newTableName;
                            joinSelectedAttribute = rightJoinComboBox.getSelectedItem().toString();
                        }
                    }
                });

                // center
                DefaultTableModel model = (DefaultTableModel) databaseFrame.getPanelCenter().getSelectQuery().getTable().getModel();
                for (String attribute : allAttributes) {
                    Object[] rowData = {attribute, "", newTableName, "Unsorted", "Unsorted", "", ""};
                    model.addRow(rowData);
                }
            }
        });
        databaseFrame.getPanelCenter().getSelectQuery().getRunButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JTable table = databaseFrame.getPanelCenter().getSelectQuery().getTable();

                // SELECT FROM
                StringBuilder selectCommand = new StringBuilder();
                JTextPane jTextPane = databaseFrame.getPanelCenter().getSelectQuery().getSelectCommandText();
                selectCommand.append("SELECT ");
                ArrayList<TableBox> tableBoxes = databaseFrame.getPanelCenter().getSelectQuery().getTableBoxes();
                StringBuilder usedTables = new StringBuilder();

                ArrayList<String> leftJoins = databaseFrame.getPanelCenter().getSelectQuery().getLeftJoins();
                ArrayList<String> innerJoins = databaseFrame.getPanelCenter().getSelectQuery().getInnerJoins();
                ArrayList<String> rightJoins = databaseFrame.getPanelCenter().getSelectQuery().getRightJoins();

                for (int i = 0; i < tableBoxes.size(); i++) {
                    String tableName = tableBoxes.get(i).getTableName();
                    ArrayList<JCheckBox> jCheckBox = tableBoxes.get(i).getCheckBoxes();
                    boolean used = false;
                    for (int j = 0; j < jCheckBox.size(); j++) {
                        if (jCheckBox.get(j).isSelected()) {
                            used = true;
                            selectCommand.append(tableName).append(".").append(jCheckBox.get(j).getText());
                            if (j != 0) {  // not *
                                String alias = getAlias(table, tableName, jCheckBox.get(j).getText());
                                if (!alias.equals("")) {
                                    selectCommand.append(" AS ").append(alias);
                                }
                            }
                            selectCommand.append(",");
                        }
                    }
                    if (used) {
                        usedTables.append(tableName).append(",");
                    }
                }
                // FIFO (queue) where I have to find all pairs of first table and then second and so on
                // bulid FROM (with joins)
                Queue<String> queue = new LinkedList<>();
                StringBuilder joinCondition = new StringBuilder();
                String[] usedTablesArray = usedTables.toString().split(",");

                // JOIN BUILDING
                buildJoin(queue, leftJoins, joinCondition);
                buildJoin(queue, innerJoins, joinCondition);
                buildJoin(queue, rightJoins, joinCondition);

                // add tables which are not part of join (CROSS JOIN)
                int k = 0;
                if (joinCondition.toString().equals("") && usedTablesArray.length != 0) {
                    joinCondition.append(usedTablesArray[0]).append(" ");
                    k = 1;
                }
                while (k < usedTablesArray.length) {
                    if (!usedTablesArray[k].equals("") && notInJoins(String.valueOf(joinCondition), usedTablesArray[k])) {
                        joinCondition.append("\nCROSS JOIN ").append(usedTablesArray[k]);
                    }
                    k++;
                }

                selectCommand = new StringBuilder(selectCommand.substring(0, selectCommand.length() - 1));
                selectCommand.append("\nFROM ");
                if (usedTables.length() != 0) {
                    selectCommand.append(joinCondition);
                }

                // WHERE AND ORDER BY
                StringBuilder conditions = new StringBuilder();
                StringBuilder orderByConditions = new StringBuilder();
                String[] orderByConditionsArray = new String[101];
                boolean existsFilter = false;
                boolean existsSortType = false;

                for (int i = 0; i < table.getRowCount(); i++) {
                    String attribute = (String) table.getValueAt(i, 0);
                    String tableName = (String) table.getValueAt(i, 2);
                    String filterCondition = (String) table.getValueAt(i, 5);
                    String sortType = (String) table.getValueAt(i, 3);
                    String sortOrder = (String) table.getValueAt(i, 4);
                    if (!filterCondition.equals("")) {
                        existsFilter = true;
                        conditions.append("(").append(tableName).append(".").append(attribute).append(filterCondition);
                        for (int j = 6; j < table.getColumnCount(); j++) {
                            String orCondition = (String) table.getValueAt(i, j);
                            if (!orCondition.equals("")) {
                                conditions.append(" OR ").append(tableName).append(".").append(attribute).append(orCondition);
                            }
                        }
                        conditions.append(") AND ");
                    }
                    if (!sortType.equals("Unsorted")) {
                        if (!sortOrder.equals("Unsorted")) {
                            existsSortType = true;
                            int index = Integer.parseInt(sortOrder);
                            orderByConditionsArray[index] = tableName + "." + attribute;
                            if (sortType.equals("Descending")) {
                                orderByConditionsArray[index] += " DESC";
                            }
                            orderByConditionsArray[index] += ",";
                        }
                    }
                }
                if (existsFilter) {
                    selectCommand.append("\nWHERE ");
                    selectCommand.append(conditions.substring(0, conditions.length() - 5));
                }
                if (existsSortType) {
                    selectCommand.append("\nORDER BY ");
                    for (int i = 1; i <= 100; i++) {
                        if (orderByConditionsArray[i] != null) {
                            orderByConditions.append(orderByConditionsArray[i]);
                        }
                    }
                    selectCommand.append(orderByConditions.substring(0, orderByConditions.length() - 1));
                }

                jTextPane.setText(String.valueOf(selectCommand));
            }
        });
        databaseFrame.getPanelCenter().getSelectQuery().getSendButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectQuery = databaseFrame.getPanelCenter().getSelectQuery().getSelectCommandText().getText();
                databaseFrame.getPanelCenter().getInputArea().setText(selectQuery.replaceAll("\n", System.lineSeparator()));

                databaseFrame.getPanelCenter().getInputLabel().setText("Command line:");
                JPanel cards = databaseFrame.getPanelCenter().getCards();
                CardLayout cl = (CardLayout) (cards.getLayout());
                cl.show(cards, databaseFrame.getPanelCenter().getPanelCommandString());
            }
        });
        TimerThread timerThread = new TimerThread(databaseFrame.getPanelTop());
        timerThread.start();
        ErrorChannelThread errorChannelThread = new ErrorChannelThread(databaseFrame.getPanelDown());
        errorChannelThread.start();
    }

    private void keyMouseAction(JTextPane jTextPane, StyledDocument doc, SimpleAttributeSet keyWordStyle, SimpleAttributeSet defaultStyle) {
        int caretPosition = jTextPane.getCaretPosition();
        String word = null;
        int startIndex, endIndex;
        try {
            startIndex = Utilities.getWordStart(jTextPane, caretPosition);
            endIndex = Utilities.getWordEnd(jTextPane, caretPosition);
            word = doc.getText(startIndex, endIndex - startIndex);
        } catch (BadLocationException ex) {
            throw new RuntimeException(ex);
        }

        boolean ok = false;
        for (String keyWord : keyWords) {
            if (keyWord.equalsIgnoreCase(word)) {
                ok = true;
                doc.setCharacterAttributes(startIndex, endIndex - startIndex, keyWordStyle, true);
                break;
            }
        }
        if (!ok) {
            doc.setCharacterAttributes(startIndex, endIndex - startIndex, defaultStyle, true);
        }
    }

    private String getAlias(JTable table, String tableName, String attributeName) {
        for (int i = 0; i < table.getRowCount(); i++) {
            if (table.getValueAt(i, 0).equals(attributeName) && table.getValueAt(i, 2).equals(tableName)) {
                return table.getValueAt(i, 1).toString();
            }
        }
        return "";
    }

    private boolean notInJoins(String joinCondition, String tableName) {
        String[] splitted = joinCondition.split("\\s+");
        for (String s : splitted) {
            if (s.equals(tableName)) {
                return false;
            }
        }
        return true;
    }

    private void buildJoin(Queue<String> queue, ArrayList<String> joins, StringBuilder joinCondition) {
        if (joins.size() != 0) {
            String[] currentJoinTables = joins.get(0).split(" ");
            queue.add(currentJoinTables[0]);
            queue.add(currentJoinTables[3]);
            joinCondition.append(joins.get(0));
            while (!queue.isEmpty()) {
                String first = queue.peek();
                for (int i = 1; i < joins.size(); i++) {
                    currentJoinTables = joins.get(i).split(" ");
                    if (currentJoinTables[0].equals(first)) {
                        queue.add(currentJoinTables[3]);
                        joinCondition.append("\n");
                        for (int j = 1; j < currentJoinTables.length; j++) {
                            joinCondition.append(currentJoinTables[j]).append(" ");
                        }
                    }
                }
                queue.remove();
            }
        }
    }

    private String formatCommandInFrontend(String message){
        message = message.replaceAll(System.lineSeparator() + "+", " ");
        message = message.replaceAll("\t", " ");
        message = message.replaceAll("\s+", " ");
        message = message.replaceAll("^\s+", "");
        message = message.replaceAll("\s+$", "");
        return message;
    }
}
