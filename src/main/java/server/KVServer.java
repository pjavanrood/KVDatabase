package server;

import kvpair.SynchMap;

public class KVServer {
    String url;
    int portNumber;
    SynchMap keyValueMap;
    ClientHandlerAgent clientHandlerAgent;
    Thread clientAgentThread;

    public KVServer(String url, int portNumber) {
        this.url = url;
        this.portNumber = portNumber;
        this.setup();
    }

    public void setup() {
        keyValueMap = new SynchMap();
        clientHandlerAgent = new ClientHandlerAgent(portNumber);
        clientAgentThread = new Thread(() -> { clientHandlerAgent.run(); });
    }

    public void run() {
        clientAgentThread.start();
    }

    public void stop() {
        clientAgentThread.interrupt();
    }
}
