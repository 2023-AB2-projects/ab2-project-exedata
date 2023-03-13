package Frontend;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class ClientConnection{
    private int status;
    private int port;
    private InetAddress addr;
    private Socket sock;

    public ClientConnection() {
        status=0;

    }

    public void connect(int port){
        this.port=port;
        try {
            sock = new Socket("127.0.0.1", port);
            addr = sock.getInetAddress();
            status=1;
            System.out.println("Connected to " + addr);
        } catch (java.io.IOException e) {
            System.out.println("Can't connect to Server");
            System.out.println(e);
        }
    }

    public void disconnect(){
        try{
            sock.close();
            status=0;
            System.out.println("Disconnected from the server");
        }catch (IOException e) {
            System.out.println("Can't disconnected from the server");
        }
    }
}
