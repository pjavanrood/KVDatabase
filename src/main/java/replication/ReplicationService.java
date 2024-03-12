package replication;

import com.google.rpc.Code;
import com.google.rpc.Status;
import datastore.KVPair;
import io.grpc.protobuf.StatusProto;
import io.grpc.stub.StreamObserver;
import datastore.SynchMap;
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
            responseObserver.onNext( result.get().getKeyValueResponse() );
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

    @Override
    public void putKeyValue(KVPairRequest request, StreamObserver<BooleanResponse> responseObserver) {
        String key = request.getKey();
        String value = request.getValue();
        int version = request.getVersion();
        Optional<KVPair> result = keyValueMap.getKVPair(key);
        if (result.isPresent()) {
            boolean updateOk = result.get().update(value, version - 1);
            if (!updateOk) {
                Status status = Status.newBuilder()
                        .setCode(Code.CANCELLED_VALUE)
                        .setMessage("Incompatible Versions!")
                        .build();
                responseObserver.onError(StatusProto.toStatusRuntimeException(status));
            } else {
                BooleanResponse response = BooleanResponse.newBuilder()
                        .setEquals(true)
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        } else {
            if (version != 0) {
                Status status = Status.newBuilder()
                        .setCode(Code.NOT_FOUND_VALUE)
                        .setMessage("Key Not Found and Version is Non-zero")
                        .build();
                responseObserver.onError(StatusProto.toStatusRuntimeException(status));
            } else {
                keyValueMap.put(key, value);
                BooleanResponse response = BooleanResponse.newBuilder()
                        .setEquals(true)
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        }
    }
}
