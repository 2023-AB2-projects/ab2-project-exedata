package Frontend.SelectPanel;

import Backend.Databases.Database;
import Backend.Databases.Databases;
import Backend.Databases.Table;
import Backend.Parser;
import Backend.SaveLoadJSON.LoadJSON;
import Frontend.ClientConnection;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.List;

public class TableBox extends JPanel {
    private int boxX = 5;
    private int boxY = 5;
    private int width = 150;
    private int height = 250;
    private Point currentPoint;
    private JPanel attributesPanel;
    private JLabel tableName;
    public TableBox() {
        this.setLayout(new GridLayout(2, 1));
        this.setBounds(boxX, boxY, width, height);
        this.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.BLACK));

        this.add(new JLabel("Attributes"));

        attributesPanel = new JPanel();
        attributesPanel.setLayout(new GridLayout(0, 1));

        this.add(attributesPanel);



        this.currentPoint = null;
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseMoved(e);
                Point point = getMousePosition();
                boxX = boxX + (point.x - currentPoint.x);
                boxY = boxY + (point.y - currentPoint.y);
                setBounds(boxX, boxY, width, height);
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mouseClicked(e);
                currentPoint = getMousePosition();
            }
        });
    }

    public JPanel getAttributesPanel() {
        return attributesPanel;
    }
}
