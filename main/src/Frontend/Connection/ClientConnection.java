package Frontend.Connection;

import Backend.Commands.Command;
import Backend.Parser;
import Backend.SocketServer.ErrorClient;
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

    public ClientConnection(int port) {
        try {
            socket = new Socket("localhost", port);
            printWriter = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            status = 0;
            ErrorClient.send("Server connection failed.");
        }
    }

    public void connect(int port) {
        try {
            socket = new Socket("localhost", port);
            printWriter = new PrintWriter(socket.getOutputStream(), true);
            status = 1;
        } catch (IOException e) {
            status = 0;
            ErrorClient.send("Server connection failed.");
        }
    }

    public void disconnect() {
        try {
            socket.close();
            printWriter.close();
        } catch (Exception e) {
            ErrorClient.send("Error with disconnect!");
            return;
        }
        status = 0;
    }

    public void send(String message) throws IOException {
        if (status == 1) {
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

    public List<String> getSelectResult() {
        List<String> data = new ArrayList<>();
        String receiveData;
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            receiveData = reader.readLine();
            while (!(receiveData.equals("null"))) {
                data.add(receiveData);
                receiveData = reader.readLine();
            }
        } catch (Exception e) {

        }
        return data;
    }
}
