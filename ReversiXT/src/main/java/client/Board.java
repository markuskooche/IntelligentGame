package client;

import java.util.LinkedList;

public class Board {

    private static final char EXPANSION = 'x';
    private static final char INVERSION = 'i';
    private static final char CHOICE = 'c';
    private static final char BONUS = 'b';

    private static final char HIDDEN = '-';

    private final Piece[][] board;
    private final int height;
    private final int width;

    public Board(int height, int width) {
        board = new Piece[height][width];
        this.height = height;
        this.width = width;

        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width; w++) {
                board[h][w] = new Piece();
            }
        }
    }

    public void setPiece(int height, int width, char piece) {
        board[height][width].setPiece(piece);
    }

    public char getPiece(int height, int width) {
        return this.board[height][width].getPiece();
    }

    // TODO: only for development
    public Piece getElement(int height, int width) {
        return this.board[height][width];
    }

    public void setTransition(int x1, int y1, int r1, int x2, int y2, int r2) {
        board[y2][x2].setTransition(String.format("%d %d %d", x1, y1, r2));
        board[y1][x1].setTransition(String.format("%d %d %d", x2, y2, r1));
    }

    public LinkedList<String> getTransition(int x, int y) {
        return this.board[y][x].getTransition();
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public void choice(char a, char b) {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (board[i][j].getPiece() == a) {
                    board[i][j].setPiece(b);
                } else if (board[i][j].getPiece() == b) {
                    board[i][j].setPiece(a);
                }
            }
        }
    }

    public void inversion(int playerAmount) {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                String piece = String.valueOf(board[i][j].getPiece());
                if (piece.matches("1|2|3|4|5|6|7|8")) {
                    int oldPlayer = Integer.parseInt(piece);
                    int newPlayer = oldPlayer % playerAmount + 1;
                    board[i][j].setPiece(String.valueOf(newPlayer).charAt(0));
                }
            }
        }
    }

    // TODO: only for development
    public String printTransitionBoard() {
        StringBuilder boardString = new StringBuilder();

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (board[i][j].hasTransition()) {
                    boardString.append("T");
                } else {
                    boardString.append(board[i][j].getPiece());
                }

                boardString.append(" ");
            }
            boardString.append("\n");
        }

        return boardString.toString();
    }

    @Override
    public String toString() {
        StringBuilder boardString = new StringBuilder();

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                boardString.append(board[i][j].getPiece());
                boardString.append(" ");
            }
            boardString.append("\n");
        }

        return boardString.toString();
    }
}