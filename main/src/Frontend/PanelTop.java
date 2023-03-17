package Frontend;

import javax.swing.*;
import java.awt.*;

public class PanelTop extends JPanel {
    public PanelTop() {
        JLabel projectLabel = new JLabel("ExeDataDB");
        projectLabel.setFont(new Font("Berlin Sans FB", Font.PLAIN, 36));
        this.setLayout(new GridLayout(1, 2));
        this.add(projectLabel);
        this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    }
}
