package heuristic.montecarlo;

import map.Board;
import map.Move;
import map.Player;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class State {

    private final Board board;
    //private Node parent;
    private final Player ourPlayer;
    private final Player[] players;
    private final Move move;
    private final Random random;

    public State(/*Node parent, */Board board, Move move, Player[] players, int ourPlayerNumber) {
        //this.parent = parent;
        this.board = board;
        this.players = players;
        this.move = move;
        this.ourPlayer = players[ourPlayerNumber - 1];
        random = new Random();
    }

    public boolean isTerminal() {
        List<Move> allMoves = new LinkedList<>();

        for (Player player : players) {
            List<Move> playerMoves = board.getLegalMoves(player, true);
            allMoves.addAll(playerMoves);
        }

        return allMoves.isEmpty();
    }

    public List<Move> getPlayerMoves() {
        return board.getLegalMoves(ourPlayer, true);
    }

    public Move getRandomMove(Player player) {
        List<Move> playerMoves = board.getLegalMoves(player, true);

        if (!playerMoves.isEmpty()) {
            int radomNumber = random.nextInt(playerMoves.size());
            return playerMoves.get(radomNumber);
        }

        return null;
    }

    public Board getBoard() {
        return board;
    }

    public Move getMove() {
        return move;
    }
}
