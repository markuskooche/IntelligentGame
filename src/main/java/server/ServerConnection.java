package server;

import controller.Game;
import loganalyze.additional.AnalyzeParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.*;

public class ServerConnection {

    private boolean bomb = false;
    private final boolean alphaBeta;

    private Game game;
    private byte ourPlayer;
    private Socket socket;
    private boolean running = true;

    public ServerConnection(String host, int port, int groupNumber, boolean alphaBeta) {
        this.alphaBeta = alphaBeta;
        AnalyzeParser.printGameInformation(alphaBeta);

        try {
            socket = new Socket(InetAddress.getByName(host), port);
            byte group = (byte) groupNumber;
            byte[] message = new byte[] {1, 0, 0, 0, 1, group};

            sendMessage(message);
            play();
        } catch (ConnectException ce) {
            System.err.println("No server is running on " + host + ":" + port + "!");
        } catch (IOException e) {
            //System.err.println("Please add this Exception to ServerConnection IOException Block");

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
        int received = inputStream.read(messageHeader, 0, 5);
        System.out.println("R1: " + received);

        if (received > -1) {

            // create an integer from the byte array
            int messageLength = get32Integer(messageHeader, 1);

            byte[] byteMessage = new byte[messageLength];
            int receivedLength = inputStream.read(byteMessage, 0, messageLength);
            System.out.println("R2: " + receivedLength);

            switch (messageHeader[0]) {
                case 2:
                    game = new Game(createMap(byteMessage));
                    break;
                case 3:
                    ourPlayer = byteMessage[0];
                    game.setOurPlayerNumber(ourPlayer);
                    AnalyzeParser.setPlayer(ourPlayer);
                    break;
                case 4:
                    int allowedTime = get32Integer(byteMessage, 0);
                    byte allowedDepth = byteMessage[4];

                    System.out.println("[TIME: " + allowedTime + "ms  ||  DEPTH: " + allowedDepth + "]");
                    game.getBoard().loggingBoard(game.getPlayer(ourPlayer));

                    if (!bomb) {
                        byte[] move = {5, 0, 0, 0, 5, 0, 0, 0, 0, 0};
                        int[] executedMove = game.executeOurMove(allowedDepth, alphaBeta);

                        // insert the x coordinate into the byte array
                        move[6] = (byte) (executedMove[0]);
                        move[5] = (byte) ((executedMove[0]) >> 8);

                        // insert the y coordinate into the byte array
                        move[8] = (byte) (executedMove[1]);
                        move[7] = (byte) ((executedMove[1]) >> 8);

                        // insert the special field into the byte array
                        move[9] = (byte) (executedMove[2]);
                        sendMessage(move);
                        AnalyzeParser.sendMove(executedMove[0], executedMove[1], ourPlayer, executedMove[2]);
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

                        sendMessage(bombMove);
                        game.executeBomb(xBomb, yBomb);
                        AnalyzeParser.sendMove(xBomb, yBomb, ourPlayer, 0);
                    }
                    break;
                case 6:
                    int x = byteMessage[0] << 8;
                    x += byteMessage[1];

                    int y = byteMessage[2] << 8;
                    y += byteMessage[3];

                    int player = byteMessage[5];
                    int additionalOperation = byteMessage[4];

                    AnalyzeParser.printCurrentTime(player);

                    if (player != ourPlayer) {
                        game.getBoard().loggingBoard(game.getPlayer(player));

                        if (!bomb) {
                            game.executeMove(x, y, player, additionalOperation);
                            AnalyzeParser.parseMove(x, y, player, additionalOperation);
                        } else {
                            game.executeBomb(x, y);
                            AnalyzeParser.parseMove(x, y, player, 0);
                        }
                    } else {
                        AnalyzeParser.parseMove(x, y, player, additionalOperation);
                    }

                    //int nextPlayer = (player % game.getPlayers().length) + 1;
                    //game.getBoard().loggingBoard(game.getPlayer(nextPlayer));
                    break;
                case 7:
                    if (byteMessage[0] == ourPlayer) {
                        System.out.println("WE WERE DISQUALIFIED (PLAYER " + ourPlayer + ")\n");
                        System.out.println(game.getBoard().toString());
                    }
                    AnalyzeParser.disqualifyPlayer(byteMessage[0]);
                    break;
                case 8:
                    bomb = true;
                    AnalyzeParser.startBombPhase();
                    break;
                case 9:
                    running = false;
                    AnalyzeParser.endGame();
                    game.getBoard().loggingBoard();
                    System.exit(0);
                    break;
                default:
                    break;
            }
        }
    }

    public static List<String> createMap(byte[] elements) {
        int length = elements.length;
        char[] message = new char[length];

        // TODO: Das kann man alles weglassen da nun diese Methode zum parsen verwendet wird. (BITTE UMBAUEN)
        List<Byte> printList = new ArrayList<>();

        for (int i = 0; i < length; i++) {
            message[i] = (char) elements[i];
            if (elements[i] != ((byte) 13)) {
                printList.add(elements[i]);
            }
        }

        AnalyzeParser.parseBoard(printList);

        String string = String.valueOf(message);

        String[] lines;
        // TODO: Könnte eventuell nicht funktionieren wenn Leerzeichen folgen. (BITTE TESTEN)
        if (elements[1] == (byte) 13) {
            lines = string.split("\r\n");
        } else {
            lines = string.split("\n");
        }

        return new LinkedList<>(Arrays.asList(lines));
    }

    private int get32Integer(byte[] header, int offset) {
        int length = 0;

        for (int i = 0; i < 4; i++) {
            int shift = (3 - i) * 8;
            length += (header[i + offset] & 0x000000FF) << shift;
        }

        return length;
    }
}
