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
                game = new Game(createMap(byteMessage), analyzeParser);
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
                        // TODO: [Markus] add moveSorting
                        executedMove = game.executeOurMoveDepth(allowedDepth, alphaBeta);
                    } else {
                        // TODO: [Markus] add moveSorting
                        // TODO: [Markus] executedMove = game.executeOurMoveTime(allowedTime, alphaBeta);
                        executedMove = game.executeOurMoveDepth(allowedDepth, alphaBeta); // delete me
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

    public static List<String> createMap(byte[] elements) {
        List<Byte> mapPieces = new ArrayList<>();
        int length = elements.length;

        int transitionPart = 0;
        int infoCounter = 0;
        int addedPieces = 0;

        int height = 0;
        int width = 0;
        int currentHeight = 0;
        int currentWidth = 0;

        for (int j = 0; j < length; j++) {
            byte currentPiece = elements[j];
            byte nextPiece = ((byte) '\n');

            if ((j + 1) < length) {
                nextPiece = elements[j + 1];
            }

            if (currentPiece == 32 || currentPiece == 10) {
                continue;
            }

            // reading the player amount
            if (infoCounter == 0) {
                if (isNumeric(currentPiece)) {
                    mapPieces.add(currentPiece);

                    if (isNotNumeric(nextPiece)) {
                        mapPieces.add(((byte) '\n'));
                        infoCounter++;
                    }
                }
            }
            // reading the overridestone amount
            else if (infoCounter == 1) {
                if (isNumeric(currentPiece)) {
                    mapPieces.add(currentPiece);

                    if (isNotNumeric(nextPiece)) {
                        mapPieces.add(((byte) '\n'));
                        infoCounter++;
                    }
                }
            }
            // reading the bomb amount and radius
            else if (infoCounter == 2) {
                if (addedPieces == 0) {
                    if (isNumeric(currentPiece)) {
                        mapPieces.add(currentPiece);

                        if (isNotNumeric(nextPiece)) {
                            mapPieces.add(((byte) ' '));
                            addedPieces++;
                        }
                    }
                } else if (addedPieces == 1) {
                    if (isNumeric(currentPiece)) {
                        mapPieces.add(currentPiece);

                        if (isNotNumeric(nextPiece)) {
                            mapPieces.add(((byte) '\n'));
                            infoCounter++;
                            addedPieces = 0;
                        }
                    }
                }
            }
            // reading the height and width
            else if (infoCounter == 3) {
                if (addedPieces == 0) {
                    if (isNumeric(currentPiece)) {
                        height = updateLength(height, currentPiece);
                        mapPieces.add(currentPiece);

                        if (isNotNumeric(nextPiece)) {
                            mapPieces.add(((byte) ' '));
                            addedPieces++;
                        }
                    }
                } else if (addedPieces == 1) {
                    if (isNumeric(currentPiece)) {
                        width = updateLength(width, currentPiece);
                        mapPieces.add(currentPiece);

                        if (isNotNumeric(nextPiece)) {
                            mapPieces.add(((byte) '\n'));
                            infoCounter++;
                            addedPieces++;
                        }
                    }
                }
            }
            // reading the field
            else if (infoCounter == 4) {
                if (currentWidth < width) {
                    if (isGamePiece(currentPiece)) {
                        mapPieces.add(currentPiece);
                        mapPieces.add(((byte) ' '));
                        currentWidth++;

                        if (currentWidth == width) {
                            mapPieces.add(((byte) '\n'));
                            currentHeight++;
                            currentWidth = 0;

                            if (currentHeight == height) {
                                addedPieces = 0;
                                infoCounter++;
                            }
                        }
                    }
                }
            }
            // reading the transitions
            else if (infoCounter == 5) {
                // reading x1, y1 and r1
                if (addedPieces >= 0 && addedPieces < 3) {
                    if (isNumeric(currentPiece)) {
                        mapPieces.add(currentPiece);

                        if (isNotNumeric(nextPiece)) {
                            mapPieces.add(((byte) ' '));
                            addedPieces++;
                        }
                    }
                }
                // creating the transition arrow
                else if (addedPieces == 3 && transitionPart == 0) {
                    if (isTransitionArrowFront(currentPiece, nextPiece)) {
                        mapPieces.add(((byte) '<'));
                        mapPieces.add(((byte) '-'));
                        transitionPart++;
                    }
                }
                // creating the transition arrow
                else if (addedPieces == 3 && transitionPart == 1) {
                    if (isTransitionArrowBack(currentPiece, nextPiece)) {
                        mapPieces.add(((byte) '>'));
                        mapPieces.add(((byte) ' '));
                        transitionPart++;
                    }
                }
                // reading x2
                else if (addedPieces == 3 && transitionPart == 2) {
                    if (isNumeric(currentPiece)) {
                        mapPieces.add(currentPiece);
                        if (isNotNumeric(nextPiece)) {
                            mapPieces.add(((byte) ' '));
                            transitionPart = 0;
                            addedPieces++;
                        }

                    }
                }
                // reading y2
                else if (addedPieces == 4) {
                    if (isNumeric(currentPiece)) {
                        mapPieces.add(currentPiece);

                        if (isNotNumeric(nextPiece)) {
                            mapPieces.add(((byte) ' '));
                            addedPieces++;
                        }
                    }
                }
                // reading r2
                else if (addedPieces == 5) {
                    if (isNumeric(currentPiece)) {
                        mapPieces.add(currentPiece);

                        if (isNotNumeric(nextPiece)) {
                            mapPieces.add(((byte) '\n'));
                            addedPieces = 0;
                        }
                    }
                }
            }
        }

        // create a new char array
        int preparedLength = mapPieces.size();
        char[] preparedMapData = new char[preparedLength];

        // copy all pieces into the new char array
        for (int j = 0; j < preparedLength; j++) {
            byte currentByte = mapPieces.get(j);
            preparedMapData[j] = ((char) currentByte);
        }

        // create a string of the char array and split it
        String preparedMapString = String.valueOf(preparedMapData);
        String[] preparedMap = preparedMapString.split("\n");

        // return the map as a list
        return new LinkedList<>(Arrays.asList(preparedMap));
    }

    private static boolean isTransitionArrowFront(byte a, byte b) {
        return ((a == ((byte) '<')) && (b == ((byte) '-')));
    }

    private static boolean isTransitionArrowBack(byte a, byte b) {
        return ((a == ((byte) '-')) && (b == ((byte) '>')));
    }

    private static boolean isNumeric(byte a) {
        return (a >= 48 && a <= 57);
    }

    private static boolean isNotNumeric(byte a) {
        return (a < 48 || a > 57);
    }

    private static boolean isGamePiece(byte a) {
        byte empty = ((byte) '-');
        byte b = ((byte) 'b');
        byte c = ((byte) 'c');
        byte i = ((byte) 'i');
        byte x = ((byte) 'x');

        return (isNumeric(a) || (a == b || a == c || a == i || a == x || a == empty));
    }

    private static int updateLength(int currentLength, byte currentPiece) {
        return (10 * currentLength) + (currentPiece - ((byte) '0'));
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
