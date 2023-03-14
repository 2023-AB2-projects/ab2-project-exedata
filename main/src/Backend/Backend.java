package Backend;

import Backend.HttpServer.Server;

import java.io.IOException;

public class Backend {
    public static void main(String[] args) throws IOException {
        // Create the HTTP server and start listening to requests
        Server server = new Server(12000);
        server.runServer();
    }
}
