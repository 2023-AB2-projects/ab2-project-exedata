package Backend;

import Backend.SocketServer.Server;

import java.io.IOException;

public class Backend {
    public static int numberOfInsertedRows = -1;
    public static int numberOfDeletedRows = -1;
    public static boolean goodInsert = false;
    public static boolean goodDelete = false;
    public static int end = -1;
    public static void main(String[] args) throws IOException {
        Thread server = new Thread(new Server(12000));
        server.start();
    }
}
