package Frontend;

import java.io.*;
import java.net.*;

public class ClientConnection {
    private int status;
    private int port;

    private HttpURLConnection con;

    public ClientConnection() {
        status = 0;
    }

    public void connect(int port) {
        this.port = port;
        try {
            URL url = new URL("http://localhost:" + port + "/");
            con = (HttpURLConnection) url.openConnection();
            status = 1;
            System.out.println("Connected to server on " + port + " port!");
        } catch (java.io.IOException e) {
            System.out.println("Can't connect to Server");
        }
    }

    public void disconnect() {
        con.disconnect();
        status = 0;
        System.out.println("Disconnected from the server");
    }

    public void send(String message) throws IOException {
        con.setRequestMethod("POST");
        con.setDoOutput(true);

        byte[] messageBytes = message.getBytes(); //encript

        OutputStream out;
        try {
            out = con.getOutputStream();
            out.write(messageBytes);
            out.flush();
            out.close();
            int responseCode = con.getResponseCode();
        } catch (Exception e) {
            System.out.println("Can't send this text!");
        }


//        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
//        String inputLine;
//        StringBuffer response = new StringBuffer();
//        while ((inputLine = in.readLine()) != null) {
//            response.append(inputLine);
//        }
//        in.close();
//
//        System.out.println("Server response: " + response.toString());


    }

    public int getStatus() {
        return status;
    }
}
