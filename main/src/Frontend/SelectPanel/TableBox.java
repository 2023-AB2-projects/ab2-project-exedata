package Frontend.SelectPanel;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class TableBox extends JPanel {
    private int boxX = 0;
    private int boxY = 0;
    private int width = 100;
    private int height = 200;
    private Point currentPoint;
    public TableBox() {
        this.setBounds(boxX, boxY, width, height);
        this.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.BLACK));
        this.currentPoint = null;
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseMoved(e);
                Point point = getMousePosition();
                System.out.println(point);
            }
        });
    }
}
