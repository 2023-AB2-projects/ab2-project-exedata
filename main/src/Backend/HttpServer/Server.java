package Backend.HttpServer;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private ServerSocket serverSocket;
    private Socket clientSocket;
    public Server(int port) throws IOException {
        serverSocket =  new ServerSocket(12000);
        System.out.println("Server started successfully!");
        while (true) {
            try {
                clientSocket = serverSocket.accept();
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String command;
                while ((command = reader.readLine()) != null) {
                    System.out.println("I got this command from the client: " + command);
                }
            } catch (Exception e) {

            }
        }
    }
}
