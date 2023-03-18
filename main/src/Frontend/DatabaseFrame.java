package Frontend;

import javax.swing.*;
import java.awt.*;

public class DatabaseFrame extends JFrame {
    private PanelTop panelTop;
    private PanelCenter panelCenter;
    private JPanel panelDown;
    private TimerThread timerThread;
    public DatabaseFrame() {
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());
        this.setBounds(0, 0, 800, 500);

        panelTop = new PanelTop();
        panelCenter = new PanelCenter();
        panelDown = new JPanel();

        this.add(panelTop, BorderLayout.NORTH);
        this.add(panelCenter, BorderLayout.CENTER);
        this.add(panelDown, BorderLayout.SOUTH);


        this.setVisible(true);

        timerThread = new TimerThread(panelTop);
        timerThread.run();

    }
}
