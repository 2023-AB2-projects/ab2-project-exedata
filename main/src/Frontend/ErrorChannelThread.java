package Frontend;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ErrorChannelThread extends Thread {
    private PanelDown panelDown;

    public ErrorChannelThread(PanelDown panelDown) {
        this.panelDown = panelDown;
        panelDown.getErrorLabel().setText("Here the error message!");
    }

    @Override
    public void run() {
        ServerSocket serverSocket = null;
        Socket clientSocket;
        BufferedReader reader;
        try {
            serverSocket = new ServerSocket(12001);
        } catch (IOException e) {
            System.out.println("Error with ErrorChannelThread!");
            return;
        }
        System.out.println("Error socket started successfully!");

        String errorMassage;
        while (true) {
            try {
                clientSocket = serverSocket.accept();
                reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                while ((errorMassage = reader.readLine()) != null) {
                    System.out.println("I receive this error massage: " + errorMassage);
                    panelDown.getErrorLabel().setText(panelDown.getErrorLabel().getText() + "\n " + errorMassage);
                }
            } catch (Exception e) {
            }
        }
    }
}
