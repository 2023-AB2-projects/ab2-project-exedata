package Frontend;

import javax.swing.*;
import java.awt.*;

public class DatabaseFrame extends JFrame {
    private final PanelTop panelTop;
    private final PanelCenter panelCenter;

    public DatabaseFrame() {
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());
        this.setBounds(0, 0, 1000, 700);

        panelTop = new PanelTop();
        panelCenter = new PanelCenter();
        PanelDown panelDown = new PanelDown();

        this.add(panelTop, BorderLayout.NORTH);
        this.add(panelCenter, BorderLayout.CENTER);
        this.add(panelDown, BorderLayout.SOUTH);

        this.setVisible(true);

    }

    public PanelTop getPanelTop() {
        return panelTop;
    }

    public PanelCenter getPanelCenter() {
        return panelCenter;
    }
}