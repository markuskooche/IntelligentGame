import server.ServerConnection;

/**
 * The Main class creates a new instance of a client.
 * It is possible to set various parameters to change the behavior of the client.
 *
 * @author Benedikt Halbritter
 * @author Iwan Eckert
 * @author Markus Koch
 */
public class Main {

    private static void printClient() {
        System.out.println(" ______          _    __  __             _       ");
        System.out.println("|___  /         | |  |  \\/  |           | |      ");
        System.out.println("   / / ___   ___| | _| \\  / | ___  _ __ | |_ ___ ");
        System.out.println("  / / / _ \\ / __| |/ / |\\/| |/ _ \\| '_ \\| __/ _ \\");
        System.out.println(" / /_| (_) | (__|   <| |  | | (_) | | | | ||  __/");
        System.out.println("/_____\\___/ \\___|_|\\_\\_|  |_|\\___/|_| |_|\\__\\___|");
        System.out.println();
        System.out.println("   Created by: Benedikt Halbritter");
        System.out.println("               Iwan Eckert");
        System.out.println("               Markus Koch");
    }

    private static void printHelp() {
        System.out.println();
        System.out.println("  -i <IP>           change to a specific ip\n");
        System.out.println("  -p <PORT>         change to a specific port\n");
        System.out.println("  -a <0 or 1>       enable/disable set alpha beta pruning");
        System.out.println("                    0 = DISABLE   |   1 = ENABLE\n");
        System.out.println("  -n <0 or 1>       enable/disable move sorting");
        System.out.println("                    0 = DISABLE   |   1 = ENABLE\n");
        System.out.println("  -m <0 or 1>       enable/disable monte carlo");
        System.out.println("                    0 = DISABLE   |   1 = ENABLE\n");
        System.out.println("  -q <0 or 1>       enable/disable console output");
        System.out.println("                    0 = DISABLE   |   1 = ENABLE\n");
        System.out.println("  -r <0 or 1>       enable/disable reduce output");
        System.out.println("                    0 = DISABLE   |   1 = ENABLE");
        System.out.println();
        System.exit(0);
    }

    public static void main(String[] args) {
        //printClient();
        ServerConnection server = new ServerConnection();

        // parameter transfers are intercepted here and processed accordingly
        for (int i = 0; i < args.length; i=i+2) {
            switch (args[i]) {
                // when help is called (no server connection)
                case "-h":
                    printHelp();
                    break;
                // here another IP address is set
                case "-i":
                    String host = args[i+1];
                    server.setHost(host);
                    break;
                // here another port is set
                case "-p":
                    int port = Integer.parseInt(args[i+1]);
                    server.setPort(port);
                    break;
                // here you can switch alpha-beta on and off
                case "-a":
                    String alphaBeta = args[i+1];
                    server.setAlphaBeta(alphaBeta);
                    break;
                // here you can switch move sorting on and off
                case "-n":
                    String moveSorting = args[i+1];
                    server.setMoveSorting(moveSorting);
                    break;
                // here you can switch monte carlo on and off
                case "-m":
                    String monteCarlo = args[i+1];
                    server.setMonteCarlo(monteCarlo);
                    break;
                // here you can switch console output completely on and off
                case "-q":
                    String consoleOutput = args[i+1];
                    server.setConsoleOutput(consoleOutput);
                    break;
                // here you can switch console reduce on and off
                case "-r":
                    String reduceOutput = args[i+1];
                    server.setReduceOutput(reduceOutput);
                    break;
                default:
                    break;
            }
        }

        server.start();
    }
}
