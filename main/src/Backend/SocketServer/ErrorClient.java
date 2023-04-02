package Backend.SocketServer;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class ErrorClient {
    private static Socket socket;
    private static PrintWriter printWriter;
    private int status;

    public ErrorClient() {
        status = 0;
        while (status == 0) {
            try {
                socket = new Socket("localhost", 12001);
                printWriter = new PrintWriter(socket.getOutputStream(), true);
                status = 1;
            } catch (IOException e) {
                //System.out.println("Error server connection failed.");
                //throw e;
            }
        }

    }

    public void connect() {
        try {
            socket = new Socket("localhost", 12001);
            System.out.println("Connected to server");
        } catch (IOException e) {
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
    }

    public void send(String message) {
        if (checkConnection()) {
            printWriter.println(message);
            System.out.println("I sent this error message: " + message);
        }
    }

    public boolean checkConnection() {
        return socket.isConnected() && !socket.isClosed();
    }
}
