package Frontend;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;

public class PanelTop extends JPanel {
    private JMenuBar menuBar;
    private JMenu connect;
    private JMenu disconnect;
    private JMenu databases;

    public PanelTop() {
        JLabel projectLabel = new JLabel("ExeDataDB");
        projectLabel.setFont(new Font("Berlin Sans FB", Font.PLAIN, 36));
        this.setLayout(new GridLayout(1, 3));
        this.add(projectLabel);
        this.setBorder(new MatteBorder(0, 0, 2, 0, Color.black));

        connect = new JMenu("Connect");
        disconnect = new JMenu("Disconnect");
        databases = new JMenu("Databases");
        menuBar = new JMenuBar();
        menuBar.add(connect);
        menuBar.add(disconnect);
        menuBar.add(databases);
        this.add(menuBar);
    }
}
