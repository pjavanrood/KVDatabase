package server;

import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import Client.Client;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ClientHandlerAgentTest {
    static final String URL = "localhost";
    static final int PORT = 1010;
    static final int MAX_CLIENTS = 20;
    static Random random;
    static ClientHandlerAgent clientHandlerAgent;
    static Thread clientHandlerThread;
    static Client client1, client2;
    static ArrayList<Client> clientsList = new ArrayList<>();

    @BeforeAll
    public static void setup() throws InterruptedException {
        clientHandlerAgent = new ClientHandlerAgent(PORT, MAX_CLIENTS);
        clientHandlerThread = new Thread(() -> {
            clientHandlerAgent.run();
        });
        clientHandlerThread.start();
        Thread.sleep(4000);
        random = new Random();
    }

    @Test
    public void testSingleClient() {
        client1 = new Client(URL, PORT);
        assertTrue( client1.setup() );
        Optional<String> response;
        Map<String, Object> responseMap;
        for (int i = 0; i < 10; i++) {
            client1.send(Map.of(
                    "method", "put",
                    "key", "k1",
                    "value", String.valueOf(i)
            ));
            response = client1.receive();
            assertTrue( response.isPresent() );
            responseMap = new JSONObject( response.get() ).toMap();
            assertFalse( responseMap.containsKey("error") );

            client1.send(Map.of(
                    "method", "get",
                    "key", "k1"
            ));
            response = client1.receive();
            assertTrue( response.isPresent() );
            responseMap = new JSONObject( response.get() ).toMap();
            assertFalse( responseMap.containsKey("error") );
            assertTrue( responseMap.containsKey("result") );
            assertEquals( responseMap.get("result"), String.valueOf(i) );
        }
        client1.close();
    }

    @Test
    public void testSingleWrSingleRe() {
        client1 = new Client(URL, PORT);
        client2 = new Client(URL, PORT);
        assertTrue( client1.setup() && client2.setup() );
        Optional<String> response;
        Map<String, Object> responseMap;
        for (int i = 0; i < 10; i++) {
            client1.send(Map.of(
                    "method", "put",
                    "key", "k1",
                    "value", String.valueOf(i)
            ));
            response = client1.receive();
            assertTrue( response.isPresent() );
            responseMap = new JSONObject( response.get() ).toMap();
            assertFalse( responseMap.containsKey("error") );

            client2.send(Map.of(
                    "method", "get",
                    "key", "k1"
            ));
            response = client2.receive();
            assertTrue( response.isPresent() );
            responseMap = new JSONObject( response.get() ).toMap();
            assertFalse( responseMap.containsKey("error") );
            assertTrue( responseMap.containsKey("result") );
            assertEquals( responseMap.get("result"), String.valueOf(i) );
        }
        client1.close();
        client2.close();
    }

    @Test
    public void testMultiClient() {
        for (int i = 0; i < MAX_CLIENTS; i++) {
            clientsList.add( new Client(URL, PORT) );
            assertTrue( clientsList.get(i).setup() );
        }
        Map<String, String> testKVMap = new HashMap<>();
        Optional<String> response;
        Map<String, Object> responseMap;
        int action;
        String key, value;
        for (Client client : clientsList) {
            action = random.nextInt(2);
            key = String.valueOf( random.nextInt(5) );
            value = String.valueOf( random.nextInt(1000) );
            if (action % 2 == 0) {
                client.send(Map.of(
                        "method", "put",
                        "key", key,
                        "value", value
                ));
                testKVMap.put(key, value);
                client.receive();
            } else {
                client.send(Map.of(
                        "method", "get",
                        "key", key
                ));
                response = client.receive();
                assertTrue( response.isPresent() );
                responseMap = new JSONObject( response.get() ).toMap();
                if ( testKVMap.containsKey(key) ) {
                    assertFalse(responseMap.containsKey("error"));
                    assertTrue(responseMap.containsKey("result"));
                    assertEquals(responseMap.get("result"), testKVMap.get(key));
                } else {
                    assertTrue(responseMap.containsKey("error"));
                }
            }
        }
        System.out.println(testKVMap);
        Client client = clientsList.get(0);
        for (String k : testKVMap.keySet() ) {
            client.send(Map.of(
                    "method", "get",
                    "key", k
            ));
            response = client.receive();
            assertTrue( response.isPresent() );
            responseMap = new JSONObject( response.get() ).toMap();
            assertFalse( responseMap.containsKey("error") );
            assertTrue( responseMap.containsKey("result") );
            assertEquals( responseMap.get("result"), testKVMap.get(k) );
        }
        clientsList.forEach(Client::close);
    }


    @AfterAll
    public static void cleanUp() {
        if (client1 != null)
            client1.close();
        if (client2 != null)
            client2.close();
        clientsList.forEach(Client::close);
        clientHandlerThread.interrupt();
    }

}
