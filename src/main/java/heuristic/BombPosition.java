package heuristic;

/**
 * The BombPosition class searches for the best position of a bomb. This class uses a greedy approach.
 *
 * @author Benedikt Halbritter
 * @author Iwan Eckert
 * @author Markus Koch
 */
public class BombPosition {

    private final int[][] evaluatedField;
    private final int[][] cachedField;

    private final char[][] field;
    private final int radius;

    private final int height;
    private final int width;

    /**
     * Initialize a BombPosition object.
     *
     * @param field the field where a bomb must be found
     * @param playerNumber the player number of the client
     * @param radius the radius of a bomb
     */
    public BombPosition(char[][] field, char playerNumber, int radius) {
        this.field = field;
        this.radius = radius;

        this.height = field.length;
        this.width = field[0].length;

        this.evaluatedField = new int[height][width];
        this.cachedField = new int[height][width];
        evaluateField(playerNumber);
    }

    /**
     * Evaluating a field.
     *
     * @param player the player number of the client
     */
    private void evaluateField(char player) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (field[y][x] == player) {
                    evaluatedField[y][x] = -5;
                } else if ("12345678".indexOf(field[y][x]) != -1) {
                    evaluatedField[y][x] = 1;
                } else {
                    evaluatedField[y][x] = 0;
                }
            }
        }

        //print(evaluatedField,  " \"EF\"");
    }

    /**
     * Search for the best bomb position.
     *
     * @return returns the best bom position [x, y]
     */
    public int[] getBestBombPosition() {
        int currentBestPosition = Integer.MIN_VALUE;
        int bestX = 0;
        int bestY = 0;

        for (int y = 0; y < height; y++) {
            calculateRow(y);
        }

        //print(cachedField, " \"CF\"");

        for (int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                if (field[y][x] != '-') {
                    int tmpCachedTotal = 0;

                    // update the value inside the radius (vertically)
                    for (int c = -radius; c <= radius; c++) {
                        int row = (y + c);

                        if (row >= 0 && row < height) {
                            tmpCachedTotal += cachedField[row][x];
                        }
                    }

                    // if the calculated position is better then the current best position
                    if (tmpCachedTotal > currentBestPosition) {
                        currentBestPosition = tmpCachedTotal;
                        bestX = x;
                        bestY = y;
                    }
                }
            }
        }

        return new int[] {bestX, bestY};
    }

    /**
     * Create a temporary field of values.
     *
     * @param row the row which should be calculated and stored
     */
    private void calculateRow(int row) {
        for(int x = 0; x < width; x++) {
            int tmpTotal = 0;

            // update the value inside the radius (horizontally)
            for (int c = -radius; c <= radius; c++) {
                int column = (x + c);

                if (column >= 0 && column < width) {
                    tmpTotal += evaluatedField[row][column];
                }
            }

            cachedField[row][x] = tmpTotal;
        }
    }

    private void print(int[][] printField, String tableName) {
        System.out.print(tableName + "  ");
        for (int x = 0; x < width; x++) {
            System.out.format("%3d ", x);
        }
        System.out.println();

        System.out.print("    /--");
        for (int x = 0; x < width; x++) {
            System.out.print("----");
        }
        System.out.println();

        for (int y = 0; y < height; y++) {
            System.out.format("%3d |  ", y);
            for (int x = 0; x < width; x++) {
                System.out.format("%3d ", printField[y][x]);
            }
            System.out.println();
        }
        System.out.println();
    }

    /*
    public static void main(String[] args) {
        char[][] field = {
                {'1', '0', '1', '0', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2'},
                {'1', '0', '0', '1', '0', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2'},
                {'1', '0', '0', '0', '1', '2', '2', '2', '1', '-', '-', '-', '-', '1', '1', '2', '2', '2', '2', '2', '2', '2'},
                {'1', '0', '0', '1', '2', '2', '2', '1', '-', '1', '1', '1', '2', '-', '1', '1', '1', '2', '2', '2', '2', '2'},
                {'1', '1', '1', '2', '2', '2', '1', '-', '2', '1', '1', '2', '2', '2', '-', '1', '2', '1', '2', '1', '2', '2'},
                {'1', '2', '2', '2', '2', '2', '-', '2', '2', '1', '1', '2', '2', '2', '1', '-', '2', '2', '2', '1', '2', '2'},
                {'1', '2', '2', '2', '1', '2', '-', '2', '2', '1', '2', '1', '2', '2', '1', '-', '2', '1', '1', '1', '2', '2'},
                {'1', '2', '1', '1', '2', '2', '-', '2', '2', '1', '2', '2', '1', '2', '1', '-', '2', '1', '1', '2', '2', '2'},
                {'1', '2', '1', '1', '1', '2', '-', '2', '1', '1', '2', '1', '2', '1', '1', '-', '2', '1', '2', '2', '2', '2'},
                {'1', '2', '2', '1', '-', '-', '-', '1', '1', '1', '1', '2', '2', '1', '1', '-', '-', '-', '1', '2', '2', '2'},
                {'1', '2', '2', '1', '-', '1', '1', '2', '2', '1', '2', '2', '2', '2', '2', '1', '2', '-', '1', '1', '2', '2'},
                {'1', '2', '2', '1', '1', '1', '-', '1', '2', '1', '1', '2', '2', '2', '2', '-', '1', '2', '1', '2', '2', '2'},
                {'1', '2', '2', '2', '1', '1', '-', '1', '1', '1', '2', '1', '2', '2', '2', '-', '1', '1', '1', '1', '2', '2'},
                {'1', '2', '2', '2', '2', '-', '1', '1', '1', '1', '1', '2', '2', '1', '1', '1', '-', '1', '1', '1', '2', '2'},
                {'1', '2', '1', '2', '-', '2', '2', '1', '2', '1', '1', '2', '2', '1', '1', '2', '1', '-', '1', '1', '2', '2'},
                {'1', '2', '2', '-', '1', '2', '2', '1', '1', '1', '2', '1', '2', '1', '2', '1', '2', '2', '-', '1', '2', '2'},
                {'1', '1', '2', '-', '-', '1', '1', '2', '1', '1', '1', '1', '1', '2', '1', '2', '2', '-', '-', '1', '2', '2'},
                {'1', '1', '1', '2', '-', '-', '-', '-', '1', '2', '1', '1', '2', '1', '-', '-', '-', '-', '1', '1', '2', '2'},
                {'1', '2', '2', '1', '2', '2', '2', '1', '-', '-', '1', '1', '-', '-', '1', '0', '0', '0', '0', '1', '2', '2'},
                {'1', '2', '1', '1', '1', '2', '2', '2', '1', '1', '-', '-', '1', '0', '0', '1', '0', '0', '2', '1', '2', '0'},
                {'1', '1', '1', '1', '1', '1', '1', '1', '1', '1', '1', '1', '0', '1', '0', '0', '0', '0', '0', '0', '1', '2'},
                {'1', '1', '1', '1', '1', '1', '1', '1', '1', '1', '1', '0', '0', '0', '1', '0', '0', '0', '0', '0', '0', '0'}
        };


        BombPosition bomb = new BombPosition(field, '1', 2);
        int[] position = bomb.getBestBombPosition();
        System.out.println("POSITION: [X: " + position[0] + " || Y: " + position[1] + "]");
    }
     */
}
