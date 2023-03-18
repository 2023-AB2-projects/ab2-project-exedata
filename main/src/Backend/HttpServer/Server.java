package Backend.HttpServer;
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
                String full = null;
                while ((command = reader.readLine()) != null) {
                    full = full + command;
                    System.out.println(command);
                }
            } catch (Exception e) {

            }
        }
    }
}
