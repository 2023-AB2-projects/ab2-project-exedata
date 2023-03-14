package Frontend;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class View extends JFrame implements ActionListener {
    private int startPosX = 50;
    private int startPosY = 50;
    private int startWidth = 750;
    private int startHeight = 750;
    private JTextArea textArea;
    private JButton button;
    private ClientConnection clientConnection;
    private JMenu menu;
    private JMenuItem connect;
    private JMenuItem disconnect;
    private JMenuBar menuBar;


    public View() {
        setBounds(startPosX, startPosY, startWidth, startHeight);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        textArea = new JTextArea();
        button = new JButton("RUN");
        button.addActionListener(this);
        menu = new JMenu("Menu");
        connect = new JMenuItem("Connect");
        disconnect = new JMenuItem("Disconnect");
        menuBar = new JMenuBar();

        menu.add(connect);
        menu.add(disconnect);
        menuBar.add(menu);
        connect.addActionListener(this);
        disconnect.addActionListener(this);

        connect.setEnabled(true);
        disconnect.setEnabled(false);
        button.setEnabled(false);

        setJMenuBar(menuBar);
        add(textArea, BorderLayout.CENTER);
        add(button, BorderLayout.SOUTH);

        clientConnection = new ClientConnection();

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == button) {
            if (clientConnection.getStatus() == 1) {
                try {
                    clientConnection.send("This is a message!");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            } else {
                System.out.println("First connect to the server!");
            }
        } else if (e.getSource() == connect) {
            clientConnection.connect(12000);
            connect.setEnabled(false);
            disconnect.setEnabled(true);
            button.setEnabled(true);
        } else if (e.getSource() == disconnect) {
            clientConnection.disconnect();
            connect.setEnabled(true);
            disconnect.setEnabled(false);
            button.setEnabled(false);
        }

    }
}
