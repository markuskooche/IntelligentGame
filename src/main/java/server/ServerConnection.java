package server;

import controller.Game;
import loganalyze.additional.AnalyzeParser;
import map.Player;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.*;

/**
 * The ServerConnection class instantiate a new connection to a socket.
 * The standard connection is '127.0.0.1:7777'. If no connection can be established, the client is terminated.
 * You can change the IP address, port and other dependencies using setters.
 * If a connection is to be started start() must be called.
 *
 * @author Benedikt Halbritter
 * @author Iwan Eckert
 * @author Markus Koch
 */
public class ServerConnection {

    private static final byte GROUP = 1;
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

    /**
     * This method tries to establish a connection and, if necessary,
     * receives/sends messages in an endless loop until the game is finished.
     */
    public void start() {
        // creates an AnalyzeParser to output important information to the console
        analyzeParser = new AnalyzeParser(GROUP, consoleOutput, reduceOutput);
        analyzeParser.printGameInformation(alphaBeta);

        // try to establish a socket connection
        try {
            socket = new Socket(InetAddress.getByName(host), port);

            // sending the group number to the socket
            byte[] message = new byte[] {1, 0, 0, 0, 1, GROUP};
            sendMessage(message);

            // keep the connection until the game is finished
            while (running) {
                receiveMessage();
            }
        }
        // throw an ConnectionException if no server is running on this host and port
        catch (ConnectException ce) {
            System.err.println("No server is running on " + host + ":" + port + "!");
            System.exit(0);
        }
        // throw an SocketException if a message could not be send (disqualification or server termination)
        catch (SocketException se) {
            System.out.println("Client could not sent a message.");
            System.out.println("Server has been terminated.");
            se.printStackTrace();
            System.exit(0);
        }
        // throw an IOException if a message could not be received (disqualification or server termination)
        catch (IOException e) {
            System.out.println("Client could not receive a message.");
            System.out.println("Server has been terminated.");
            e.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * Use this method to send a message to the output stream
     *
     * @param message contains the message to be sent in a byte-array
     * @throws IOException throws an IOException if server is closed or client is disqualified
     */
    private void sendMessage(byte[] message) throws IOException {
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(message);
    }

    /**
     * This method receives messages until the game is finished.
     *
     * @throws IOException throws an IOException if this socket is closed or the socket is not connected
     */
    private void receiveMessage() throws IOException {
        InputStream inputStream = socket.getInputStream();
        byte[] messageHeader = new byte[5];

        // storage the length of read bytes
        int readBytes = inputStream.read(messageHeader, 0, 5);

        // if length == -1, not bytes could be read
        // this happens with a closed socket connection
        if (readBytes == -1) {
            throw new IOException();
        }

        // create an integer from the byte array which contains the length
        int messageLength = get32Integer(messageHeader, 1);

        // read the remaining input stream (this is the actual message)
        byte[] byteMessage = new byte[messageLength];
        int endOfStream = inputStream.read(byteMessage, 0, messageLength);

        // execute the corresponding received message
        switch (messageHeader[0]) {
            // receive the map
            case 2:
                // print the received map to the console
                analyzeParser.parseBoard(byteMessage);

                // create a new game
                List<String> receivedMap = MapParser.createMap(byteMessage);
                game = new Game(receivedMap, analyzeParser);
                break;
            // receive your player number
            case 3:
                // set our player number
                ourPlayer = byteMessage[0];
                game.setOurPlayerNumber(ourPlayer);

                // print our player number to the console
                analyzeParser.setPlayer(ourPlayer);
                break;
            // receive a move request and sending a move
            case 4:
                // get the time- or depth-limit
                int allowedTime = get32Integer(byteMessage, 0);
                byte allowedDepth = byteMessage[4];

                // when console output is on and reduce is off,
                // the map is output to the console
                if (analyzeParser.isPrintable()) {
                    Player printPlayer = game.getPlayer(ourPlayer);
                    analyzeParser.loggingBoard(game.getBoard(), printPlayer);
                }

                // if you should send a normal move (phase 1)
                if (!bomb) {
                    // selects the best move
                    int[] executedMove;

                    // when the game is played with depth limit
                    if (allowedTime == 0) {
                        executedMove = game.executeOurMoveDepth(allowedDepth, alphaBeta, moveSorting);
                    }
                    // when the game is played with time limit
                    else {
                        executedMove = game.executeOurMoveTime(allowedTime, alphaBeta, moveSorting);
                    }

                    // sends the best move to the server
                    byte[] move = prepareMove(executedMove);
                    sendMessage(move);

                    // print the selected bomb move to the console
                    analyzeParser.sendMove(executedMove[0],executedMove[1], ourPlayer, executedMove[2]);
                }
                // if you should send a bomb move (phase 2)
                else {
                    // selects the best bomb move and sends it to the server
                    int[] executedBombMove = game.executeOurBomb();
                    byte[] move = prepareMove(executedBombMove);
                    sendMessage(move);

                    // print the selected bomb move to the console
                    analyzeParser.sendMove(executedBombMove[0] ,executedBombMove[1], ourPlayer,0);
                }
                break;
            // receive a move of a player (also from itself)
            case 6:
                // composes the x coordinate
                int x = byteMessage[0] << 8;
                x += byteMessage[1];

                // composes the y coordinate
                int y = byteMessage[2] << 8;
                y += byteMessage[3];

                // stores the player and additional operation
                int player = byteMessage[5];
                int additionalOperation = byteMessage[4];

                // prints the current time to the console
                analyzeParser.printCurrentTime(player);

                // if it is a move from an opponent
                if (player != ourPlayer) {
                    // when console output is on and reduce is off,
                    // the map is output to the console
                    if (analyzeParser.isPrintable()) {
                        Player printPlayer = game.getPlayer(player);
                        analyzeParser.loggingBoard(game.getBoard(), printPlayer);
                    }

                    // // execute the selected move from the opponent (phase 1)
                    if (!bomb) {
                        game.executeMove(x, y, player, additionalOperation);
                    }
                    // execute the selected bomb move from the opponent (phase 2)
                    else {
                        game.executeBomb(x, y);
                    }
                }

                // print the selected move to the console
                analyzeParser.parseMove(x, y, player, additionalOperation);
                break;
            // receive a disqualified player
            case 7:
                // remove the disqualified player
                Player disqualifiedPlayer = game.getPlayer(byteMessage[0]);
                disqualifiedPlayer.setDisqualified();
                game.decreasePlayerNumber();

                // terminate in case our client was disqualified
                if (byteMessage[0] == ourPlayer) {
                    analyzeParser.disqualifiedSelf(ourPlayer, game.getBoard());
                    System.exit(1);
                }
                // else print the information to the console
                else {
                    analyzeParser.disqualifyPlayer(byteMessage[0]);
                }
                break;
            // receive that phase 2 has started
            case 8:
                // set the bomb mode and print this information
                analyzeParser.startBombPhase();
                bomb = true;
                break;
            // receive that the game has finished
            case 9:
                // stop the game
                running = false;
                analyzeParser.endGame();

                // when console output is on and reduce is off,
                // the map is output to the console
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

    /**
     * Creates a move to be sent from the coordinates and possibly the special field.
     *
     * @param selectedMove selected move with size 2 (= [x, y]) or 3 (= [x, y, a])
     * @return a byte array which could be send
     */
    private byte[] prepareMove(int[] selectedMove) {
        // default message which is updated and sent
        byte[] move = {5, 0, 0, 0, 5, 0, 0, 0, 0, 0};

        // insert the x coordinate into the byte array
        move[6] = (byte) (selectedMove[0]);
        move[5] = (byte) ((selectedMove[0]) >> 8);

        // insert the y coordinate into the byte array
        move[8] = (byte) (selectedMove[1]);
        move[7] = (byte) ((selectedMove[1]) >> 8);

        // insert the special field into the byte array
        if (selectedMove.length == 3) {
            move[9] = (byte) (selectedMove[2]);
        }

        return move;
    }

    /**
     * A byte array with at least 4 unsigned bytes is converted into an integer.
     *
     * @param header byte array which should be converted
     * @param offset shift if it should not start at index 0
     * @return converted integer from 4 byte
     */
    private int get32Integer(byte[] header, int offset) {
        if (header.length < 4) {
            return -1;
        }

        int value = 0;

        for (int i = 0; i < 4; i++) {
            int shift = (3 - i) * 8;
            value += (header[i + offset] & 0x000000FF) << shift;
        }

        return value;
    }

    /**
     * Set a different ip address. (Standard: 127.0.0.1)
     *
     * @param host the ip address (e.g. 192.168.178.92)
     */
    public void setHost(String host) {
        // check if it is a invalid IP (e.g. 192.10.14.999) and not 'localhost'
        if (!host.matches("^((25[0-5]|(2[0-4]|1[0-9]|[1-9]|)[0-9])(\\.(?!$)|$)){4}$") && !host.equals("localhost")) {
            System.err.println("ERROR: The entered IP is not valid!");
            System.exit(1);
        }

        this.host = host;
    }

    /**
     * Set a different ip address. (Standard: 7777)
     *
     * @param port enter a valid port (e.g. 4293)
     */
    public void setPort(int port) {
        // a port could only be between 1 and 65534
        if (port < 1 || port > 65535) {
            System.err.println("ERROR: The entered PORT is not valid!");
            System.exit(1);
        }

        this.port = port;
    }

    /**
     * Turn alpha-beta on or off.
     *
     * @param alphaBeta ON = 1 || OFF = 0
     */
    public void setAlphaBeta(String alphaBeta) {
        // only on (= 1) and off (= 0) is allowed
        if (alphaBeta.equals("0")) {
            this.alphaBeta = false;
        } else if (alphaBeta.equals("1")) {
            this.alphaBeta = true;
        } else {
            System.err.println("ERROR: Please set alpha beta pruning to 0 or 1!");
            System.exit(1);
        }
    }

    /**
     * Turn move sorting on or off.
     *
     * @param moveSorting ON = 1 || OFF = 0
     */
    public void setMoveSorting(String moveSorting) {
        // only on (= 1) and off (= 0) is allowed
        if (moveSorting.equals("0")) {
            this.moveSorting = false;
        } else if (moveSorting.equals("1")) {
            this.moveSorting = true;
        } else {
            System.err.println("ERROR: Please set move sorting to 0 or 1!");
            System.exit(1);
        }
    }

    /**
     * Turn console output on or off.
     *
     * @param consoleOutput ON = 1 || OFF = 0
     */
    public void setConsoleOutput(String consoleOutput) {
        // only on (= 1) and off (= 0) is allowed
        if (consoleOutput.equals("0")) {
            this.consoleOutput = false;
        } else if (consoleOutput.equals("1")) {
            this.consoleOutput = true;
        } else {
            System.err.println("ERROR: Please set console output to 0 or 1!");
            System.exit(1);
        }
    }

    /**
     * Turn console reduce on or off.
     *
     * @param reduceOutput ON = 1 || OFF = 0
     */
    public void setReduceOutput(String reduceOutput) {
        // only on (= 1) and off (= 0) is allowed
        if (reduceOutput.equals("0")) {
            this.reduceOutput = false;
        } else if (reduceOutput.equals("1")) {
            this.reduceOutput = true;
        } else {
            System.err.println("ERROR: Please set console reduce to 0 or 1!");
            System.exit(1);
        }
    }
}
