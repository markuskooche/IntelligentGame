package server;

import controller.Game;
import loganalyze.AnalyzeParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.*;

public class ServerConnection {

    private boolean bomb = false;

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
        int messageLength = getMessageLength(messageHeader);

        byte[] byteMessage = new byte[messageLength];
        inputStream.read(byteMessage, 0, messageLength);

        switch (messageHeader[0]) {
            case 2:
                game = new Game(createMap(byteMessage));
                AnalyzeParser.parseBoard(byteMessage);
                break;
            case 3:
                ourPlayer = byteMessage[0];
                game.setOurPlayerNumber(ourPlayer);
                AnalyzeParser.setPlayer(ourPlayer);
                break;
            case 4:
                if (!bomb) {
                    byte[] move = {5, 0, 0, 0, 5, 0, 0, 0, 0, 0};
                    int[] executedMove = game.executeOurMove();

                    // insert the x coordinate into the byte array
                    move[6] = (byte) (executedMove[0]);
                    move[5] = (byte) ((executedMove[0]) >> 8);

                    // insert the y coordinate into the byte array
                    move[8] = (byte) (executedMove[1]);
                    move[7] = (byte) ((executedMove[1]) >> 8);

                    // insert the special field into the byte array
                    move[9] = (byte) (executedMove[2]);
                    //AnalyzeParser.parseMove(executedMove[0], executedMove[1], ourPlayer, executedMove[3]);
                    sendMessage(move);
                } else {
                    char[][] field = game.getBoard().getField();
                    byte[] bombMove = {5, 0, 0, 0, 5, 0, 0, 0, 0, 0};

                    int xBomb = 0;
                    int yBomb = 0;

                    for (int i = 0; i < field.length; i++) {
                        for (int j = 0; j < field[0].length; j++) {
                            if (field[i][j] != '-') {
                                xBomb = j;
                                yBomb = i;
                                break;
                            }
                        }
                    }

                    bombMove[6] = (byte) (xBomb);
                    bombMove[5] = (byte) ((xBomb) >> 8);

                    bombMove[8] = (byte) (yBomb);
                    bombMove[7] = (byte) ((yBomb) >> 8);

                    //AnalyzeParser.parseMove(xBomb, yBomb, ourPlayer, 0);
                    game.executeBomb(xBomb, yBomb);
                    sendMessage(bombMove);
                }
                break;
            case 6:
                int x = byteMessage[0] << 8;
                x += byteMessage[1];

                int y = byteMessage[2] << 8;
                y += byteMessage[3];

                int player = byteMessage[5];

                if (!bomb) {
                    int additionalOperation = byteMessage[4];

                    game.executeMove(x, y, player, additionalOperation);
                    AnalyzeParser.parseMove(x, y, player, additionalOperation);
                } else {
                    game.executeBomb(x, y);
                    AnalyzeParser.parseMove(x, y, player, 0);
                }

                break;
            case 7:
                if (byteMessage[0] == ourPlayer) {
                    System.err.println("YOU WERE DISQUALIFIED (PLAYER " + ourPlayer + ")\n");
                    System.err.println(game.getBoard().toString());
                    System.exit(1);
                }
                AnalyzeParser.disqualifyPlayer(byteMessage[0]);
                break;
            case 8:
                AnalyzeParser.startBombPhase();
                bomb = true;
                break;
            case 9:
                AnalyzeParser.endGame();
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

    private int getMessageLength(byte[] header) {
        int length = 0;

        for (int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            length += (header[i + 1] & 0x000000FF) << shift;
        }

        return length;
    }
}
