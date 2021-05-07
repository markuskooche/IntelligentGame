import server.ServerConnection;

public class Main {

    public static void main(String[] args) {
        String host = "127.0.0.1";
        int port = 7777;

        boolean consoleOutput = false;
        boolean alphaBeta = true;

        // parameter transfers are intercepted here and processed accordingly
        for (int i = 0; i < args.length; i=i+2) {
            switch (args[i]) {
                case "-h":
                    System.out.println("-i <IP>         change to a specific ip");
                    System.out.println("-p <PORT>       change to a specific port");
                    System.out.println("-a <0 / 1>      set alpha beta pruning");
                    System.out.println("                0 = OFF   |   1 = ON");
                    System.out.println("-q <0 / 1>      enable/disable console output");
                    System.out.println("                0 = DISABLE   |   1 = ENABLE");
                    System.exit(0);
                    break;
                case "-i":
                    host = args[i+1];
                    if (!host.matches("^((25[0-5]|(2[0-4]|1[0-9]|[1-9]|)[0-9])(\\.(?!$)|$)){4}$")) {
                        System.err.println("ERROR: The entered IP is not valid!");
                        System.exit(1);
                    }
                    break;
                case "-p":
                    port = Integer.parseInt(args[i+1]);
                    if (port < 1 || port > 65535) {
                        System.err.println("ERROR: The entered PORT is not valid!");
                        System.exit(1);
                    }
                    break;
                case "-a":
                    String alphaBetaEntry = args[i+1];
                    if (alphaBetaEntry.equals("0")) {
                        alphaBeta = false;
                    } else if (alphaBetaEntry.equals("1")) {
                        alphaBeta = true;
                    } else {
                        System.err.println("ERROR: Please set alpha beta pruning to 0 or 1!");
                        System.exit(1);
                    }
                    break;
                case "-q":
                    String consoleEntry = args[i+1];
                    if (consoleEntry.equals("0")) {
                        consoleOutput = false;
                    } else if (consoleEntry.equals("1")) {
                        consoleOutput = true;
                    } else {
                        System.err.println("ERROR: Please set console output to 0 or 1!");
                        System.exit(1);
                    }
                    break;
                default:
                    break;
            }
        }

        new ServerConnection(host, port, alphaBeta, consoleOutput);
    }
}
