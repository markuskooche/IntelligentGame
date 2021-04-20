package loganalyze;

public class FieldStatistic {

    private final char[][] field;

    public FieldStatistic(char[][] field) {
        int height = field.length;
        int width = field[0].length;
        this.field = new char[height][width];

        for (int y = 0; y < height; y++) {
            System.arraycopy(field[y], 0, this.field[y], 0, width);
        }
    }

    public String getPiece(int x, int y) {
        return (field[y][x] + " ");
    }

    public char[][] getField() {
        return field;
    }
}
