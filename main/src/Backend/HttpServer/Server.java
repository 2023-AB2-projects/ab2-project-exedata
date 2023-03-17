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

public class Server {

    public Server(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        System.out.println("Server started successfully!");
        HttpContext context = server.createContext("/");
        context.setHandler(new RequestHandler());

        server.start();
    }

    private static class RequestHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(httpExchange.getRequestBody()));
            String message = reader.readLine();
            System.out.println("Server: I got your message: " + message);

            // Send the answer
            OutputStream outputStream = httpExchange.getResponseBody();
            String response = "Hello client!";
            httpExchange.sendResponseHeaders(200, response.length());
            outputStream.write(response.getBytes());
            outputStream.close();
        }

    }


    // Create HTTP server
    // Define a port

    // Listen to requests
    // Process the request
    // Should call the parser
    // Send response to the client
}
