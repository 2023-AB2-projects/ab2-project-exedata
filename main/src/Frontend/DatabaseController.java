package Frontend;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;

public class DatabaseController {
    private final DatabaseFrame databaseFrame;
    private final ClientConnection clientConnection;

    public DatabaseController() {
        databaseFrame = new DatabaseFrame();
        clientConnection = new ClientConnection();
        clientConnection.connect(12000);
        databaseFrame.getPanelTop().getConnect().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clientConnection.connect(12000);
                databaseFrame.getPanelTop().getConnect().setEnabled(false);
                databaseFrame.getPanelTop().getDisconnect().setEnabled(true);
            }
        });
        databaseFrame.getPanelTop().getDisconnect().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clientConnection.disconnect();
                databaseFrame.getPanelTop().getConnect().setEnabled(true);
                databaseFrame.getPanelTop().getDisconnect().setEnabled(false);
            }
        });
        databaseFrame.getPanelCenter().getInputArea().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if (e.getKeyCode()==116) {
                    // elkuldes a szervernek
                    try {
                        String command = databaseFrame.getPanelCenter().getInputArea().getSelectedText();
                        if (command==null) {
                            command = databaseFrame.getPanelCenter().getInputArea().getText();
                        }
                        String[] commands = command.split( ";\n" );
                        for (String s : commands) {
                            ClientConnection.send(s);
                            // varom a valaszt
                        }

                    } catch (IOException ex) {
                        databaseFrame.getPanelCenter().getMessagesLabel().setText("Connection ERROR to server on 12000 port!");
                    }
                }
            }
        });
        databaseFrame.getPanelTop().getExit().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clientConnection.disconnect();
                databaseFrame.dispose();
                System.exit(0);
            }
        });
        TimerThread timerThread = new TimerThread(databaseFrame.getPanelTop());
        timerThread.run();
    }
}