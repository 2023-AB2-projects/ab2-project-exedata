package Backend.SocketServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ErrorClient {
    public static void send(String message) {
        Socket clientSocket;
        OutputStream out;
        PrintWriter writer;
        try {
            clientSocket = new Socket("localhost", 12001);
            out=clientSocket.getOutputStream();
            writer = new PrintWriter(out, true);

            System.out.println("Error client started successfully!");
            writer.println(message);
            System.out.println("I sent this error message: " + message);
            out.close();
            clientSocket.close();
            System.out.println("Error client stop successfully!");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
