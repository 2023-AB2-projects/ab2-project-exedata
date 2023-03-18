package Frontend;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;

public class PanelCenter extends JPanel {
    private JPanel commandLineInput; // left side of PanelCenter
    private JPanel commandLineResults; // right side of PanelCenter
    private JLabel commandLineInputLabel; // left label
    private JLabel commandLineResultsLabel; // right label
    private JLabel messagesLabel; // messages under query result
    private JLabel tables; // result appear in table format
    private JTextArea inputArea; // command input (ex. CREATE TABLE)
    public PanelCenter() {
        this.setLayout(new GridLayout(1, 2));

        commandLineInput = new JPanel();
        inputArea = new JTextArea();
        inputArea.setBackground(Color.gray);
        inputArea.setForeground(Color.white);
        inputArea.setFont(new Font("Courier New", Font.PLAIN, 15));
        commandLineInputLabel = new JLabel("Command line:");
        commandLineInput.setLayout(new BorderLayout());
        commandLineInputLabel.setFont(new Font("Courier New", Font.BOLD, 15));
        commandLineInputLabel.setBorder(new MatteBorder(0, 0, 2, 0, Color.black));
        commandLineInput.add(commandLineInputLabel, BorderLayout.NORTH);
        commandLineInput.add(inputArea, BorderLayout.CENTER);
        this.add(commandLineInput);

        //=====================================
        commandLineResults = new JPanel();
        commandLineResults.setLayout(new BorderLayout());
        commandLineResultsLabel = new JLabel("Results");
        commandLineResultsLabel.setFont(new Font("Courier New", Font.BOLD, 15));
        commandLineResultsLabel.setBorder(new MatteBorder(0, 0, 2, 0, Color.black));
        commandLineResults.add(commandLineResultsLabel, BorderLayout.NORTH);

        //=====================================
        messagesLabel = new JLabel("There will appear compilation results.");
        messagesLabel.setBorder(new MatteBorder(2, 2, 2, 2, Color.black));
        commandLineResults.add(messagesLabel, BorderLayout.SOUTH);

        //=====================================
        tables = new JLabel("There will appear the results of the query in table format.");
        tables.setBorder(new MatteBorder(0, 2, 0, 2, Color.black));
        commandLineResults.add(tables, BorderLayout.CENTER);

        this.add(commandLineResults);
    }

}
