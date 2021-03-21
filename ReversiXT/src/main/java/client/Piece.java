package client;

import java.util.LinkedList;

public class Piece {

    private char piece;
    private boolean transitions;
    private LinkedList<String> list;

    public Piece() {
        this.transitions = false;
        this.piece = ' ';
    }

    public void setPiece(char piece) {
        this.piece = piece;
    }

    public char getPiece() {
        return piece;
    }

    public boolean hasTransition() {
        return transitions;
    }

    public void setTransition(String transition) {
        if (!transitions) {
            list = new LinkedList<>();
            transitions = true;
        }

        list.add(transition);
    }

    public LinkedList<String> getTransition() {
        if (!transitions) {
            return null;
        }

        return list;
    }

    @Override
    public String toString() {
        return String.valueOf(piece);
    }
}