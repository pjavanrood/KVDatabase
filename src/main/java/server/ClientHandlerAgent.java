package server;

import kvpair.SynchMap;
import utils.ErrorHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientHandlerAgent {
    static int WORKERS_MAX = 4;
    SynchMap keyValueMap;
    int portNumber;
    int workersMax;

    public ClientHandlerAgent(SynchMap keyValueMap, int portNumber, int workersMax) {
        this.keyValueMap = keyValueMap;
        this.portNumber = portNumber;
        this.workersMax = workersMax;
        System.out.printf("Server Initialized with Port %d\n", portNumber);
    }

    public ClientHandlerAgent(int portNumber, int workersMax) {
        this(new SynchMap(), portNumber, workersMax);
    }

    public ClientHandlerAgent(int portNumber) {
        this(new SynchMap(), portNumber, WORKERS_MAX);
    }

    public void run() {
        System.out.println("Running KV Server");
        ServerSocket socket = null;
        ExecutorService workersPool = Executors.newFixedThreadPool(workersMax);
        try {
            socket = new ServerSocket(this.portNumber);
            while ( true ) {
                Socket client = socket.accept();
                workersPool.execute( new ClientHandler(keyValueMap, client) );
            }
        } catch ( IOException e ) {
            ErrorHandler.printException(
                    "ClientHandlerAgent",
                    socket == null ? "Error initializing Socket" : "Error accepting connection",
                    e
            );
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    ErrorHandler.printException( "ClientHandlerAgent", "Error closing Socket", e);
                }
            }
        }
    }
}
