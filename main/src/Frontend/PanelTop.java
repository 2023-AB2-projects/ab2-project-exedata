package Frontend;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PanelTop extends JPanel {
    private JMenuBar menuBar;
    private JMenu menu;
    private JMenuItem connect;
    private JMenuItem disconnect;
    private JMenu databases;

    public PanelTop() {
        JLabel projectLabel = new JLabel("ExeDataDB");
        projectLabel.setFont(new Font("Berlin Sans FB", Font.PLAIN, 36));
        this.setLayout(new GridLayout(1, 3));
        this.add(projectLabel);
        this.setBorder(new MatteBorder(0, 0, 2, 0, Color.black));

        menu = new JMenu("Menu");
        connect = new JMenuItem("Connect");
        disconnect = new JMenuItem("Disconnect");
        menu.add(connect);
        menu.add(disconnect);
        databases = new JMenu("Databases");
        connect.setEnabled(false);
        disconnect.setEnabled(true);


        menuBar = new JMenuBar();
        menuBar.add(menu);
        menuBar.add(databases);
        this.add(menuBar);
    }

    public JMenuItem getConnect() {
        return connect;
    }

    public JMenuItem getDisconnect() {
        return disconnect;
    }
}
