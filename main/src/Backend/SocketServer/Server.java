package Backend.SocketServer;

import Backend.Commands.Command;
import Backend.MongoDBManagement.MongoDB;
import Backend.Parser;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private BufferedReader reader;
    private PrintWriter writer;
    private int port;

    public Server(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started successfully!");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        while (true) {
            try {
                clientSocket = serverSocket.accept();
                reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                writer = new PrintWriter(clientSocket.getOutputStream(), true);
                String command;
                while ((command = reader.readLine()) != null) {
                    System.out.println(command);
                    if (!command.startsWith("!GET")) {
                        Command a = Parser.commandType(command);
                        if (a != null) {
                            a.performAction();
                        }
                    } else {
                        sendData(command);
                    }
                }
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    private void sendData(String command) {
        String databaseName = command.split(" ")[2];
        String tableName = command.split(" ")[3];
        tableName = tableName.substring(0, tableName.length() - 1);
        MongoDB mongoDB = new MongoDB();
        mongoDB.createDatabaseOrUse(databaseName);
        MongoCollection<Document> documents = mongoDB.getDocuments(tableName);
        for (Document i : documents.find()) {
            writer.println(i.toJson());
        }
        writer.println("null");
        mongoDB.disconnectFromLocalhost();
    }
}
