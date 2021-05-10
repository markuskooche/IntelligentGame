package heuristic;

import map.Board;
import map.Player;

import java.util.ArrayList;
import java.util.List;

public class SearchNode {

    private List<SearchNode> nextChilds = new ArrayList<>();
    private SearchNode root;
    private Board board;
    private List<BoardMove> executedMoves;
    private Player player;
    private boolean max;
    private int depth;
    private int pickedBoardValue;

    public SearchNode(SearchNode root, Board board, List<BoardMove> executedMoves, Player player, boolean max, int depth) {
        this.root = root;
        this.board = board;
        this.executedMoves = executedMoves;
        this.player = player;
        this.max = max;
        this.depth = depth;
    }

    public void setPickedBoardValue(int value) {
        this.pickedBoardValue = value;
    }

    public void addNextChild(SearchNode child) {
        nextChilds.add(child);
    }

    public void setNextChilds(List<SearchNode> nextChilds) {
        this.nextChilds = nextChilds;
    }

    public List<SearchNode> getNextChilds() {
        return nextChilds;
    }

    public Board getBoard() {
        return board;
    }

    public List<BoardMove> getExecutedMoves() {
        return executedMoves;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isMax() {
        return max;
    }

    public int getDepth() {
        return depth;
    }
}
