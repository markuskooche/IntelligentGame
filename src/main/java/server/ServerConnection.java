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

    public static List<String> createMap(byte[] elements) {
        int length = elements.length;
        List<Byte> mapPieces = new ArrayList<>();
        int currentInformation = 0;
        int addedPieces = 0;
        int infoCounter = 0;

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

            //
            if (currentPiece == ((byte) '\n')) {
                if (currentInformation == 1 && infoCounter == 0) {
                    mapPieces.add(currentPiece);
                    infoCounter++;
                } else if (currentInformation == 2 && infoCounter == 1) {
                    mapPieces.add(currentPiece);
                    infoCounter++;
                } else if (currentInformation == 3 && infoCounter == 2) {
                    mapPieces.add(currentPiece);
                    infoCounter++;
                    /*
                    if (addedPieces == 2) {
                        mapPieces.add(currentPiece);
                        addedPieces = 0;
                        infoCounter++;
                    }
                    */
                } else if (currentInformation == 4 && infoCounter == 3) {
                    if (addedPieces == 2) {
                        mapPieces.add(currentPiece);
                        addedPieces = 0;
                        infoCounter++;
                    }
                } else if (currentInformation == 4 && infoCounter == 4) {
                    if (currentWidth == width) {
                        mapPieces.add(currentPiece);
                        currentWidth = 0;
                        currentHeight++;
                    }

                    if (currentHeight == height) {
                        currentInformation++;
                        infoCounter++;
                    }
                } else if (currentInformation == 5 && infoCounter == 5) {
                    if (addedPieces == 6) {
                        mapPieces.add(currentPiece);
                        addedPieces = 0;
                    }
                }
            } else if (currentPiece >= 48 && currentPiece <= 57) {
                mapPieces.add(currentPiece);

                if (currentInformation == 0) {
                    currentInformation++;
                } else if (currentInformation == 1 && infoCounter == 1) {
                    if (elements[j + 1] < 48 || elements[j + 1] > 57) {
                        currentInformation++;
                    }
                } else if (currentInformation == 2 && infoCounter == 2) {
                    if (addedPieces == 0) {
                        if (elements[j + 1] < 48 || elements[j + 1] > 57) {
                            mapPieces.add(((byte) ' '));
                            addedPieces++;
                        }
                    } else if (addedPieces == 1) {
                        if (elements[j + 1] < 48 || elements[j + 1] > 57) {
                            currentInformation++;
                            addedPieces = 0;
                        }
                    }
                } else if (currentInformation == 3 && infoCounter == 3) {
                    if (addedPieces == 0) {
                        height = (10 * height) + (currentPiece - ((byte) '0'));

                        if (elements[j + 1] < 48 || elements[j + 1] > 57) {
                            mapPieces.add(((byte) ' '));
                            addedPieces++;
                        }
                    } else {
                        width = (10 * width) + (currentPiece - ((byte) '0'));

                        if (elements[j + 1] < 48 || elements[j + 1] > 57) {
                            currentInformation++;
                            addedPieces++;
                        }
                    }
                } else if (currentInformation == 4 && infoCounter == 4) {
                    if (currentHeight < height) {
                        if (currentWidth < width) {
                            mapPieces.add(((byte) ' '));
                            currentWidth++;
                        }
                    }
                } else if (currentInformation == 5 && infoCounter == 5) {
                    if (addedPieces < 6) {
                        if (elements[j + 1] < 48 || elements[j + 1] > 57) {
                            mapPieces.add(((byte) ' '));
                            addedPieces++;
                        }
                    }
                }
            } else if (currentPiece == b || currentPiece == c || currentPiece == i || currentPiece == x) {
                if (currentInformation == 4 && infoCounter == 4) {
                    mapPieces.add(currentPiece);
                    if (currentHeight < height) {
                        if (currentWidth < (width - 1)) {
                            mapPieces.add(((byte) ' '));
                            currentWidth++;
                        } else if (currentWidth < width) {
                            currentWidth++;
                        }
                    }
                }
            } else if (currentPiece == ((byte) '<')) {
                if (currentInformation == 5 && infoCounter == 5) {
                    mapPieces.add(currentPiece);
                }
            } else if (currentPiece == ((byte) '-')) {
                if (currentInformation == 4 && infoCounter == 4) {
                    mapPieces.add(currentPiece);
                    if (currentHeight < height) {
                        if (currentWidth < (width - 1)) {
                            mapPieces.add(((byte) ' '));
                            currentWidth++;
                        } else if (currentWidth < width) {
                            currentWidth++;
                        }
                    }
                } else if (currentInformation == 5 && infoCounter == 5) {
                    mapPieces.add(currentPiece);
                }
            } else if (currentPiece == ((byte) '>')) {
                mapPieces.add(currentPiece);
                mapPieces.add(((byte) ' '));
            }
        }

        int preparedLength = mapPieces.size();
        char[] preparedMapData = new char[preparedLength];

        for (int j = 0; j < preparedLength; j++) {
            byte currentByte = mapPieces.get(j);
            preparedMapData[j] = ((char) currentByte);
        }

        String preparedMapString = String.valueOf(preparedMapData);
        String[] preparedMap = preparedMapString.split("\n");

        return new LinkedList<>(Arrays.asList(preparedMap));
    }

    private boolean isNumeric(byte a) {
        return (a >= 48 && a <= 57);
    }

    private boolean isNotNumeric(int a) {
        return (a < 48 || a > 57);
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
