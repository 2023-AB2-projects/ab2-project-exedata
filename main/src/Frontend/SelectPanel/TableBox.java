package Frontend.SelectPanel;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;

public class TableBox extends JPanel {
    private int boxX = 0;
    private int boxY = 0;
    private int width = 100;
    private int height = 200;
    public TableBox() {
        this.setBounds(boxX, boxY, width, height);
        this.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.BLACK));
    }
}
