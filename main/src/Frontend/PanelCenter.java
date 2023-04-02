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
    private InsertDeleteQuery insertDeleteQuery;
    private JPanel commandLineInput;
    private JPanel commandLineHeader;
    private JButton buttonCommandLine;
    private JButton buttonInsertDeleteQuery;

    public PanelCenter() {
        this.setLayout(new GridLayout(1, 2));

        // left side of PanelCenter
        commandLineInput = new JPanel();
        inputArea = new JTextArea();
        inputArea.setBackground(new Color(171, 173, 182));
        inputArea.setForeground(Color.black);
        inputArea.setFont(new Font("Courier New", Font.BOLD, 15));
        inputArea.setCaretColor(Color.black);

        // left label
        insertDeleteQuery = new InsertDeleteQuery(this);

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
        commandLineInputButtonPanel.add(buttonCommandLine);
        commandLineInputButtonPanel.add(buttonInsertDeleteQuery);
        commandLineInputButtonPanel.setFont(new Font("Courier New", Font.BOLD, 15));
        commandLineInputButtonPanel.setBorder(new MatteBorder(0, 0, 2, 0, Color.black));

        //fill header with label and buttons
        commandLineHeader.add(commandLineInputLabel);
        commandLineHeader.add(commandLineInputButtonPanel);

        commandLineInput.add(commandLineHeader, BorderLayout.NORTH);
        commandLineInput.add(inputArea, BorderLayout.CENTER);
        this.add(commandLineInput);

        //=====================================
        // right side of PanelCenter
        JPanel commandLineResults = new JPanel();
        commandLineResults.setLayout(new BorderLayout());
        // right label
        commandLineResults.setFont(new Font("Courier New", Font.BOLD, 15));
        commandLineResults.setBorder(new MatteBorder(0, 0, 2, 0, Color.black));

        //=====================================
        messagesLabel = new JLabel("There will appear compilation results.");
        messagesLabel.setBorder(new MatteBorder(2, 2, 2, 2, Color.black));
        commandLineResults.add(messagesLabel, BorderLayout.SOUTH);

        //=====================================
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

    public JLabel getMessagesLabel() {
        return messagesLabel;
    }

    public JTextArea getInputArea() {
        return inputArea;
    }

    public JPanel getCommandLineInput() {
        return commandLineInput;
    }
}
