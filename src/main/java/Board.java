import java.util.HashMap;

public class Board {

    private char[][] field;
    private final int bombRadius;
    private final HashMap<String, Transition> transitions;
    private final int width;
    private final int height;

    public Board(char[][] field, HashMap<String, Transition> transitions, int bombRadius) {
        this.field = field;
        this.transitions = transitions;
        this.bombRadius = bombRadius;

        this.height = field.length;
        this.width = field[0].length;
    }

    public int getBombRadius() {
        return bombRadius;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public void setPiece(int height, int width, char piece) {
        this.field[height][width] = piece;
    }

    public char getPiece(int height, int width) {
        return field[height][width];
    }

    public HashMap<String, Transition> getTransition() {
        return transitions;
    }

    @Override
    public String toString() {
        StringBuilder boardString = new StringBuilder();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                boardString.append(field[y][x]);
                boardString.append(" ");
            }
            boardString.append("\n");
        }

        return boardString.toString();
    }
}
