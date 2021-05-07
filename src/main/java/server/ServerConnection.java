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
            System.err.println("Please add this Exception to ServerConnection IOException Block");
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
        int messageLength = get32Integer(messageHeader, 1);

        byte[] byteMessage = new byte[messageLength];
        inputStream.read(byteMessage, 0, messageLength);

        switch (messageHeader[0]) {
            // receive the map
            case 2:
                game = new Game(createMap(byteMessage));
                AnalyzeParser.parseBoard(byteMessage);
                break;
            // receive your player number
            case 3:
                ourPlayer = byteMessage[0];
                game.setOurPlayerNumber(ourPlayer);
                AnalyzeParser.setPlayer(ourPlayer);
                break;
            // receive a move request and sending a move
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
                    AnalyzeParser.sendMove(executedMove[0],executedMove[1], ourPlayer, executedMove[2]);
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
                    AnalyzeParser.sendMove(xBomb ,yBomb, ourPlayer,0);
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
                break;
            // receive a disqualified player
            case 7:
                Player disqualifiedPlayer = game.getPlayer(byteMessage[0]);
                disqualifiedPlayer.setDisqualified();

                if (byteMessage[0] == ourPlayer) {
                    System.out.println("WE WERE DISQUALIFIED (PLAYER " + ourPlayer + ")\n");
                    System.out.println(game.getBoard().toString());
                }
                AnalyzeParser.disqualifyPlayer(byteMessage[0]);
                break;
            // receive that phase 2 has started
            case 8:
                bomb = true;
                AnalyzeParser.startBombPhase();
                break;
            // receive that the game has finished
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

    public static List<String> createMap(byte[] elements) {
        List<Byte> mapPieces = new ArrayList<>();
        int length = elements.length;

        int infoCounter = 0;
        int currInfo = 0;

        int addedPieces = 0;

        int height = 0;
        int width = 0;
        int currentHeight = 0;
        int currentWidth = 0;

        byte b = ((byte) 'b');
        byte c = ((byte) 'c');
        byte i = ((byte) 'i');
        byte x = ((byte) 'x');

        for (int j = 0; j < length; j++) {
            byte currentPiece = elements[j];

            // check if it is a newline and if so if it needs to be added
            if (currentPiece == ((byte) '\n')) {
                // (when the number of players was read) or (when the number of overridestones has been read)
                if ((currInfo == 1 && infoCounter == 0) || (currInfo == 2 && infoCounter == 1)) {
                    mapPieces.add(currentPiece);
                    infoCounter++;
                }
                // (when the number of bombs and the radius has been read) or (when height and width was read)
                else if (((currInfo == 3 && infoCounter == 2) || (currInfo == 4 && infoCounter == 3)) && addedPieces == 2) {
                    mapPieces.add(currentPiece);
                    addedPieces = 0;
                    infoCounter++;
                }
                // if one line of the actual field was read in
                else if (currInfo == 4 && infoCounter == 4) {
                    // checks if it has really reached the end of a line
                    if (currentWidth == width) {
                        mapPieces.add(currentPiece);
                        currentWidth = 0;
                        currentHeight++;

                        // checks if it has reached the end of the map
                        if (currentHeight == height) {
                            currInfo++;
                            infoCounter++;
                        }
                    }
                }
                // when both positions and directions have been read
                else if (currInfo == 5 && infoCounter == 5 && addedPieces == 6) {
                    mapPieces.add(currentPiece);
                    addedPieces = 0;
                }
            }
            // if the read symbol is a number
            else if (isNumeric(currentPiece)) {
                mapPieces.add(currentPiece);

                // when you read the number of players
                if (currInfo == 0) {
                    currInfo++;
                }
                // if just the number of overwrite stones is read
                else if (currInfo == 1 && infoCounter == 1) {
                    // if the next character is not a number
                    if (isNotNumeric(elements[j + 1])) {
                        currInfo++;
                    }
                }
                // when just reading the information for bombs
                else if (currInfo == 2 && infoCounter == 2) {
                    // if the next character is not a number
                    if (isNotNumeric(elements[j + 1])) {
                        // when the bomb count has finished reading in
                        if (addedPieces == 0) {
                            mapPieces.add(((byte) ' '));
                            addedPieces++;
                        }
                        // when the bomb radius has finished being read in
                        else if (addedPieces == 1) {
                            currInfo++;
                            addedPieces++;
                        }
                    }
                }
                // if the height or width is currently being read in
                else if (currInfo == 3 && infoCounter == 3) {
                    // it is about the height when addedPieces == 0
                    if (addedPieces == 0) {
                        height = updateLength(height, currentPiece);

                        // if the next character is not a number
                        if (isNotNumeric(elements[j + 1])) {
                            mapPieces.add(((byte) ' '));
                            addedPieces++;
                        }
                    }
                    // it is about the height when addedPieces == 1
                    else if (addedPieces == 1) {
                        width = updateLength(width, currentPiece);

                        // if the next character is not a number
                        if (isNotNumeric(elements[j + 1])) {
                            currInfo++;
                            addedPieces++;
                        }
                    }
                }
                // when the actual playing field is read in
                else if (currInfo == 4 && infoCounter == 4) {
                    // if it is not out of range
                    if ((currentHeight < height) && (currentWidth < width)) {
                        mapPieces.add(((byte) ' '));
                        currentWidth++;
                    }
                }
                // when a transition is being read in
                else if (currInfo == 5 && infoCounter == 5 && addedPieces < 6) {
                    // if the next character is not a number
                    if (isNotNumeric(j + 1)) {
                        mapPieces.add(((byte) ' '));
                        addedPieces++;
                    }
                }
            }
            // when a special field is read in
            else if (currentPiece == b || currentPiece == c || currentPiece == i || currentPiece == x) {
                // when the actual playing field is read in
                if (currInfo == 4 && infoCounter == 4) {
                    mapPieces.add(currentPiece);

                    // if it is not out of range
                    if (currentWidth < width) {
                        mapPieces.add(((byte) ' '));
                        currentWidth++;
                    }
                }
            }
            // if it is the first symbol of a transition
            else if (currentPiece == ((byte) '<')) {
                if (currInfo == 5 && infoCounter == 5) {
                    mapPieces.add(currentPiece);
                }
            }
            // if it is the second symbol of a transition or a empty piece
            else if (currentPiece == ((byte) '-')) {
                // when the actual playing field is read in
                if (currInfo == 4 && infoCounter == 4) {
                    mapPieces.add(currentPiece);

                    // if it is not out of range
                    if (currentWidth < width) {
                        mapPieces.add(((byte) ' '));
                        currentWidth++;
                    }
                }
                // if it is a part of the transition separation
                else if (currInfo == 5 && infoCounter == 5) {
                    mapPieces.add(currentPiece);
                }
            }
            // if it is the third symbol of a transition
            else if (currentPiece == ((byte) '>')) {
                mapPieces.add(currentPiece);
                mapPieces.add(((byte) ' '));
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

    private static boolean isNumeric(byte a) {
        return (a >= 48 && a <= 57);
    }

    private static boolean isNotNumeric(int a) {
        return (a < 48 || a > 57);
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
}
