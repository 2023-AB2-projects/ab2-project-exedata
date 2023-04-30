package Frontend;

import Frontend.SelectPanel.TableBox;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class DatabaseController {
    private final DatabaseFrame databaseFrame;
    private final ClientConnection clientConnection;

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
        databaseFrame = new DatabaseFrame();
        clientConnection = new ClientConnection(12000);
        clientConnection.connect(12000);
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
                        for (String s : commands) {
                            clientConnection.send(s);
                            // varom a valaszt
                        }
                        clientConnection.send("END");

                    } catch (IOException ex) {
                        databaseFrame.getPanelCenter().getMessagesLabel().setText("Connection ERROR to server on 12000 port!");
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

                TableBox tableBox = new TableBox(newTableName);
                String[] allAttributes = databaseFrame.getPanelCenter().getSelectQuery().getAllAttributes();
                ArrayList<JCheckBox> checkBoxes = tableBox.getCheckBoxes();

                tableBoxes.add(tableBox);
                int length = tableBoxes.size();

                tableBoxes.get(length - 1).getAttributesPanel().add(checkBoxes.get(0));
                for (int i = 0; i < allAttributes.length; i++) {
                    checkBoxes.add(new JCheckBox(allAttributes[i]));
                    tableBoxes.get(length - 1).getAttributesPanel().add(checkBoxes.get(i + 1));
                }

                centerDown.add(tableBoxes.get(length - 1));

                // add actionListener to each attribute
                for (int i=1; i<checkBoxes.size(); i++) {
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

                // center
                DefaultTableModel model = (DefaultTableModel) databaseFrame.getPanelCenter().getSelectQuery().getTable().getModel();
                for (String attribute : allAttributes) {
                    Object[] rowData = {attribute, "", newTableName, "", "", "", ""};
                    model.addRow(rowData);
                }
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
}
