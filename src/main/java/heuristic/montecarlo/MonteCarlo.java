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
/*
------- MIT 1 -------
      0 1 2 3 4 5 6 7
    /----------------
  0 | 0 1 0 0 0 0 0 1
  1 | 2 1 2 0 0 0 1 0
  2 | 0 1 0 0 1 1 1 0
  3 | 0'1 1 1 1 1 1 0
  4 | 0 0 0 1 1 1 0 0
  5 | 0 0 1 1 1 0 0 0
  6 | 0 0 0 1 0 1 0 0
  7 | 0 0 0 0 0 0 1 0

------- MIT 5 -------
      0 1 2 3 4 5 6 7
    /----------------
  0 | 2 2 2 1 1 1 1 1
  1 | 2 2 2 1 1 1 1 1
  2 | 2 2 2 2 1 1 1 1
  3 | 2 2 1 2 1 1 1 1
  4 | 2 1 2 2 2 2 1 1
  5 | 1 2 1 2 1 2 1 1
  6 | 1 1 2 2 2 2 1 1
  7 | 1 1 1 1 1 1 1 1

------- MIT 10 ------
      0 1 2 3 4 5 6 7
    /----------------
  0 | 1 1 1 2 1 1 1 1
  1 | 2 2 2 2 2 2 1 1
  2 | 1 2 1 2 2 2 1 1
  3 | 1 2 1 2 1 1 1 1
  4 | 1 2 1 1 1 2 1 1
  5 | 1 2 1 2 2 2 1 1
  6 | 1 1 2 1 1 1 2 1
  7 | 1 2 2 2 2 2 2 2
 */

public class MonteCarlo {

    private Token timeToken;

    private final Player[] players;
    private final int ourPlayerNumber;
    private final int playerAmount;
    private int currentPlayer;

    public MonteCarlo(Player[] players, int ourPlayerNumber) {
        this.players = players;
        this.ourPlayerNumber = ourPlayerNumber;
        this.currentPlayer = ourPlayerNumber;
        this.playerAmount = players.length;
        this.timeToken = new Token();
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
                count++;
                Node node = treePolicy(rootNode);
                State state = defaultPolicy(node.getState());
                int[] evaluation = getEvaluation(state.getBoard());
                backup(node, evaluation);
            } catch (TimeExceededException e) {
                System.out.println("Visited Nodes: " + count);
                Node selectedNode = bestChild(rootNode, 0);
                return selectedNode.getState().getMove();
            }
        }
    }

    private Node treePolicy(Node node) throws TimeExceededException {
        while (!node.isTerminal()) {
            if (timeToken.timeExceeded()) {
                System.out.println("treePolicy Exception");
                throw new TimeExceededException();
            }

            if (!node.isFullyExpanded()) {
                return expand(node);
            } else {
                // TODO: make Cp dynamic
                if (!timeToken.timeExceeded()) {
                    node = bestChild(node, (1 / Math.sqrt(2)));
                } else {
                    throw new TimeExceededException();
                }

            }
        }
        return node;
    }

    private Node expand(Node node) {
        // get random move and remove from list
        Move move = node.getUntriedMove();

        // select current player, board and additionalInfo
        Board board = new Board(node.getState().getBoard());
        Player player = getNextPlayer(board);
        int additionalInfo = getAdditionalInfo(move, player, board);

        // execute selected move
        board.colorizeMove(move, player, additionalInfo);

        //create new State with the altered board
        State state = new State(/*node, */board, move, players, ourPlayerNumber);

        //create new Node and add the child node to the childNodeList of the Parent node
        Node newChildNode = new Node(state, node, playerAmount);
        node.addChild(newChildNode);

        return newChildNode;
    }

    private int getAdditionalInfo(Move move, Player player, Board board) {
        int info = 0;
        if (move.isBonus()) info = 21;
        if (move.isChoice()) {
            // TODO: info = getBestPlayer(player.getIntNumber(), board);
            info = player.getIntNumber();
        }
        return info;
    }

    private Player getNextPlayer(Board board) {
        currentPlayer = ((currentPlayer % playerAmount) + 1);
        return players[currentPlayer - 1];
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
        while (!state.isTerminal()) {
            if (timeToken.timeExceeded()) {
                System.out.println("timeTokenException");
                throw new TimeExceededException();
            }

            // select current player, board and additionalInfo
            Board board = state.getBoard();
            Player player = getNextPlayer(board);
            Move randomMove = state.getRandomMove(player);

            if (randomMove != null) {
                int additionalInfo = getAdditionalInfo(randomMove, player, board);

                board.colorizeMove(randomMove, player, additionalInfo);
                state = new State(/*null, */board, randomMove, players, ourPlayerNumber);
            } else {
                state = new State(/*null, */board, new Move(), players, ourPlayerNumber);
            }
        }

        return state;
    }

    private void backup(Node node, int[] evaluation) throws TimeExceededException {
        while (node.getParent() != null) {
            if (timeToken.timeExceeded()) {
                System.out.println("Backup Exeption");
                throw new TimeExceededException();
            }

            node.increaseN();
            node.increaseQ(evaluation);
            node = node.getParent();
        }
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
