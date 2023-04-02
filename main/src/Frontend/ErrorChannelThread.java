package Frontend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class ErrorChannelThread extends Thread {
    private PanelDown panelDown;

    public ErrorChannelThread(PanelDown panelDown) {
        this.panelDown = panelDown;
        panelDown.getErrorLabel().setText("Hiba uzenet!");
    }

    @Override
    public void run() {
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(12001);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Error socket started successfully!");
        String errorMassage;
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                while ((errorMassage = reader.readLine()) != null) {
                    System.out.println("I receive this error massage: " + errorMassage);
                    panelDown.getErrorLabel().setText(errorMassage);
                }
            } catch (Exception e) {
            }
        }
    }
}
