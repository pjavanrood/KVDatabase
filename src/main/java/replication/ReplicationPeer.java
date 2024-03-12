package replication;

import datastore.KVPair;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import utils.Result;

import java.util.List;
import java.util.Objects;

public class ReplicationPeer {
    String peerId;
    String grpcUrl;
    int grpcPortNumber;
    ManagedChannel channel;
    ReplicationServiceGrpc.ReplicationServiceBlockingStub stub;

    public ReplicationPeer(String peerId, String url, int portNumber) {
        this.peerId = peerId;
        this.grpcUrl = url;
        this.grpcPortNumber = portNumber;
        this.channel = ManagedChannelBuilder.forAddress(url, portNumber).usePlaintext().build();
        this.stub = ReplicationServiceGrpc.newBlockingStub(this.channel);
    }

    public Result<KVPair> rpcGetKeyValue(String key) {
        try {
            KVPairResponse response = stub.getKeyValue(
                    KeyRequest.newBuilder().setKey(key).build()
            );
            KVPair kvPair = new KVPair(
                    response.getKey(),
                    response.getValue(),
                    response.getVersion()
            );
            return Result.success(kvPair);
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    public Result<Boolean> rpcIsValueEqual(KVPair kvPair) {
        try {
            List<String> kvPairCast = kvPair.get();
            BooleanResponse response = stub.isKeyValueEqual(
                    KVPairRequest.newBuilder()
                            .setKey(kvPairCast.get(0))
                            .setValue(kvPairCast.get(1))
                            .setVersion(Integer.parseInt(kvPairCast.get(2)))
                            .build()
            );
            return Result.success(response.getEquals());
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    public Result<Boolean> rpcPutKeyValue(KVPair kvPair) {
        try {
            List<String> kvPairCast = kvPair.get();
            BooleanResponse response = stub.putKeyValue(
                    KVPairRequest.newBuilder()
                            .setKey(kvPairCast.get(0))
                            .setValue(kvPairCast.get(1))
                            .setVersion(Integer.parseInt(kvPairCast.get(2)))
                            .build()
            );
            return Result.success(response.getEquals());
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReplicationPeer that = (ReplicationPeer) o;

        return (
                grpcPortNumber == that.grpcPortNumber
                && Objects.equals(peerId, that.peerId)
                && Objects.equals(grpcUrl, that.grpcUrl)
        );
    }
}
