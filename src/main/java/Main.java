import org.apache.commons.cli.*;

import server.KVServer;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Options options = new Options();
        Option portOption = new Option("p", "port", true, "client socket port number");
        portOption.setRequired(true);
        Option rpcPortOption = new Option("rpc", "rpcPort", true, "rpc server port number");
        rpcPortOption.setRequired(true);
        options.addOption(portOption);
        options.addOption(rpcPortOption);
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);
            System.exit(1);
        }

        int portNumber = Integer.parseInt(cmd.getOptionValue("p"));
        int rpcPortNumber = Integer.parseInt(cmd.getOptionValue("rpc"));
        if (
                0 > portNumber
                || portNumber > 65536
                || 0 > rpcPortNumber
                || rpcPortNumber > 65536
                || rpcPortNumber == portNumber
        ) {
            System.out.println("port and rpc port must be an integer between 0 to 65536");
            System.exit(1);
        }
        KVServer kvServer = new KVServer(portNumber, rpcPortNumber);
        kvServer.run();
        ( new Scanner(System.in) ).nextLine();
        kvServer.stop();
    }
}
