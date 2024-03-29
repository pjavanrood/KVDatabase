import server.KVServer;

import java.util.Scanner;

public class SimpleCluster {
    public static void main(String[] args) {
        KVServer server1 = new KVServer(1010, 1011);
        KVServer server2 = new KVServer(1012, 1013);
        KVServer server3 = new KVServer(1014, 1015);

        server1.addPeer("serer2", "localhost", 1013);
        server1.addPeer("serer3", "localhost", 1015);

        server2.addPeer("serer1", "localhost", 1011);
        server2.addPeer("serer3", "localhost", 1015);

        server3.addPeer("serer1", "localhost", 1011);
        server3.addPeer("serer2", "localhost", 1013);

        server1.run();
        server2.run();
        server3.run();
        ( new Scanner(System.in) ).nextLine();
        server1.stop();
        server2.stop();
        server3.stop();
    }
}
