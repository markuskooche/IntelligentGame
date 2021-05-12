import server.ServerConnection;

public class  Main {

    public static void main(String[] args) {
        ServerConnection server = new ServerConnection();

        // parameter transfers are intercepted here and processed accordingly
        for (int i = 0; i < args.length; i=i+2) {
            switch (args[i]) {
                case "-h":
                    System.out.println();
                    System.out.println("  -i <IP>           change to a specific ip\n");
                    System.out.println("  -p <PORT>         change to a specific port\n");
                    System.out.println("  -a <0 or 1>       enable/disable set alpha beta pruning");
                    System.out.println("                    0 = DISABLE   |   1 = ENABLE\n");
                    System.out.println("  -n <0 or 1>       enable/disable move sorting");
                    System.out.println("                    0 = DISABLE   |   1 = ENABLE\n");
                    System.out.println("  -q <0 or 1>       enable/disable console output");
                    System.out.println("                    0 = DISABLE   |   1 = ENABLE\n");
                    System.out.println("  -r <0 or 1>       enable/disable reduce output");
                    System.out.println("                    0 = DISABLE   |   1 = ENABLE");
                    System.out.println();
                    System.exit(0);
                    break;
                case "-i":
                    String host = args[i+1];
                    if (!host.matches("^((25[0-5]|(2[0-4]|1[0-9]|[1-9]|)[0-9])(\\.(?!$)|$)){4}$")) {
                        System.err.println("ERROR: The entered IP is not valid!");
                        System.exit(1);
                    }
                    server.setHost(host);
                    break;
                case "-p":
                    int port = Integer.parseInt(args[i+1]);
                    if (port < 1 || port > 65535) {
                        System.err.println("ERROR: The entered PORT is not valid!");
                        System.exit(1);
                    }
                    server.setPort(port);
                    break;
                case "-a":
                    String alphaBetaEntry = args[i+1];
                    if (alphaBetaEntry.equals("0")) {
                        server.setAlphaBeta(false);
                    } else if (alphaBetaEntry.equals("1")) {
                        server.setAlphaBeta(true);
                    } else {
                        System.err.println("ERROR: Please set alpha beta pruning to 0 or 1!");
                        System.exit(1);
                    }
                    break;
                case "-n":
                    String sortingEntry = args[i+1];
                    if (sortingEntry.equals("0")) {
                        server.setMoveSorting(false);
                    } else if (sortingEntry.equals("1")) {
                        server.setMoveSorting(true);
                    } else {
                        System.err.println("ERROR: Please set move sorting to 0 or 1!");
                        System.exit(1);
                    }
                    break;
                case "-q":
                    String consoleEntry = args[i+1];
                    if (consoleEntry.equals("0")) {
                        server.setConsoleOutput(false);
                    } else if (consoleEntry.equals("1")) {
                        server.setConsoleOutput(true);
                    } else {
                        System.err.println("ERROR: Please set console output to 0 or 1!");
                        System.exit(1);
                    }
                    break;
                case "-r":
                    String reduceEntry = args[i+1];
                    if (reduceEntry.equals("0")) {
                        server.setReduceOutput(false);
                    } else if (reduceEntry.equals("1")) {
                        server.setReduceOutput(true);
                    } else {
                        System.err.println("ERROR: Please set console reduce to 0 or 1!");
                        System.exit(1);
                    }
                    break;
                default:
                    break;
            }
        }

        server.start();
    }
}
