package Frontend;

import Frontend.SelectPanel.SelectQuery;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;

public class PanelCenter extends JPanel {
    private final JPanel cards;
    private final InsertDeleteQuery inputAreaPanelInsDelQuery;
    private final SelectQuery inputAreaPanelSelectQuery;
    private final JTextPane inputArea; // command input (ex. CREATE TABLE)
    private final JPanel commandLineInput;
    private final JTable resultsTable;
    private final JButton buttonCommandLine;
    private final JButton buttonInsertDeleteQuery;
    private final JButton buttonSelectQuery;
    private final String panelCommandString = "PANELCOMMAND";
    private final String panelInsDelQuery = "PANELINSDELQUERY";
    private final String panelSelectQuery = "PANELSELECTQUERY";

    private final JLabel inputLabel;

    public PanelCenter(PanelDown panelDown, ClientConnection clientConnection) {
        this.setLayout(new GridLayout(1, 2));

        // left side of PanelCenter
        commandLineInput = new JPanel();
        inputArea = new JTextPane();
        inputArea.setBackground(new Color(171, 173, 182));
        inputArea.setForeground(Color.black);
        inputArea.setFont(new Font("Courier New", Font.BOLD, 15));
        inputArea.setCaretColor(Color.black);

        // left label
        //top of commandLine
        JPanel commandLineHeader = new JPanel();
        commandLineHeader.setLayout(new GridLayout(1, 2));

        //commandLineInputLabel
        inputLabel = new JLabel("Command line:");
        commandLineInput.setLayout(new BorderLayout());
        inputLabel.setFont(new Font("Courier New", Font.BOLD, 15));
        inputLabel.setBorder(new MatteBorder(0, 0, 2, 0, Color.black));

        //commandLineInputButtons
        JPanel commandLineInputButtonPanel = new JPanel();
        buttonCommandLine = new JButton("CommandLine");
        buttonInsertDeleteQuery = new JButton("InsDelQuery");
        buttonSelectQuery = new JButton("SelectQuery");
        commandLineInputButtonPanel.add(buttonCommandLine);
        commandLineInputButtonPanel.add(buttonInsertDeleteQuery);
        commandLineInputButtonPanel.add(buttonSelectQuery);

        commandLineInputButtonPanel.setFont(new Font("Courier New", Font.BOLD, 15));
        commandLineInputButtonPanel.setBorder(new MatteBorder(0, 0, 2, 0, Color.black));

        //fill header with label and buttons
        commandLineHeader.add(inputLabel);
        commandLineHeader.add(commandLineInputButtonPanel);

        //init 3 JPanel to card
        //================================================================================================
        cards = new JPanel();
        JPanel inputAreaPanelCommandLine = new JPanel();
        inputAreaPanelInsDelQuery = new InsertDeleteQuery(panelDown, clientConnection);
        inputAreaPanelSelectQuery = new SelectQuery();

        inputAreaPanelCommandLine.setLayout(new GridLayout(1, 1));
        JScrollPane scrollPane = new JScrollPane(inputArea);
        inputAreaPanelCommandLine.add(scrollPane);

        cards.setLayout(new CardLayout());

        //add three side to the card
        cards.add(inputAreaPanelCommandLine, panelCommandString);
        cards.add(inputAreaPanelInsDelQuery, panelInsDelQuery);
        cards.add(inputAreaPanelSelectQuery, panelSelectQuery);

        commandLineInput.add(commandLineHeader, BorderLayout.NORTH);
        commandLineInput.add(cards, BorderLayout.CENTER);
        this.add(commandLineInput);

        //================================================================================================
        // right side of PanelCenter
        JPanel commandLineResults = new JPanel();
        commandLineResults.setLayout(new GridLayout(1, 1));
        resultsTable = new JTable();
        JScrollPane jScrollPane = new JScrollPane(resultsTable);
        commandLineResults.add(jScrollPane);
        this.add(commandLineResults);
    }

    public InsertDeleteQuery getInsertDeleteQuery() {
        return inputAreaPanelInsDelQuery;
    }

    public SelectQuery getSelectQuery() {
        return inputAreaPanelSelectQuery;
    }

    public JTextPane getInputArea() {
        return inputArea;
    }

    public JPanel getCommandLineInput() {
        return commandLineInput;
    }

    public JPanel getCards() {
        return cards;
    }

    public JButton getButtonCommandLine() {
        return buttonCommandLine;
    }

    public JButton getButtonInsertDeleteQuery() {
        return buttonInsertDeleteQuery;
    }

    public JButton getButtonSelectQuery() {
        return buttonSelectQuery;
    }

    public String getPanelCommandString() {
        return panelCommandString;
    }

    public String getPanelInsDelQuery() {
        return panelInsDelQuery;
    }

    public String getPanelSelectQuery() {
        return panelSelectQuery;
    }

    public InsertDeleteQuery getInputAreaPanelInsDelQuery() {
        return inputAreaPanelInsDelQuery;
    }

    public JLabel getInputLabel() {
        return inputLabel;
    }

    public JTable getResultsTable() {
        return resultsTable;
    }
}
