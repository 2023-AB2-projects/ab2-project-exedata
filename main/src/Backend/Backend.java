package Backend;

import Backend.SocketServer.Server;
import java.io.IOException;
public class Backend {
    public static void main(String[] args) throws IOException {
        Server server = new Server(12000);
    }
}
