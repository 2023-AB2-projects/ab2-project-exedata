package Backend.HttpServer;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;

public class Server {

    public Server(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        System.out.println("Server started successfully!");
        HttpContext context = server.createContext("/");
        server.start();

        context.setHandler(new RequestHandler());
    }

    private static class RequestHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(httpExchange.getRequestBody()));
            String message = reader.readLine();
            System.out.println("Message = " + message);
        }

    }


    // Create HTTP server
    // Define a port

    // Listen to requests
    // Process the request
    // Should call the parser
    // Send response to the client
}
