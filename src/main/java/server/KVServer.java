package server;

import datastore.SynchMap;
import replication.ReplicationAgent;

public class KVServer {
    int portNumber;
    int rpcPortNumber;
    SynchMap keyValueMap;
    ClientHandlerAgent clientHandlerAgent;
    Thread clientAgentThread;
    ReplicationAgent replicationAgent;

    public KVServer(int portNumber, int rpcPortNumber) {
        this.portNumber = portNumber;
        this.rpcPortNumber = rpcPortNumber;
        this.setup();
    }

    public void setup() {
        keyValueMap = new SynchMap();
        replicationAgent = new ReplicationAgent(rpcPortNumber, keyValueMap);
        clientHandlerAgent = new ClientHandlerAgent(keyValueMap, replicationAgent, portNumber);
        clientAgentThread = new Thread(() -> { clientHandlerAgent.run(); });
    }

    public void run() {
        clientAgentThread.start();
        replicationAgent.startGrpcServer();
    }

    public void stop() {
        clientHandlerAgent.stop();
        clientAgentThread.interrupt();
        replicationAgent.stopGrpcServer();
    }

    public boolean addPeer(String peerId, String url, int peerPort) {
        return replicationAgent.addReplicationPeer(peerId, url, peerPort);
    }
}
