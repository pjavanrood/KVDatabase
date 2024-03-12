package server;

import datastore.KVPair;
import datastore.SynchMap;
import replication.ReplicationAgent;
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
    boolean replicationFlag;
    ReplicationAgent replicationAgent;

    public ClientHandler(SynchMap kvMap, Socket clientSocket, ReplicationAgent replicationAgent) throws IOException {
        this.kvMap = kvMap;
        this.client = clientSocket;
        this.inputStream = new BufferedReader( new InputStreamReader( clientSocket.getInputStream() ) );
        this.outputStream = new PrintWriter( clientSocket.getOutputStream(), true );
        this.replicationFlag = replicationAgent != null;
        this.replicationAgent = replicationAgent;
    }

    public void handleRequest(String request) {
        System.out.printf("[ClientHandler-%d]Request: %s\n", client.getLocalPort(), request);
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
            case "put" -> handlePutRequest(requestMap);
            case "get" -> handleGetRequest(requestMap);
            default -> handleInvalidRequest();
        }
    }

    synchronized public void handlePutRequest(Map<String, String> requestMap) {
        final String key = requestMap.get("key");
        final String value = requestMap.get("value");
        this.kvMap.put(key, value);
        Optional<KVPair> kvPairOpt = this.kvMap.getKVPair(key);
        if (kvPairOpt.isEmpty()) {
            outputStream.println( RequestParser.parseMessageMap( Map.of("error", "Server-side Error") ) );
            throw new RuntimeException("Inconsistent Key-Value Map");
        }
        this.replicationAgent.getReplicationPeers().forEach(peer -> {
            peer.rpcPutKeyValue(
                    kvPairOpt.get()
            );
        });
        outputStream.println( RequestParser.parseMessageMap( Map.of("result", "ok") ) );
    }

    public void handleGetRequest(Map<String, String> requestMap) {
        Optional<String> result = this.kvMap.get(requestMap.get("key"));
        if ( result.isPresent() ) {
            outputStream.println( RequestParser.parseMessageMap( Map.of("result", result.get()) ) );
        } else {
            outputStream.println( RequestParser.parseMessageMap( Map.of("error", "key not found") ) );
        }
    }

    public void handleInvalidRequest() {
        outputStream.println( RequestParser.parseMessageMap( Map.of("error", "invalid request") ) );
    }

    @Override
    public void run() {
        while ( !client.isClosed() && !Thread.interrupted() ) {
            try {
                String request = inputStream.readLine();
                if (request != null) handleRequest(request);
                else break;
            } catch ( IOException e ) {
                System.err.println("[ClientHandler]: Error reading request");
                System.err.println(e.getMessage());
            }
        }
        try {
            inputStream.close();
            client.close();
        } catch (IOException e) {
            System.err.println("[ClientHandler]: Error closing socket");
            System.err.println(e.getMessage());
        }
    }
}
