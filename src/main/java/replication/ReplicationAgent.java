package replication;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import datastore.SynchMap;
import utils.ErrorHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReplicationAgent {
    SynchMap keyValueMap;
    Server grpcServer;
    ReplicationService replicationService;
    List<ReplicationPeer> replicationPeers;

    public ReplicationAgent(int portNumber, SynchMap keyValueMap) {
        this.keyValueMap = keyValueMap;
        this.grpcServer = ServerBuilder
                .forPort(portNumber)
                .addService( new ReplicationService(keyValueMap) )
                .build();
        this.replicationService = new ReplicationService(keyValueMap);
        this.replicationPeers = new ArrayList<>();
    }

    public boolean startGrpcServer() {
        try {
            grpcServer.start();
            return true;
        } catch (IOException exception) {
            ErrorHandler.printException(
                    "ReplicationAgent", "Failed to start grpc server", exception
            );
            return false;
        }
    }

    public void stopGrpcServer() {
        grpcServer.shutdownNow();
    }

    public boolean addReplicationPeer(String peerId, String url, int portNumber) {
        ReplicationPeer peer = new ReplicationPeer(peerId, url, portNumber);
        if ( this.replicationPeers.contains(peer) ) {
            return false;
        } else {
            this.replicationPeers.add(peer);
            return true;
        }
    }

    public List<ReplicationPeer> getReplicationPeers() {
        return replicationPeers;
    }
}
