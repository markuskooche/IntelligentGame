package server;

import controller.Game;
import loganalyze.additional.AnalyzeParser;
import map.Player;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.*;

public class ServerConnection {

    private static final byte group = 1;
    private AnalyzeParser analyzeParser;

    private String host = "127.0.0.1";
    private int port = 7777;

    private boolean alphaBeta = true;
    private boolean moveSorting = true;

    private boolean consoleOutput = true;
    private boolean reduceOutput = true;

    private Game game;
    private byte ourPlayer;
    private Socket socket;
    private boolean running = true;
    private boolean bomb = false;

    public void start() {
        this.analyzeParser = new AnalyzeParser(group, consoleOutput, reduceOutput);
        analyzeParser.printGameInformation(alphaBeta);

        try {
            socket = new Socket(InetAddress.getByName(host), port);
            byte[] message = new byte[] {1, 0, 0, 0, 1, group};

            sendMessage(message);

            while (running) {
                receiveMessage();
            }
        } catch (ConnectException ce) {
            System.err.println("No server is running on " + host + ":" + port + "!");
        } catch (IOException e) {
            System.err.println("Please add this Exception to ServerConnection IOException Block");
            e.printStackTrace();
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
        int messageLength = get32Integer(messageHeader, 1);

        byte[] byteMessage = new byte[messageLength];
        inputStream.read(byteMessage, 0, messageLength);

        switch (messageHeader[0]) {
            // receive the map
            case 2:
                analyzeParser.parseBoard(byteMessage);
                List<String> receivedMap = MapParser.createMap(byteMessage);
                game = new Game(receivedMap, analyzeParser);
                break;
            // receive your player number
            case 3:
                ourPlayer = byteMessage[0];
                game.setOurPlayerNumber(ourPlayer);
                analyzeParser.setPlayer(ourPlayer);
                break;
            // receive a move request and sending a move
            case 4:
                int allowedTime = get32Integer(byteMessage, 0);
                byte allowedDepth = byteMessage[4];

                if (analyzeParser.isPrintable()) {
                    Player printPlayer = game.getPlayer(ourPlayer);
                    analyzeParser.loggingBoard(game.getBoard(), printPlayer);
                }

                if (!bomb) {
                    byte[] move = {5, 0, 0, 0, 5, 0, 0, 0, 0, 0};
                    int[] executedMove;

                    if (allowedTime == 0) {
                        executedMove = game.executeOurMoveDepth(allowedDepth, alphaBeta, moveSorting);
                    } else {
                        executedMove = game.executeOurMoveTime(allowedTime, alphaBeta, moveSorting);
                    }

                    // insert the x coordinate into the byte array
                    move[6] = (byte) (executedMove[0]);
                    move[5] = (byte) ((executedMove[0]) >> 8);

                    // insert the y coordinate into the byte array
                    move[8] = (byte) (executedMove[1]);
                    move[7] = (byte) ((executedMove[1]) >> 8);

                    // insert the special field into the byte array
                    move[9] = (byte) (executedMove[2]);
                    sendMessage(move);
                    analyzeParser.sendMove(executedMove[0],executedMove[1], ourPlayer, executedMove[2]);
                } else {
                    char[][] field = game.getBoard().getField();
                    byte[] bombMove = {5, 0, 0, 0, 5, 0, 0, 0, 0, 0};

                    int[] bombPosition = game.executeOurBomb();
                    int xBomb = bombPosition[0];
                    int yBomb = bombPosition[1];

                    bombMove[6] = (byte) (xBomb);
                    bombMove[5] = (byte) ((xBomb) >> 8);

                    bombMove[8] = (byte) (yBomb);
                    bombMove[7] = (byte) ((yBomb) >> 8);

                    sendMessage(bombMove);
                    analyzeParser.sendMove(xBomb ,yBomb, ourPlayer,0);
                }
                break;
            // receive a move of a player (also from itself)
            case 6:
                int x = byteMessage[0] << 8;
                x += byteMessage[1];

                int y = byteMessage[2] << 8;
                y += byteMessage[3];

                int player = byteMessage[5];
                int additionalOperation = byteMessage[4];

                analyzeParser.printCurrentTime(player);

                if (player != ourPlayer) {
                    if (analyzeParser.isPrintable()) {
                        Player printPlayer = game.getPlayer(player);
                        analyzeParser.loggingBoard(game.getBoard(), printPlayer);
                    }

                    if (!bomb) {
                        game.executeMove(x, y, player, additionalOperation);
                        analyzeParser.parseMove(x, y, player, additionalOperation);
                    } else {
                        game.executeBomb(x, y);
                        analyzeParser.parseMove(x, y, player, 0);
                    }
                } else {
                    analyzeParser.parseMove(x, y, player, additionalOperation);
                }
                break;
            // receive a disqualified player
            case 7:
                Player disqualifiedPlayer = game.getPlayer(byteMessage[0]);
                disqualifiedPlayer.setDisqualified();
                game.decreasePlayerNumber();

                if (byteMessage[0] == ourPlayer) {
                    analyzeParser.disqualifiedSelf(ourPlayer, game.getBoard());
                } else {
                    analyzeParser.disqualifyPlayer(byteMessage[0]);
                }
                break;
            // receive that phase 2 has started
            case 8:
                bomb = true;
                analyzeParser.startBombPhase();
                break;
            // receive that the game has finished
            case 9:
                running = false;
                analyzeParser.endGame();
                if (analyzeParser.isPrintable()) {
                    char[][] field = game.getBoard().getField();
                    analyzeParser.loggingBoard(field);
                }
                System.exit(0);
                break;
            default:
                break;
        }
    }

    private int get32Integer(byte[] header, int offset) {
        int length = 0;

        for (int i = 0; i < 4; i++) {
            int shift = (3 - i) * 8;
            length += (header[i + offset] & 0x000000FF) << shift;
        }

        return length;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setAlphaBeta(boolean alphaBeta) {
        this.alphaBeta = alphaBeta;
    }

    public void setMoveSorting(boolean moveSorting) {
        this.moveSorting = moveSorting;
    }

    public void setConsoleOutput(boolean consoleOutput) {
        this.consoleOutput = consoleOutput;
    }

    public void setReduceOutput(boolean reduceOutput) {
        this.reduceOutput = reduceOutput;
    }
}
