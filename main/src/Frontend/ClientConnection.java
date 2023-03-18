package Frontend;

import java.io.*;
import java.net.*;

public class ClientConnection {
    private static Socket socket;
    private static PrintWriter printWriter;

    public ClientConnection() {
        try {
            socket = new Socket("localhost", 12000);
            printWriter = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            System.out.println("Can't connect to Server");
        }
    }

    public void connect(int port) {
        try {
            socket = new Socket("localhost", 12000);
            System.out.println("Reconnect to server");
        } catch (IOException e) {
            System.out.println("Can't connect to Server");
        }
    }

    public void disconnect() {
        try {
            socket.close();
        } catch (Exception e) {
            System.out.println("Error with disconnect!");
        }
        System.out.println("Disconnected from the server");
    }

    public static void send(String message) throws IOException {
        printWriter.println(message);


//        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
//        String inputLine;
//        StringBuffer response = new StringBuffer();
//        while ((inputLine = in.readLine()) != null) {
//            response.append(inputLine);
//        }
//        //in.close();
//
//        System.out.println("Server response: " + response.toString());


    }
}
