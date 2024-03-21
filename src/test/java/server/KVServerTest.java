package server;

import client.Client;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class KVServerTest {
    static KVServer server1, server2;
    static Client client1, client2;
    static int DELAY = 300;

    @BeforeAll
    public static void setup() throws InterruptedException {
        server1 = new KVServer(1010, 1011);
        server2 = new KVServer(1012, 1013);
        server1.addPeer("server2", "localhost", 1013);
        server2.addPeer("server1", "localhost", 1011);
        server1.run();
        server2.run();
        Thread.sleep(1000);
        client1 = new Client("localhost", 1010);
        client2 = new Client("localhost", 1012);
        assertTrue( client1.setup() );
        assertTrue( client2.setup() );
    }

    @Test
    public void testWriteRead() {
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
    }

    @Test
    public void testWriteRead2() throws InterruptedException {
        Optional<String> response;
        Map<String, Object> responseMap;

        for (int i = 0; i < 10; i++) {
            client1.send(Map.of(
                    "method", "put",
                    "key", "k1",
                    "value", String.valueOf(i)
            ));
            Thread.sleep(DELAY);
            client2.send(Map.of(
                    "method", "get",
                    "key", "k1"
            ));
            response = client2.receive();
            assertTrue(response.isPresent());
            responseMap = new JSONObject( response.get() ).toMap();
            assertFalse( responseMap.containsKey("error") );
            assertTrue( responseMap.containsKey("result") );
            assertEquals( responseMap.get("result"), String.valueOf(i) );

            response = client1.receive();
            assertTrue(response.isPresent());
            responseMap = new JSONObject(response.get()).toMap();
            assertFalse(responseMap.containsKey("error"));
        }
    }

    @Test
    public void testMultiWriter() throws InterruptedException {
        Optional<String> response;
        Map<String, Object> responseMap;
        Client writer, reader;

        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) {
                writer = client1;
                reader = client2;
            } else {
                writer = client2;
                reader = client1;
            }
            writer.send(Map.of(
                    "method", "put",
                    "key", "k1",
                    "value", String.valueOf(i)
            ));
            Thread.sleep(DELAY);
            reader.send(Map.of(
                    "method", "get",
                    "key", "k1"
            ));
            response = reader.receive();
            assertTrue(response.isPresent());
            responseMap = new JSONObject( response.get() ).toMap();
            assertFalse( responseMap.containsKey("error") );
            assertTrue( responseMap.containsKey("result") );
            assertEquals( responseMap.get("result"), String.valueOf(i) );

            response = writer.receive();
            assertTrue(response.isPresent());
            responseMap = new JSONObject(response.get()).toMap();
            assertFalse(responseMap.containsKey("error"));
        }
    }

    @AfterAll
    public static void cleanup() {
        client1.close();
        client2.close();
        server1.stop();
        server2.stop();
    }
}
