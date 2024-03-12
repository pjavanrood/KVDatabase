package server;

import client.Client;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class KVServerTest {
    @Test
    public void testDoubleServers() throws InterruptedException {
        KVServer server1 = new KVServer(1010, 1011);
        KVServer server2 = new KVServer(1012, 1013);
        server1.addPeer("server2", "localhost", 1013);
        server2.addPeer("server1", "localhost", 1011);
        server1.run();
        server2.run();
        Thread.sleep(1000);

        Client client1 = new Client("localhost", 1010);
        Client client2 = new Client("localhost", 1012);
        assertTrue( client1.setup() );
        assertTrue( client2.setup() );

        Optional<String> response;
        Map<String, Object> responseMap;
        client1.send(Map.of(
                "method", "put",
                "key", "k1",
                "value", "this is value 1"
        ));
        response = client1.receive();
        assertTrue(response.isPresent());
        responseMap = new JSONObject(response.get()).toMap();
        assertFalse(responseMap.containsKey("error"));

        client2.send(Map.of(
                "method", "get",
                "key", "k1"
        ));
        response = client2.receive();
        assertTrue(response.isPresent());
        responseMap = new JSONObject( response.get() ).toMap();
        assertFalse( responseMap.containsKey("error") );
        assertTrue( responseMap.containsKey("result") );
        assertEquals( responseMap.get("result"), "this is value 1" );

        client1.close();
        client2.close();
        server1.stop();
        server2.stop();
    }
}
