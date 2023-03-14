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

    private final HttpServer httpServer;

    public Server(int serverPort) throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress(serverPort), 0);
        HttpContext httpContext = httpServer.createContext("/");
        httpContext.setHandler(new RequestHandler());
        // check other methods!!!
    }

    public void runServer() {
        httpServer.start();
        System.out.println("Server is running...");
    }

    private static class RequestHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            // Handling the request
            BufferedReader reader = new BufferedReader(new InputStreamReader(httpExchange.getRequestBody()));
            String message = reader.readLine();
            System.out.println("Server: I got your message: " + message);

            // process the request and send the answer
            // ... (analyze the text) - call the parser

            // Send response to the client
            OutputStream outputStream = httpExchange.getResponseBody();
            String response = "Hello client!";
            httpExchange.sendResponseHeaders(200, response.length());
            outputStream.write(response.getBytes());
            outputStream.close();
        }

    }
}
