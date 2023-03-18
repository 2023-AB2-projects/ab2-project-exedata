package Backend.HttpServer;
import Backend.Parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public Server(int port) throws IOException {
        ServerSocket serverSocket = new ServerSocket(12000);
        System.out.println("Server started successfully!");
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String command;
                while ((command = reader.readLine()) != null) {
                    System.out.println(command);
                    // feldolgozas
                    Parser.commandType(command).performAction();
                }
            } catch (Exception e) {

            }
        }
    }
}
