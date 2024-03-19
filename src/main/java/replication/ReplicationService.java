package replication;

import com.google.rpc.Code;
import com.google.rpc.Status;
import datastore.KVPair;
import io.grpc.protobuf.StatusProto;
import io.grpc.stub.StreamObserver;
import datastore.SynchMap;

import java.util.List;
import java.util.Optional;

public class ReplicationService extends ReplicationServiceGrpc.ReplicationServiceImplBase {
    SynchMap keyValueMap;

    public ReplicationService(SynchMap keyValueMap) {
        super();
        this.keyValueMap = keyValueMap;
    }

    @Override
    public void getKeyValue(KeyRequest request, StreamObserver<KVPairResponse> responseObserver) {
        String key = request.getKey();
        Optional<KVPair> result = keyValueMap.getKVPair(key);
        if (result.isPresent()) {
            List<String> kvPairCast = result.get().get();
            KVPairResponse response = KVPairResponse.newBuilder()
                            .setKey(kvPairCast.get(0))
                            .setValue(kvPairCast.get(1))
                            .setVersion(Integer.parseInt(kvPairCast.get(2)))
                            .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } else {
            Status status = Status.newBuilder()
                    .setCode(Code.NOT_FOUND_VALUE)
                    .setMessage("Key Not Found!")
                    .build();
            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
        }
    }

    @Override
    public void isKeyValueEqual(KVPairRequest request, StreamObserver<BooleanResponse> responseObserver) {
        String key = request.getKey();
        String value = request.getValue();
        int version = request.getVersion();
        Optional<KVPair> result = keyValueMap.getKVPair(key);
        if (result.isPresent()) {
            boolean checkEquals = result.get().equalsKVPair(key, value, version);
            BooleanResponse response = BooleanResponse.newBuilder()
                    .setEquals(checkEquals)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } else {
            Status status = Status.newBuilder()
                    .setCode(Code.NOT_FOUND_VALUE)
                    .setMessage("Key Not Found!")
                    .build();
            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
        }
    }

    public void sendError(int code, String message, StreamObserver<BooleanResponse> responseObserver) {
        Status status = Status.newBuilder()
                .setCode(code)
                .setMessage(message)
                .build();
        responseObserver.onError(StatusProto.toStatusRuntimeException(status));
    }

    @Override
    public void commitKeyValue(KVPairRequest request, StreamObserver<BooleanResponse> responseObserver) {
        String key = request.getKey();
        String value = request.getValue();
        int version = request.getVersion();
        Optional<KVPair> result = keyValueMap.getKVPair(key);
        if (result.isPresent()) {
            boolean commitOk = result.get().commit(version);
            if (!commitOk) {
                sendError(Code.CANCELLED_VALUE, "Incompatible Versions!", responseObserver);
            } else {
                BooleanResponse response = BooleanResponse.newBuilder()
                        .setEquals(true)
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        } else {
            boolean commitResult = keyValueMap.commit(key, value, version);
            BooleanResponse response = BooleanResponse.newBuilder()
                    .setEquals(commitResult)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void unCommitKeyValue(KVPairRequest request, StreamObserver<BooleanResponse> responseObserver) {
        String key = request.getKey();
        int version = request.getVersion();
        Optional<KVPair> result = keyValueMap.getKVPair(key);
        result.ifPresent(kvPair -> kvPair.unCommit(version - 1));
        BooleanResponse response = BooleanResponse.newBuilder()
                .setEquals(true)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void putKeyValue(KVPairRequest request, StreamObserver<BooleanResponse> responseObserver) {
        String key = request.getKey();
        String value = request.getValue();
        int version = request.getVersion();
        Optional<KVPair> result = keyValueMap.getKVPair(key);
        if (result.isPresent()) {
            boolean updateOk = result.get().update(value, version);
            if (!updateOk) {
                sendError(Code.CANCELLED_VALUE, "Incompatible Versions!", responseObserver);
            } else {
                BooleanResponse response = BooleanResponse.newBuilder()
                        .setEquals(true)
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        } else {
            sendError(Code.NOT_FOUND_VALUE, "Key Not Found and Version is Non-zero! You must commit first!", responseObserver);
        }
    }
}
