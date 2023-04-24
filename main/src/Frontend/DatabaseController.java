package Frontend;

import Backend.Parser;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;

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
                        String[] commands = command.split(";\n");
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

                    int caretPosition = jTextPane.getCaretPosition();
                    int startIndex, endIndex;
                    if (e.getKeyCode() == 9) {
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
                        String word = null;
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
                JPanel cards = databaseFrame.getPanelCenter().getCards();
                CardLayout cl = (CardLayout) (cards.getLayout());
                cl.show(cards, databaseFrame.getPanelCenter().getPanelCommandString());
            }
        });
        databaseFrame.getPanelCenter().getButtonInsertDeleteQuery().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                databaseFrame.getPanelCenter().getInsertDeleteQuery().refresh();
                JPanel cards = databaseFrame.getPanelCenter().getCards();
                CardLayout cl = (CardLayout) (cards.getLayout());
                cl.show(cards, databaseFrame.getPanelCenter().getPanelInsDelQuery());
            }
        });
        databaseFrame.getPanelCenter().getButtonSelectQuery().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JPanel cards = databaseFrame.getPanelCenter().getCards();
                CardLayout cl = (CardLayout) (cards.getLayout());
                cl.show(cards, databaseFrame.getPanelCenter().getPanelSelectQuery());
            }
        });
        TimerThread timerThread = new TimerThread(databaseFrame.getPanelTop());
        timerThread.start();
        ErrorChannelThread errorChannelThread = new ErrorChannelThread(databaseFrame.getPanelDown());
        errorChannelThread.start();
    }
}
