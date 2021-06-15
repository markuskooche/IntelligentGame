package heuristic;

import map.Move;

import java.util.List;

public class Line {

    private Move move;
    private List<Character> stoneRow;
    private int moveValue;

    public Line(Move move, List<Character> stones) {
        this.move = move;
        this.stoneRow = stones;
    }

    public int getMoveValue() {
        return moveValue;
    }

    public void setMoveValue(int moveValue) {
        this.moveValue = moveValue;
    }

    public Move getMove() {
        return move;
    }

    public List<Character> getStoneRow() {
        return stoneRow;
    }
}
