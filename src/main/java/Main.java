public class Main {

    public static void main(String[] args) {
        String host = "127.0.0.1";
        int port = 7777;

        // parameter transfers are intercepted here and processed accordingly
        for (int i = 0; i < args.length; i=i+2) {
            switch (args[i]) {
                case "-h":
                    System.out.println("-i <IP>     Change to a specific ip");
                    System.out.println("-p <PORT>   Change to a specific port");
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
                default:
                    break;
            }
        }

        new ServerConnection(host, port, 1);
    }
}
