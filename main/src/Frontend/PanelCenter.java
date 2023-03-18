package Frontend;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class PanelCenter extends JPanel {
    private final JLabel messagesLabel; // messages under query result
    private final JTextArea inputArea; // command input (ex. CREATE TABLE)

    public PanelCenter() {
        this.setLayout(new GridLayout(1, 2));

        // left side of PanelCenter
        JPanel commandLineInput = new JPanel();
        inputArea = new JTextArea();
        inputArea.setBackground(new Color(171, 173, 182));
        inputArea.setForeground(Color.black);
        inputArea.setFont(new Font("Courier New", Font.BOLD, 15));
        inputArea.setCaretColor(Color.black);
        // left label
        JLabel commandLineInputLabel = new JLabel("Command line:");
        commandLineInput.setLayout(new BorderLayout());
        commandLineInputLabel.setFont(new Font("Courier New", Font.BOLD, 15));
        commandLineInputLabel.setBorder(new MatteBorder(0, 0, 2, 0, Color.black));
        commandLineInput.add(commandLineInputLabel, BorderLayout.NORTH);
        commandLineInput.add(inputArea, BorderLayout.CENTER);
        this.add(commandLineInput);

        //=====================================
        // right side of PanelCenter
        JPanel commandLineResults = new JPanel();
        commandLineResults.setLayout(new BorderLayout());
        // right label
        JLabel commandLineResultsLabel = new JLabel("Results:");
        commandLineResultsLabel.setFont(new Font("Courier New", Font.BOLD, 15));
        commandLineResultsLabel.setBorder(new MatteBorder(0, 0, 2, 0, Color.black));
        commandLineResults.add(commandLineResultsLabel, BorderLayout.NORTH);

        //=====================================
        messagesLabel = new JLabel("There will appear compilation results.");
        messagesLabel.setBorder(new MatteBorder(2, 2, 2, 2, Color.black));
        commandLineResults.add(messagesLabel, BorderLayout.SOUTH);

        //=====================================
        // result appear in table format
        JLabel tables = new JLabel("There will appear the results of the query in table format.");
        tables.setBorder(new MatteBorder(0, 2, 0, 2, Color.black));
        commandLineResults.add(tables, BorderLayout.NORTH);
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File("background.png"));
            JLabel imageLabel = new JLabel(new ImageIcon(img));
            commandLineResults.add(imageLabel, BorderLayout.CENTER);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.add(commandLineResults);
    }

    public JLabel getMessagesLabel() {
        return messagesLabel;
    }

    public JTextArea getInputArea() {
        return inputArea;
    }
}
