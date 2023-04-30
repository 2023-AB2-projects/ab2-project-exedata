package Frontend.SelectPanel;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class TableBox extends JPanel {
    private int boxX = 5;
    private int boxY = 5;
    private final int width = 150;
    private final int height = 200;
    private Point currentPoint;
    private final JPanel attributesPanel;
    private String tableName;
    private final ArrayList<JCheckBox> checkBoxes;

    public TableBox(String tableName) {
        this.tableName = tableName;
        this.checkBoxes = new ArrayList<>();
        checkBoxes.add(new JCheckBox("*"));

        this.setLayout(new GridBagLayout());

        // Init constraints to GridBagLayout
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.weighty = 0.95;
        gbc.fill = GridBagConstraints.BOTH;

        this.setBounds(boxX, boxY, width, height);
        this.setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED, Color.LIGHT_GRAY, Color.GRAY));
        this.setBackground(Color.LIGHT_GRAY);
        this.currentPoint = null;

        this.add(new JLabel(tableName));

        attributesPanel = new JPanel();
        attributesPanel.setLayout(new GridLayout(0, 1));

        this.add(attributesPanel, gbc);

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
        checkBoxes.get(0).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (int i=1; i<checkBoxes.size(); i++) {
                    checkBoxes.get(i).setSelected(false);
                }
            }
        });
    }

    public String getTableName() {
        return tableName;
    }

    public JPanel getAttributesPanel() {
        return attributesPanel;
    }

    public ArrayList<JCheckBox> getCheckBoxes() {
        return checkBoxes;
    }
}
