import server.ClientHandlerAgent;
import server.KVServer;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        int PORT = 1010;
        KVServer kvServer = new KVServer("localhost", PORT);
        kvServer.run();
//        System.out.println("Here");
//        ClientHandlerAgent server = new ClientHandlerAgent(PORT);
//        server.run();
    }
}
