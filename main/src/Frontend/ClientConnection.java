package Frontend;

import java.io.*;
import java.net.*;

public class ClientConnection {
    private static Socket socket;
    private static PrintWriter printWriter;

    private static int status = 1;

    public ClientConnection() {
        try {
            socket = new Socket("localhost", 12000);
            printWriter = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            status = 0;
            System.out.println("Server connection failed.");
        }
    }

    public void connect(int port) {
        try {
            socket = new Socket("localhost", 12000);
            System.out.println("Connected to server");
            status = 1;
        } catch (IOException e) {
            status = 0;
            System.out.println("Server connection failed.");
        }
    }

    public void disconnect() {
        try {
            socket.close();
        } catch (Exception e) {
            System.out.println("Error with disconnect!");
            return;
        }
        System.out.println("Disconnected from the server");
        status = 0;
    }

    public static void send(String message) throws IOException {
        if (status == 1) {
            message = message.replaceAll("\n", " ");
            message = message.replaceAll("\t", " ");
            message = message.replaceAll("\s+", " ");
            message = message.replaceAll("^\s+", "");
            System.out.println("I sent this command: " + message);
            printWriter.println(message);
        }
    }
}
