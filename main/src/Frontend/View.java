package Frontend;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class View extends JFrame implements ActionListener {
    private int startPosX=50;
    private int startPosY=50;
    private int startWidth=750;
    private int startHeight=750;
    private JTextField textField;
    private JButton Button;
    private ClientConnection c;

    public View() {
        setBounds(startPosX,startPosY,startWidth,startHeight);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        textField=new JTextField();
        Button=new JButton("Send");
        add(textField,BorderLayout.CENTER);
        add(Button,BorderLayout.SOUTH);
        Button.addActionListener(this);
        c=new ClientConnection();


        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        c.connect(12000);
        c.send("asd");
        //c.disconnect();
    }
}
