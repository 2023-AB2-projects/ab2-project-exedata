package Frontend.SelectPanel;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;

public class TableBox extends JPanel {
    private int boxX = 5;
    private int boxY = 5;
    private final int width = 150;
    private final int height = 200;
    private Point currentPoint;
    private final JPanel attributesPanel;
    private final JComboBox<String> innerJoinComboBox;
    private final JComboBox<String> rightJoinComboBox;
    private final JComboBox<String> leftJoinComboBox;
    private final String tableName;
    private final ArrayList<JCheckBox> checkBoxes;

    public TableBox(SelectQuery selectQuery, String tableName) {
        this.tableName = tableName;
        this.checkBoxes = new ArrayList<>();
        checkBoxes.add(new JCheckBox("*"));

        this.setLayout(new GridBagLayout());

        // Init constraints to GridBagLayout
        GridBagConstraints gbc1 = new GridBagConstraints();
        gbc1.gridx = 0;
        gbc1.gridy = 1;
        gbc1.weightx = 1;
        gbc1.weighty = 0.05;
        gbc1.fill = GridBagConstraints.BOTH;

        this.setBounds(boxX, boxY, width, height);
        this.setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED, Color.LIGHT_GRAY, Color.GRAY));
        this.setBackground(Color.LIGHT_GRAY);
        this.currentPoint = null;

        // Main label
        JLabel labelTableName = new JLabel(tableName);
        this.add(labelTableName, gbc1);

        // Attributes panel
        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.gridx = 0;
        gbc2.gridy = 2;
        gbc2.weightx = 1;
        gbc2.weighty = 0.90;
        gbc2.fill = GridBagConstraints.BOTH;

        attributesPanel = new JPanel();
        attributesPanel.setLayout(new GridLayout(0, 1));

        this.add(attributesPanel, gbc2);

        // Join buttons
        GridBagConstraints gbc3 = new GridBagConstraints();
        gbc3.gridx = 0;
        gbc3.gridy = 3;
        gbc3.weightx = 1;
        gbc3.weighty = 0.05;
        gbc3.fill = GridBagConstraints.BOTH;

        String[] allAttributes = selectQuery.getAllAttributes();
        leftJoinComboBox = new JComboBox<>();
        innerJoinComboBox = new JComboBox<>();
        rightJoinComboBox = new JComboBox<>();

        leftJoinComboBox.addItem("LJ");
        innerJoinComboBox.addItem("J");
        rightJoinComboBox.addItem("RJ");
        for (String allAttribute : allAttributes) {
            leftJoinComboBox.addItem(allAttribute);
            innerJoinComboBox.addItem(allAttribute);
            rightJoinComboBox.addItem(allAttribute);
        }

        leftJoinComboBox.setBorder(new MatteBorder(2, 0, 2, 0, Color.black));
        //leftJoinComboBox.setHorizontalAlignment(JLabel.CENTER);
        innerJoinComboBox.setBorder(new MatteBorder(2, 2, 2, 2, Color.black));
        //innerJoinComboBox.setHorizontalAlignment(JLabel.CENTER);
        rightJoinComboBox.setBorder(new MatteBorder(2, 0, 2, 0, Color.black));
        //rightJoinComboBox.setHorizontalAlignment(JLabel.CENTER);
        JPanel joinPanel = new JPanel();
        joinPanel.setLayout(new GridLayout(1, 3));
        joinPanel.add(leftJoinComboBox);
        joinPanel.add(innerJoinComboBox);
        joinPanel.add(rightJoinComboBox);

        this.add(joinPanel, gbc3);

        labelTableName.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseMoved(e);
                Point point = getMousePosition();
                boxX = boxX + (point.x - currentPoint.x);
                boxY = boxY + (point.y - currentPoint.y);
                setBounds(boxX, boxY, width, height);
            }
        });
        labelTableName.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getButton() == MouseEvent.BUTTON3) {
                    TableBox.super.setVisible(false);
                    Iterator<TableBox> iterator = selectQuery.getTableBoxes().iterator();
                    while (iterator.hasNext()) {
                        TableBox obj = iterator.next();
                        if (obj.getTableName().equals(tableName)) {
                            iterator.remove(); // delete itself
                            break;
                        }
                    }
                    DefaultTableModel model = (DefaultTableModel) selectQuery.getTable().getModel();
                    model.setRowCount(0);
                    ArrayList<TableBox> tableBoxes = selectQuery.getTableBoxes();
                    for (TableBox tableBox : tableBoxes) {
                        ArrayList<JCheckBox> jCheckBoxes = tableBox.getCheckBoxes();
                        String tableName = tableBox.getTableName();
                        for (int j = 1; j < jCheckBoxes.size(); j++) {
                            Object[] rowData = {jCheckBoxes.get(j).getText(), "", tableName, "Unsorted", "Unsorted", "", ""};
                            model.addRow(rowData);
                        }
                    }
                    selectQuery.setLeftJoins(updateJoins(selectQuery.getLeftJoins()));
                    selectQuery.setInnerJoins(updateJoins(selectQuery.getInnerJoins()));
                    selectQuery.setRightJoins(updateJoins(selectQuery.getRightJoins()));
                }
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

    public JComboBox<String> getInnerJoinComboBox() {
        return innerJoinComboBox;
    }

    public JComboBox<String> getRightJoinComboBox() {
        return rightJoinComboBox;
    }

    public JComboBox<String> getLeftJoinComboBox() {
        return leftJoinComboBox;
    }

    private ArrayList<String> updateJoins(ArrayList<String> joins) {
        ArrayList<String> newJoins = new ArrayList<>();
        for (String join : joins) {
            String[] splitted = join.split(" ");
            if (!splitted[0].equals(tableName) && !splitted[3].equals(tableName)) {
                newJoins.add(join);
            }
        }
        return newJoins;
    }
}
