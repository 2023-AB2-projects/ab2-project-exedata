package Frontend;

import Backend.Commands.Select;
import Frontend.SelectPanel.SelectQuery;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class PanelCenter extends JPanel {
    private final JLabel messagesLabel; // messages under query result
    private JPanel cards;
    private JPanel inputAreaPanelCommandLine;
    private InsertDeleteQuery inputAreaPanelInsDelQuery;
    private SelectQuery inputAreaPanelSelectQuery;
    private final JTextPane inputArea; // command input (ex. CREATE TABLE)
    private JPanel commandLineInput;
    private JPanel commandLineHeader;
    private JButton buttonCommandLine;
    private JButton buttonInsertDeleteQuery;
    private JButton buttonSelectQuery;
    private final String panelCommandString = "PANELCOMMAND";
    private final String panelInsDelQuery = "PANELINSDELQUERY";
    private final String panelSelectQuery = "PANELSELECTQUERY";
    private final PanelDown panelDown;

    public PanelCenter(PanelDown panelDown) {
        this.panelDown = panelDown;
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
        commandLineHeader = new JPanel();
        commandLineHeader.setLayout(new GridLayout(1, 2));

        //commandLineInputLabel
        JLabel commandLineInputLabel = new JLabel("Command line:");
        commandLineInput.setLayout(new BorderLayout());
        commandLineInputLabel.setFont(new Font("Courier New", Font.BOLD, 15));
        commandLineInputLabel.setBorder(new MatteBorder(0, 0, 2, 0, Color.black));

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
        commandLineHeader.add(commandLineInputLabel);
        commandLineHeader.add(commandLineInputButtonPanel);

        //init 3 JPanel to card
        //================================================================================================
        cards = new JPanel();
        inputAreaPanelCommandLine = new JPanel();
        inputAreaPanelInsDelQuery = new InsertDeleteQuery(this.panelDown);
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
        commandLineResults.setLayout(new BorderLayout());
        // right label
        commandLineResults.setFont(new Font("Courier New", Font.BOLD, 15));
        commandLineResults.setBorder(new MatteBorder(0, 0, 2, 0, Color.black));

        //================================================================================================
        messagesLabel = new JLabel("There will appear compilation results.");
        messagesLabel.setBorder(new MatteBorder(2, 2, 2, 2, Color.black));
        commandLineResults.add(messagesLabel, BorderLayout.SOUTH);

        //================================================================================================
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File("main/background.png"));
            JLabel imageLabel = new JLabel(new ImageIcon(img));
            commandLineResults.add(imageLabel, BorderLayout.CENTER);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.add(commandLineResults);
    }

    public InsertDeleteQuery getInsertDeleteQuery() {
        return inputAreaPanelInsDelQuery;
    }

    public SelectQuery getSelectQuery() {
        return inputAreaPanelSelectQuery;
    }

    public JLabel getMessagesLabel() {
        return messagesLabel;
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
}
