package client.enums;

public enum Direction {

    TOP,
    TOP_RIGHT,
    RIGHT,
    BOTTOM_RIGHT,
    BOTTOM,
    BOTTOM_LEFT,
    LEFT,
    TOP_LEFT;

    public static Direction getName(int index) {
        return Direction.values()[index];
    }

    public static int getIndex(Direction direction) {
        return direction.ordinal();
    }

    public static int getIndex(String name) {
        return Direction.valueOf(name).ordinal();
    }

    public static boolean isEqual(Direction direction, int index) {
        return direction == Direction.values()[index];
    }
}
