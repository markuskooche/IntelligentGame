public class Direction {

    private static final int[][] directions = {
            {-1, 0 }, {-1, 1}, {0, 1}, {1, 1},
            {1, 0}, {1, -1}, {0, -1}, {-1, -1}
    };

    public static int[][] getList() {
        return directions;
    }

    public static int[] valueOf(int number) {
        switch (number) {
            case 0: return directions[0];
            case 1: return directions[1];
            case 2: return directions[2];
            case 3: return directions[3];
            case 4: return directions[4];
            case 5: return directions[5];
            case 6: return directions[6];
            case 7: return directions[7];
            default: return null;
        }
    }

    public static int indexOf(int[] direction) {
        if (compare(direction, directions[0])) {
            return 0;
        } else if (compare(direction, directions[1])) {
            return 1;
        } else if (compare(direction, directions[2])) {
            return 2;
        } else if (compare(direction, directions[3])) {
            return 3;
        } else if (compare(direction, directions[4])) {
            return 4;
        } else if (compare(direction, directions[5])) {
            return 5;
        } else if (compare(direction, directions[6])) {
            return 6;
        } else if (compare(direction, directions[7])) {
            return 7;
        } else {
            return -1;
        }
    }

    private static boolean compare(int[] a, int[] b) {
        return a[0] == b[0] && a[1] == b[1];
    }
}
