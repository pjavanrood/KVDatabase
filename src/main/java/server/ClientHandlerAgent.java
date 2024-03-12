package server;

import datastore.SynchMap;
import replication.ReplicationAgent;
import utils.ErrorHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientHandlerAgent {
    static int WORKERS_MAX = 4;
    SynchMap keyValueMap;
    int portNumber;
    int workersMax;
    ServerSocket socket;
    ArrayList<Socket> clients;
    boolean replicationFlag;
    ReplicationAgent replicationAgent;

    public ClientHandlerAgent(SynchMap keyValueMap, ReplicationAgent replicationAgent, int portNumber, int workersMax) {
        this.keyValueMap = keyValueMap;
        this.portNumber = portNumber;
        this.workersMax = workersMax;
        this.socket = null;
        this.clients = new ArrayList<>();
        this.replicationFlag = replicationAgent != null;
        this.replicationAgent = replicationAgent;
        System.out.printf("[ClientHandlerAgent]: Initialized on Port %d\n", portNumber);
    }

    public ClientHandlerAgent(SynchMap keyValueMap, ReplicationAgent replicationAgent, int portNumber) {
        this(keyValueMap, replicationAgent, portNumber, WORKERS_MAX);
    }

    public void run() {
        System.out.printf("[ClientHandlerAgent-%d]: Running ....\n", portNumber);
        ExecutorService workersPool = Executors.newFixedThreadPool(workersMax);
        try {
            socket = new ServerSocket(portNumber);
            while ( !Thread.interrupted() ) {
                Socket client = socket.accept();
                clients.add( client );
                ClientHandler clientHandler = new ClientHandler(keyValueMap, client, replicationAgent);
                workersPool.execute( clientHandler );
            }
        } catch ( IOException e ) {
            ErrorHandler.printException(
                    "ClientHandlerAgent-" + portNumber,
                    socket == null ? "Error initializing Socket" : "Error accepting connection",
                    e
            );
        } finally {
            System.out.printf("[ClientHandlerAgent-%d]: Shutting Down all workers\n", portNumber);
            workersPool.shutdownNow();
            if (socket != null && !socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    ErrorHandler.printException( "ClientHandlerAgent" + portNumber, "Error closing Socket", e);
                }
            }
        }
    }

    public void stop() {
        try {
            socket.close();
            clients.forEach(client -> {
                try {
                    client.close();
                } catch (IOException ignore) { }
            });
        } catch (IOException ignore) { }
    }
}
