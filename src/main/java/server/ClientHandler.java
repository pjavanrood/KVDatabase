package server;

import kvpair.SynchMap;
import utils.RequestParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.Optional;

public class ClientHandler implements Runnable {
    private final SynchMap kvMap;
    private final Socket client;
    private final BufferedReader inputStream;
    private final PrintWriter outputStream;

    public ClientHandler(SynchMap kvMap, Socket clientSocket) throws IOException {
        this.kvMap = kvMap;
        this.client = clientSocket;
        this.inputStream = new BufferedReader( new InputStreamReader( clientSocket.getInputStream() ) );
        this.outputStream = new PrintWriter( clientSocket.getOutputStream(), true );
    }

    public void handleRequest(String request) {
        System.out.printf("Request: %s\n", request);
        Map<String, String> requestMap = RequestParser.parseMessageString(request);
        if ( requestMap.containsKey("error") ) {
            outputStream.println(
                    RequestParser.parseMessageMap( Map.of(
                            "error", String.format("Invalid Request: %s", requestMap.get("error"))
                    ) )
            );
            return;
        }
        switch ( requestMap.get("method") ) {
            case "put" -> {
                this.kvMap.put(requestMap.get("key"), requestMap.get("value"));
                outputStream.println( RequestParser.parseMessageMap( Map.of("result", "ok") ) );
            }
            case "get" -> {
                Optional<String> result = this.kvMap.get(requestMap.get("key"));
                if ( result.isPresent() ) {
                    outputStream.println( RequestParser.parseMessageMap( Map.of("result", result.get()) ) );
                } else {
                    outputStream.println( RequestParser.parseMessageMap( Map.of("error", "key not found") ) );
                }
            }
            default -> {
                outputStream.println( RequestParser.parseMessageMap( Map.of("error", "invalid request") ) );
            }
        }
    }

    @Override
    public void run() {
        while ( !client.isClosed() ) {
            try {
                String request = inputStream.readLine();
                if (request != null) handleRequest(request);
                else break;
            } catch ( IOException e ) {
                System.err.println("[ClientHandler]: Error reading request");
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
        }
        try {
            this.client.close();
        } catch (IOException e) {
            System.err.println("[ClientHandler]: Error closing socket");
            System.err.println(e.getMessage());
        }
    }
}
