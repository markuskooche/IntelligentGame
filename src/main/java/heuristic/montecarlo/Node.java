package heuristic.montecarlo;


import map.Move;

import java.util.LinkedList;
import java.util.List;

public class Node {

    private State state;
    private Node parent;
    private final List<Node> childArray;
    private int n;
    private int[] q;
    private final boolean isTerminal;
    private List<Move> possibleMoves;
    private int playerAmount;

    public Node(State state, Node parent, int playerAmount) {
        this.state = state;
        this.parent = parent;
        childArray = new LinkedList<>();
        n = 0;
        q = new int[playerAmount];
        isTerminal = state.isTerminal();
        possibleMoves = state.getPlayerMoves();
        this.playerAmount = playerAmount;
    }

    public void addChild(Node node) {
        childArray.add(node);
    }

    public void increaseN() {
        n++;
    }

    public int getN() {
        return n;
    }

    public void increaseQ(int[] evaluation) {
        for (int i = 0; i < playerAmount; i++) {
            q[i] += evaluation[i];
        }
    }

    public int getQ(int player) {
        int playerScore = q[player - 1];
        int bestScore = playerScore;
        int bestPlayer = player;

        for (int i = 0; i < playerAmount; i++) {
            if (i == player - 1) {
                continue;
            }

            if (bestScore < q[i]) {
                bestScore = q[i];
                bestPlayer = i + 1;
            }
        }
        //TODO weiter verfeinern: nicht 2ter platz ist besser als 6ter (hier nicht beachtet)
        //     hier könnte man auch hohen abstand noch besser bewerten
        if (bestPlayer == player) {
            return 1;
        } else {
            return -1;
        }
    }

    public Node getParent() {
        return parent;
    }

    public List<Node> getChildren() {
        return childArray;
    }

    public boolean isTerminal() {
        return isTerminal;
    }

    public boolean isFullyExpanded(){
        return possibleMoves.isEmpty();
    }

    public State getState() {
        return state;
    }

    public Move getUntriedMove() {
        if(possibleMoves.isEmpty()){
            return null;
        }else{
            Move newMove = possibleMoves.get(0);
            possibleMoves.remove(0);
            return newMove;
        }

    }
}
