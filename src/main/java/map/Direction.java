package map;

/**
 * The map.Direction class is a static class to get single directions as well
 * as the whole list and to convert a direction into an array or vice versa.
 *
 * @author Benedikt Halbritter
 * @author Iwan Eckert
 * @author Markus Koch
 */
public class Direction {

    public static final int[] TOP = {0, -1};
    public static final int[] TOP_RIGHT = {1, -1};
    public static final int[] RIGHT = {1, 0};
    public static final int[] BOTTOM_RIGHT = {1, 1};
    public static final int[] BOTTOM = {0, 1};
    public static final int[] BOTTOM_LEFT = {-1, 1};
    public static final int[] LEFT = {-1, 0};
    public static final int[] TOP_LEFT = {-1 ,-1};

    private static final int[][] directions = {
            TOP, TOP_RIGHT, RIGHT, BOTTOM_RIGHT, BOTTOM, BOTTOM_LEFT, LEFT, TOP_LEFT
    };

    /**
     * Static method to get a list of all directions.
     *
     * @return list of all directions
     */
    public static int[][] getList() {
        return directions;
    }

    /**
     *Static method to get the opposite direction Value
     *
     * @param directionValue the current direction Value
     * @return the opposite direction Value
     */
    public static int getOppositeDirectionValue(int directionValue){
        return (directionValue + 4) % 8;
    }

    /**
     *Static method to get the opposite direction
     *
     * @param directionValue the current direction Value
     * @return the opposite direction
     */
    public static int[] getOppositeDirection(int directionValue){
        return directions[(directionValue + 4) % 8];
    }

    /**
     *Static method to get the opposite direction
     *
     * @param direction the current direction Value
     * @return the opposite direction
     */
    public static int[] getOppositeDirection(int[] direction){
        int[] myDirection = new int[2];
        myDirection[0] = direction[0] * (-1);
        myDirection[1] = direction[1] * (-1);
        return myDirection;
    }

    /**
     * Static method to get the direction array by a passed direction number.
     *
     * @param number direction number
     *
     * @return direction array from a passed direction number
     */
    public static int[] valueOf(int number) {
        switch (number) {
            case 0: return TOP;
            case 1: return TOP_RIGHT;
            case 2: return RIGHT;
            case 3: return BOTTOM_RIGHT;
            case 4: return BOTTOM;
            case 5: return BOTTOM_LEFT;
            case 6: return LEFT;
            case 7: return TOP_LEFT;
            default: return null;
        }
    }

    /**
     * Static method to get the direction number by a passed direction array.
     *
     * @param direction direction array
     *
     * @return direction number from a passed direction array
     */
    public static int indexOf(int[] direction) {
        if (compare(direction, TOP)) {
            return 0;
        } else if (compare(direction, TOP_RIGHT)) {
            return 1;
        } else if (compare(direction, RIGHT)) {
            return 2;
        } else if (compare(direction, BOTTOM_RIGHT)) {
            return 3;
        } else if (compare(direction, BOTTOM)) {
            return 4;
        } else if (compare(direction, BOTTOM_LEFT)) {
            return 5;
        } else if (compare(direction, LEFT)) {
            return 6;
        } else if (compare(direction, TOP_LEFT)) {
            return 7;
        } else {
            return -1;
        }
    }

    /**
     * Compares to directions
     *
     * @param a first direction
     * @param b second direction
     * @return true if directions are the same
     */
    private static boolean compare(int[] a, int[] b) {
        return a[0] == b[0] && a[1] == b[1];
    }
}
