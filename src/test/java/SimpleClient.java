import utils.ErrorHandler;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class SimpleClient {
    private static String URL = "localhost";
    private static final int PORT = 1010;

    public static void main(String[] args) {
        Socket socket = null;
        PrintWriter out = null;
        BufferedReader in = null;
        try {
            socket = new Socket(URL, PORT);
            out = new PrintWriter( socket.getOutputStream(), true );
            in = new BufferedReader(
                    new InputStreamReader( socket.getInputStream() )
            );
        } catch ( IOException e ) {
            ErrorHandler.printException("client", "Error Initializing socket", e );
            return;
        }
        Scanner scanner = new Scanner(System.in);
        while ( true ) {
            System.out.println("> Request");
            String input = scanner.nextLine();
            if ( input.equals("quit") ) {
                break;
            } else {
                try {
                    String[] splitInput = input.split(" ");
                    Map<String, String> requestMap = new HashMap<>(Map.of(
                            "method", splitInput[0],
                            "key", splitInput[1]
                    ));
                    if (splitInput[0].equals("put"))
                        requestMap.put("value", splitInput[2]);
                    JSONObject requestJSON = new JSONObject(requestMap);
                    System.out.println(requestJSON);
                    out.println(requestJSON);
                } catch (Exception e) {
                    System.err.println("Error: Invalid request");
                    System.err.println(e.getMessage());
                    continue;
                }
                try {
                    String response = in.readLine();
                    System.out.println("[Response]:");
                    System.out.println(response);
                } catch ( IOException e ) {
                    ErrorHandler.printException("client", "Error reading response", e );
                }
            }
        }
        scanner.close();
    }
}
