package heuristic.bombposition;

public class FieldPrinter {

    public static void print(int[][] printField, String tableName) {
        int height = printField.length;
        int width = printField[0].length;

        System.out.print(tableName + "  ");
        for (int x = 0; x < width; x++) {
            System.out.format("%5d ", x);
        }
        System.out.println();

        System.out.print("    /--");
        for (int x = 0; x < width; x++) {
            System.out.print("------");
        }
        System.out.println();

        for (int y = 0; y < height; y++) {
            System.out.format("%3d |  ", y);
            for (int x = 0; x < width; x++) {
                if (printField[y][x] == Integer.MIN_VALUE) {
                    System.out.print("   -∞ ");
                } else {
                    System.out.format("%5d ", printField[y][x]);
                }
            }
            System.out.println();
        }
        System.out.println();
    }
}
