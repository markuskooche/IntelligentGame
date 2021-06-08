package heuristic;

import map.Board;
import map.Move;
import map.Player;

public class BoardMove {

    private Board board;
    private Move move;
    private Player player;
    private int cutOff;

    public BoardMove(Board board, Move move, Player player) {
        this.board = board;
        this.move = move;
        this.player = player;
        cutOff = 0;
    }

    public void addCutOff() {
        this.cutOff ++;
    }

    public int getCutOff() {
        return cutOff;
    }

    public Board getBoard() {
        return board;
    }

    public Move getMove() {
        return move;
    }

    public int[] getMovePos() {
        int [] movePos = new int [2];
        movePos[0] = move.getX();
        movePos[1] = move.getY();
        return movePos;
    }

    public Player getPlayer() {
        return player;
    }
}
