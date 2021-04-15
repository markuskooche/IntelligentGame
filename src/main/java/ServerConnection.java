import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.*;

public class ServerConnection {

    private Game game;
    private byte player;
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

    // 01 | 00 00 00 03 | 06 07 08

    private void receiveMessage() throws IOException {
        InputStream inputStream = socket.getInputStream();
        System.out.println(inputStream.toString());
        byte[] messageHeader = new byte[5];

        inputStream.read(messageHeader, 0, 5);
        byte messageType = messageHeader[0];

        int messageLength = ByteBuffer.wrap(Arrays.copyOfRange(messageHeader, 1, 5)).getInt();

        byte[] byteMessage = new byte[messageLength];
        inputStream.read(byteMessage, 0, messageLength);

        switch (messageType) {
            case 2:
                game = new Game(createMap(byteMessage));
                System.out.println(game.toString());
                break;
            case 3:
                player = byteMessage[0];
                break;
            case 4:
                System.out.println("TYPE 4");
                byte[] send = {5, 0, 0, 0, 5, 0, 3, 0, 1, 0};
                sendMessage(send);

                //int[] myMove = game.executeOurMove(player);
                // [x, y, s]
                // x = x-Koordinate
                // y = y-Koordinate
                // s = 0 bei normalem Feld
                //       beim Choice-Feld die Spielernummer mit der getauscht wird
                //       beim Bonus-Felder eine 20 fü̈r Bombe
                //       beim Bonus-Felder eine 21 fü̈r Ü̈berschreibstein


                //TODO: hier muss der Zug (Type 5) erstellt werden
                break;
            case 6:
                int x = byteMessage[0] << 8;
                x += byteMessage[1];

                int y = byteMessage[2] << 8;
                y += byteMessage[3];

                // TODO: Spezialfelder werden noch nicht beachtet
                int spezialField = byteMessage[4];
                int executor = byteMessage[5];

                game.executeMove(x, y, byteMessage[5]);
                break;
            case 7:
                if (byteMessage[0] == player) {
                    System.err.println("YOU WERE DISQUALIFIED (PLAYER " + player + ")\n");
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
