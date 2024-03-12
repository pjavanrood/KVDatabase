package replication;

import datastore.KVPair;
import datastore.SynchMap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import utils.Result;

import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class ReplicationAgentTest {
    static SynchMap kvMap;
    static ReplicationAgent agent;
    static ReplicationPeer client;
    static Random random;

    @BeforeAll
    public static void setup() {
        kvMap = new SynchMap();
        agent = new ReplicationAgent(1010, kvMap);
        agent.startGrpcServer();
        client = new ReplicationPeer("id-0", "localhost", 1010);
        random = new Random();
    }

    @Test
    public void testPutKeyValue() {
        String k1 = String.valueOf(random.nextInt());
        String v1 = String.valueOf(random.nextInt());
        Result<Boolean> response = client.rpcPutKeyValue(
                new KVPair(k1, v1)
        );
        assertTrue(response.isSuccess());
        Optional<String> result = kvMap.get(k1);
        assertTrue(result.isPresent());
        assertEquals(result.get(), v1);
        kvMap.clear();
    }

    @Test
    public void testGetKeyValue() {
        String k1 = String.valueOf(random.nextInt());
        String v1 = String.valueOf(random.nextInt());
        kvMap.put(k1, v1);
        Result<KVPair> response = client.rpcGetKeyValue(k1);
        assertTrue(response.isSuccess());
        assertEquals(response.get().getValue(), v1);
        kvMap.clear();
    }

    @Test
    public void testPutGetKeyValue1() {
        String k1 = String.valueOf(random.nextInt());
        String v1 = String.valueOf(random.nextInt());
        Result<Boolean> responsePut = client.rpcPutKeyValue(
                new KVPair(k1, v1)
        );
        assertTrue(responsePut.isSuccess());
        Result<KVPair> responseGet = client.rpcGetKeyValue(k1);
        assertTrue(responseGet.isSuccess());
        assertEquals(responseGet.get().getValue(), v1);
        assertEquals(responseGet.get().getVersion(), 0);
        kvMap.clear();
    }

    @Test
    public void testPutGetKeyValue2() {
        String k1 = String.valueOf(random.nextInt());
        String v1 = String.valueOf(random.nextInt());
        String v2 = String.valueOf(random.nextInt());
        Result<Boolean> responsePut = client.rpcPutKeyValue(
                new KVPair(k1, v1)
        );
        assertTrue(responsePut.isSuccess());
        Result<KVPair> responseGet = client.rpcGetKeyValue(k1);
        assertTrue(responseGet.isSuccess());
        assertEquals(responseGet.get().getValue(), v1);
        assertEquals(responseGet.get().getVersion(), 0);
        responsePut = client.rpcPutKeyValue(
                new KVPair(k1, v2, 1)
        );
        assertTrue(responsePut.isSuccess());
        Optional<KVPair> optKVPair = kvMap.getKVPair(k1);
        assertTrue(optKVPair.isPresent());
        assertEquals(optKVPair.get().getValue(), v2);
        assertEquals(optKVPair.get().getVersion(), 1);
        kvMap.clear();
    }

    @Test
    public void testPutKeyValueFail1() {
        String k1 = String.valueOf(random.nextInt());
        String v1 = String.valueOf(random.nextInt());
        Result<Boolean> responsePut = client.rpcPutKeyValue(
                new KVPair(k1, v1, 1)
        );
        assertFalse(responsePut.isSuccess());
//        System.out.println(responsePut.getErrorMessage()); Must be "NOT_FOUND: Key Not Found and Version is Non-zero"
    }

    @Test
    public void testCheckKeyValueEqual() {
        String k1 = String.valueOf(random.nextInt());
        String v1 = String.valueOf(random.nextInt());
        kvMap.put(k1, v1);
        kvMap.put(k1, v1 + v1);
        Result<Boolean> responseCheck = client.rpcIsValueEqual(
                new KVPair(k1, v1)
        );
        assertTrue(responseCheck.isSuccess());
        assertFalse(responseCheck.get());
        responseCheck = client.rpcIsValueEqual(
                new KVPair(k1, v1 + v1)
        );
        assertTrue(responseCheck.isSuccess());
        assertFalse(responseCheck.get());
        responseCheck = client.rpcIsValueEqual(
                new KVPair(k1, v1 + v1, 1)
        );
        assertTrue(responseCheck.isSuccess());
        assertTrue(responseCheck.get());
        kvMap.clear();
    }
}
