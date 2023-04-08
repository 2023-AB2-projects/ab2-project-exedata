package Backend;

import Backend.SocketServer.Server;

import java.io.IOException;

public class Backend {
    public static void main(String[] args) throws IOException {
        Thread server = new Thread(new Server(12000));
        server.start();
        Thread transferDataServer = new Thread(new Server(12002));
        transferDataServer.start();
    }
}
