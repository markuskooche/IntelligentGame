package heuristic;

import map.Move;

import java.util.List;

public class Line {

    private Move move;
    private List<Character> stoneRow;

    public Line(Move move, List<Character> stones) {
        this.move = move;
        this.stoneRow = stones;
    }

    public Move getMove() {
        return move;
    }

    public List<Character> getStoneRow() {
        return stoneRow;
    }
}
