import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.*;

public class ServerConnection {

    private Game game;
    private byte ourPlayer;
    private Socket socket;
    private boolean running = true;

    public ServerConnection(String host, int port, int groupNumber) {
        try {
            socket = new Socket(InetAddress.getByName(host), port);
            byte group = (byte) groupNumber;
            byte[] message = new byte[] {1, 0, 0, 0, 1, group};

            sendMessage(message);
            play();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void play() throws IOException {
        while (running) {
            receiveMessage();
        }
    }

    private void sendMessage(byte[] message) throws IOException {
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(message);
    }

    private void receiveMessage() throws IOException {
        InputStream inputStream = socket.getInputStream();
        byte[] messageHeader = new byte[5];
        inputStream.read(messageHeader, 0, 5);

        // create an integer from the byte array
        int messageLength = messageHeader[1] << 24;
        messageLength += messageHeader[2] << 16;
        messageLength += messageHeader[3] << 8;
        messageLength += messageHeader[4];

        byte[] byteMessage = new byte[messageLength];
        inputStream.read(byteMessage, 0, messageLength);

        switch (messageHeader[0]) {
            case 2:
                game = new Game(createMap(byteMessage), ourPlayer);
                System.out.println(game.toString());
                break;
            case 3:
                ourPlayer = byteMessage[0];
                break;
            case 4:
                byte[] move = {5, 0, 0, 0, 5, 0, 0, 0, 0, 0};
                //int[] executedMove = new int[] {3, 1, 0};
                int [] executedMove = game.executeOurMove();

                // insert the x coordinate into the byte array
                move[6] = (byte) (executedMove[0]);
                move[5] = (byte) ((executedMove[0]) >> 8);

                // insert the y coordinate into the byte array
                move[8] = (byte) (executedMove[1]);
                move[7] = (byte) ((executedMove[1]) >> 8);

                // insert the special field into the byte array
                move[9] = (byte) (executedMove[2]);

                sendMessage(move);
                break;
            case 6:
                int x = byteMessage[0] << 8;
                x += byteMessage[1];

                int y = byteMessage[2] << 8;
                y += byteMessage[3];

                int player = byteMessage[5];
                int additionalOperation = byteMessage[4];

                game.executeMove(x, y, player, additionalOperation);

                break;
            case 7:
                if (byteMessage[0] == ourPlayer) {
                    System.err.println("YOU WERE DISQUALIFIED (PLAYER " + ourPlayer + ")\n");
                    System.err.println(game.getBoard().toString());
                    System.exit(1);
                }
                break;
            case 8:
                System.out.println("TYPE 8 - BOMBENPHASE");
                System.exit(-1);
                break;
            case 9:
                running = false;
                System.exit(0);
                break;
            default:
                break;
        }
    }

    private List<String> createMap(byte[] elements) {
        int length = elements.length;
        char[] message = new char[length];

        for (int i = 0; i < length; i++) {
            message[i] = (char) elements[i];
        }

        String string = String.valueOf(message);
        String[] lines = string.split("\n");

        return new LinkedList<>(Arrays.asList(lines));
    }
}
