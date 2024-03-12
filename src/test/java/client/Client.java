package client;

import utils.ErrorHandler;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

public class Client {
    String url;
    int portNumber;
    Socket socket;
    PrintWriter out;
    BufferedReader in;

    public Client(String url, int portNumber) {
        this.url = url;
        this.portNumber = portNumber;
    }

    public boolean setup() {
        try {
            socket = new Socket(url, portNumber);
            out = new PrintWriter( socket.getOutputStream(), true );
            in = new BufferedReader(
                    new InputStreamReader( socket.getInputStream() )
            );
        } catch ( IOException e ) {
            ErrorHandler.printException("client", "Error Initializing socket", e );
            return false;
        }
        return true;
    }

    public void send(Map<String, String> requestMap) {
        JSONObject requestJSON = new JSONObject(requestMap);
        out.println(requestJSON);
    }

    public Optional<ArrayList<String>> receive(int messagesCount) {
        ArrayList<String> receivedMessages = new ArrayList<>();
        try {
            String response;
            for (int i = 0; i < messagesCount; i++) {
                response = in.readLine();
                receivedMessages.add(response);
            }
        } catch ( IOException e ) {
            ErrorHandler.printException("client", "Error reading response", e );
            return Optional.empty();
        }
        return Optional.of(receivedMessages);
    }

    public Optional<String> receive() {
        try {
            String response = in.readLine();
            return Optional.of(response);
        } catch ( IOException e ) {
            ErrorHandler.printException("client", "Error reading response", e );
            return Optional.empty();
        }
    }

    public void close() {
        try {
            socket.close();
        } catch ( IOException ignored) { }
    }
}
