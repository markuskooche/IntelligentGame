package heuristic.montecarlo;

import heuristic.ThreadTimer;
import heuristic.TimeExceededException;
import heuristic.TimeOutTask;
import heuristic.Token;
import map.Board;
import map.Move;
import map.Player;

import java.util.List;
import java.util.Timer;

public class MonteCarlo {

    private Token timeToken;

    private final Player[] players;
    private final int ourPlayerNumber;
    private final int playerAmount;
    private int currentPlayer;

    private Player[] tmpPlayers;
    private double cp = 5.0;

    public MonteCarlo(Player[] players, int ourPlayerNumber) {
        this.players = players;
        this.ourPlayerNumber = ourPlayerNumber;
        this.currentPlayer = ourPlayerNumber;
        this.playerAmount = players.length;
        this.timeToken = new Token();
        this.tmpPlayers = new Player[playerAmount];
    }

    private void startTimer(long maxTimeForMove) {
        timeToken.start();
        Thread t = new Thread(new ThreadTimer(timeToken));
        Timer timer = new Timer();
        timer.schedule(new TimeOutTask(t, timer), (maxTimeForMove - 200));
        t.start();
    }

    public Move getMove(Board board, long time) {
        startTimer(time);

        Board startBoard = new Board(board);
        State rootState = new State(/*null, */startBoard, null, players,  ourPlayerNumber);
        Node rootNode = new Node(rootState, null, playerAmount);

        int count = 0;

        while (true) {
            /*
            if (timeToken.timeExceeded()) {
                System.out.println("getMove OBEN");
                System.out.println("Count : " + count);
                Node selectedNode = bestChild(rootNode, 0);
                return selectedNode.getState().getMove();
            }*/

            try {
                copyPlayers();
                count++;
                Node node = treePolicy(rootNode);
                /*
                if (count <= 4) {
                    System.out.println(node.getState().getBoard());
                }*/
                State state = defaultPolicy(node.getState());
                int[] evaluation = getEvaluation(state.getBoard());
                backup(node, evaluation);
            } catch (TimeExceededException e) {
                System.out.println("Turns calculated: " + count);
                Node selectedNode = bestChild(rootNode, 0);
                return selectedNode.getState().getMove();
            }
        }
    }

    private void copyPlayers() {
        for (int i = 0; i < players.length; i++) {
            tmpPlayers[i] = new Player(players[i]);
        }
    }

    private Node treePolicy(Node node) throws TimeExceededException {
        int count = 0;
        while (!node.isTerminal()) {
            if (timeToken.timeExceeded()) {
                System.out.println("Tree Policy Counter (Exception): " + count);
                throw new TimeExceededException();
            }
            count++;
            if (!node.isFullyExpanded()) {
                return expand(node);
            } else {
                // TODO: make Cp dynamic
                if (!timeToken.timeExceeded()) {
                    node = bestChild(node, cp);
                } else {
                    throw new TimeExceededException();
                }

            }
        }
        //System.out.println("Tree Policy Counter: " + count);
        return node;
    }

    private Node expand(Node node) {
        // get random move and remove from list
        Move move = node.getUntriedMove();

        // select current player, board and additionalInfo
        Board board = new Board(node.getState().getBoard());
        Player player = getNextPlayer(board);
        int additionalInfo = getAdditionalInfo(move, player, board);

        if (move != null) {
            /* TODO: ??????
            if (move.isOverride()) {
                player.increaseOverrideStone();
            }*/

            // execute selected move
            board.colorizeMove(move, player, additionalInfo);
        }

        //create new State with the altered board
        State state = new State(/*node, */board, move, tmpPlayers, ourPlayerNumber);

        /*
        if (player.getIntNumber() == ourPlayerNumber && state.noNormalMoves(player)) {
            cp = (1 / Math.sqrt(2));
        }
        */

        //create new Node and add the child node to the childNodeList of the Parent node
        Node newChildNode = new Node(state, node, playerAmount);
        node.addChild(newChildNode);

        return newChildNode;
    }

    private int getAdditionalInfo(Move move, Player player, Board board) {
        int info = 0;

        if (move != null) {
            if (move.isBonus()) info = 21;
            if (move.isChoice()) {
                // TODO: info = getBestPlayer(player.getIntNumber(), board);
                info = player.getIntNumber();
            }
        }
        return info;
    }

    private Player getNextPlayer(Board board) {
        currentPlayer = ((currentPlayer % playerAmount) + 1);
        return tmpPlayers[currentPlayer - 1];
    }

    private Node bestChild(Node node, double cp) {
        List<Node> children = node.getChildren();
        if (children.size() != 0) {
            Node bestChild = children.get(0);
            double maxValue = Double.MIN_VALUE;

            for (Node child : children) {

                double value = ((double) child.getQ(ourPlayerNumber) / child.getN());
                if (cp != 0) {
                    value += cp * Math.sqrt((2 * Math.log10(node.getN())) / child.getN());
                }

                if (maxValue < value) {
                    maxValue = value;
                    bestChild = child;
                }
            }

            return bestChild;
        }

        return node.getParent();
    }

    private State defaultPolicy(State state) throws TimeExceededException {
        int counter = 0;

        while (!state.isTerminal()) {
            if (timeToken.timeExceeded()) {
                System.out.println("Counter DefaultPolicy (exception): " + counter);
                throw new TimeExceededException();
            }
            counter++;

            // select current player, board and additionalInfo
            Board board = state.getBoard();
            Player player = getNextPlayer(board);
            Move randomMove = state.getRandomMove(player);
            //System.out.println(randomMove);

            if (randomMove != null) {
                int additionalInfo = getAdditionalInfo(randomMove, player, board);

                /*
                if (randomMove.isOverride()) {
                    player.increaseOverrideStone();
                }
                */

                board.colorizeMove(randomMove, player, additionalInfo);
                state = new State(/*null, */board, randomMove, tmpPlayers, ourPlayerNumber);
            } else {
                state = new State(/*null, */board, new Move(), tmpPlayers, ourPlayerNumber);
            }
        }

        /*
        if (state.isTerminal() && counter == 0 && globalD > 1) {
            System.out.println(state.getBoard());
            System.exit(1);
        }
        */

        //System.out.println("Counter DefaultPolicy: " + counter);
        return state;
    }

    private void backup(Node node, int[] evaluation) throws TimeExceededException {
        int counter = 0;

        while (node.getParent() != null) {
            if (timeToken.timeExceeded()) {
                System.out.println("Counter Backup (Exception): " + counter);
                throw new TimeExceededException();
            }

            node.increaseN();
            node.increaseQ(evaluation);
            node = node.getParent();
            counter++;
        }

        //System.out.println("Counter Backup: " + counter);
    }

    public int[] getEvaluation(Board board) {
        int[] playerCount = new int[playerAmount];
        char[][] field = board.getField();

        int height = field.length;
        int width = field[0].length;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if ("12345678".indexOf(field[y][x]) != -1) {
                    int playerNumber = field[y][x] - '0';
                    playerCount[playerNumber - 1] += 1;
                }
            }
        }

        return playerCount;
    }
}
