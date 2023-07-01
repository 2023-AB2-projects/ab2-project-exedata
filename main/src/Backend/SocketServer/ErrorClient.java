package Backend.SocketServer;

import java.io.*;
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

            writer.println(message);
            out.close();
            clientSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
