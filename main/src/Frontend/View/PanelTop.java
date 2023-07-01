package Frontend.View;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;

public class PanelTop extends JPanel {
    private final JMenuItem connect;
    private final JMenuItem disconnect;
    private final JMenuItem exit;

    public PanelTop() {
        JLabel projectLabel = new JLabel("ExeDataDB");
        projectLabel.setFont(new Font("Berlin Sans FB", Font.PLAIN, 36));
        this.setLayout(new GridLayout(1, 3));
        this.add(projectLabel);
        this.setBorder(new MatteBorder(0, 0, 2, 0, Color.black));

        JMenu menu = new JMenu("Menu");
        connect = new JMenuItem("Connect");
        disconnect = new JMenuItem("Disconnect");
        exit = new JMenuItem("Exit");
        menu.add(connect);
        menu.add(disconnect);
        menu.add(exit);
        connect.setEnabled(false);
        disconnect.setEnabled(true);


        JMenuBar menuBar = new JMenuBar();
        menuBar.add(menu);
        this.add(menuBar);
    }

    public JMenuItem getConnect() {
        return connect;
    }

    public JMenuItem getDisconnect() {
        return disconnect;
    }

    public JMenuItem getExit() {
        return exit;
    }
}
