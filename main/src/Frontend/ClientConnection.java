package Frontend;

import Backend.Commands.Command;
import Backend.Parser;
import com.mongodb.client.FindIterable;
import org.bson.Document;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class ClientConnection {
    private Socket socket;
    private PrintWriter printWriter;
    private BufferedReader reader;
    private int status = 1;
    private int port;

    public ClientConnection(int port) {
        this.port = port;
        try {
            socket = new Socket("localhost", port);
            printWriter = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            status = 0;
            System.out.println("Server connection failed.");
        }
    }

    public void connect(int port) {
        try {
            socket = new Socket("localhost", port);
            printWriter = new PrintWriter(socket.getOutputStream(), true);
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
            printWriter.close();
        } catch (Exception e) {
            System.out.println("Error with disconnect!");
            return;
        }
        System.out.println("Disconnected from the server");
        status = 0;
    }

    public void send(String message) throws IOException {
        if (status == 1) {
            message = message.replaceAll(System.lineSeparator(), " ");
            message = message.replaceAll("\t", " ");
            message = message.replaceAll("\s+", " ");
            message = message.replaceAll("^\s+", "");
            System.out.println("I sent this command: " + message);
            printWriter.println(message);
        }
    }

    public List<Document> getData(String databaseName, String tableName) {
        List<Document> data = new ArrayList<>();
        printWriter.println("!GET DATA " + databaseName + " " + tableName + ";");
        Document receiveData;
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while ((receiveData = Document.parse(reader.readLine())) != null) {
                data.add(receiveData);
            }
            reader.close();
        } catch (Exception e) {

        }
        return data;
    }
}
