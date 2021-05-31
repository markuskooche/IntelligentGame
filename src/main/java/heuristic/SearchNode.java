package heuristic;

import map.Board;
import map.Move;
import map.Player;

import java.util.ArrayList;
import java.util.List;

public class SearchNode {

    private List<SearchNode> nextChilds = new ArrayList<>();
    private SearchNode root;
    private Move move;
    private int cutOffs;
    private int depth;

    public SearchNode(SearchNode root, Move move, int depth) {
        this.root = root;
        this.move = move;
        this.depth = depth;
        cutOffs = 0;
    }

    public void addCutOff() {
        cutOffs++;
    }

    public int getCutOffs() {
        return cutOffs;
    }

    public Move getMove() {
        return move;
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

    public int getDepth() {
        return depth;
    }
}
