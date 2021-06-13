package map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DirectionTest {

    private int[] TOP = {0, -1};
    private int[] TOP_RIGHT = {1, -1};
    private int[] RIGHT = {1, 0};
    private int[] BOTTOM_RIGHT = {1, 1};
    private int[] BOTTOM = {0, 1};
    private int[] BOTTOM_LEFT = {-1, 1};
    private int[] LEFT = {-1, 0};
    private int[] TOP_LEFT = {-1 ,-1};

    @Test
    @DisplayName("Test getList()")
    void getList() {
        int[][] expected = {{0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1 ,-1}};

        Assertions.assertArrayEquals(expected, Direction.getList());
    }

    @Test
    @DisplayName("Test getOppositeDirectionValue()")
    void getOppositeDirectionValue() {
        int V_TOP = 0;
        int V_TOP_RIGHT = 1;
        int V_RIGHT = 2;
        int V_BOTTOM_RIGHT = 3;
        int V_BOTTOM = 4;
        int V_BOTTOM_LEFT = 5;
        int V_LEFT = 6;
        int V_TOP_LEFT = 7;

        Assertions.assertEquals(Direction.getOppositeDirectionValue(V_TOP), V_BOTTOM);
        Assertions.assertEquals(Direction.getOppositeDirectionValue(V_TOP_RIGHT), V_BOTTOM_LEFT);
        Assertions.assertEquals(Direction.getOppositeDirectionValue(V_RIGHT), V_LEFT);
        Assertions.assertEquals(Direction.getOppositeDirectionValue(V_BOTTOM_RIGHT), V_TOP_LEFT);
        Assertions.assertEquals(Direction.getOppositeDirectionValue(V_BOTTOM), V_TOP);
        Assertions.assertEquals(Direction.getOppositeDirectionValue(V_BOTTOM_LEFT), V_TOP_RIGHT);
        Assertions.assertEquals(Direction.getOppositeDirectionValue(V_LEFT), V_RIGHT);
        Assertions.assertEquals(Direction.getOppositeDirectionValue(V_TOP_LEFT), V_BOTTOM_RIGHT);
    }

    @Test
    @DisplayName("Test getOppositeDirection()")
    void getOppositeDirection() {
        Assertions.assertArrayEquals(Direction.getOppositeDirection(TOP), BOTTOM);
        Assertions.assertArrayEquals(Direction.getOppositeDirection(TOP_RIGHT), BOTTOM_LEFT);
        Assertions.assertArrayEquals(Direction.getOppositeDirection(RIGHT), LEFT);
        Assertions.assertArrayEquals(Direction.getOppositeDirection(BOTTOM_RIGHT), TOP_LEFT);
        Assertions.assertArrayEquals(Direction.getOppositeDirection(BOTTOM), TOP);
        Assertions.assertArrayEquals(Direction.getOppositeDirection(BOTTOM_LEFT), TOP_RIGHT);
        Assertions.assertArrayEquals(Direction.getOppositeDirection(LEFT), RIGHT);
        Assertions.assertArrayEquals(Direction.getOppositeDirection(TOP_LEFT), BOTTOM_RIGHT);
    }

    @Test
    @DisplayName("Test testGetOppositeDirection()")
    void testGetOppositeDirection() {
        Assertions.assertArrayEquals(Direction.getOppositeDirection(0), BOTTOM);
        Assertions.assertArrayEquals(Direction.getOppositeDirection(1), BOTTOM_LEFT);
        Assertions.assertArrayEquals(Direction.getOppositeDirection(2), LEFT);
        Assertions.assertArrayEquals(Direction.getOppositeDirection(3), TOP_LEFT);
        Assertions.assertArrayEquals(Direction.getOppositeDirection(4), TOP);
        Assertions.assertArrayEquals(Direction.getOppositeDirection(5), TOP_RIGHT);
        Assertions.assertArrayEquals(Direction.getOppositeDirection(6), RIGHT);
        Assertions.assertArrayEquals(Direction.getOppositeDirection(7), BOTTOM_RIGHT);
    }

    @Test
    @DisplayName("Test valueOf()")
    void valueOf() {
        Assertions.assertArrayEquals(Direction.valueOf(0), TOP);
        Assertions.assertArrayEquals(Direction.valueOf(1), TOP_RIGHT);
        Assertions.assertArrayEquals(Direction.valueOf(2), RIGHT);
        Assertions.assertArrayEquals(Direction.valueOf(3), BOTTOM_RIGHT);
        Assertions.assertArrayEquals(Direction.valueOf(4), BOTTOM);
        Assertions.assertArrayEquals(Direction.valueOf(5), BOTTOM_LEFT);
        Assertions.assertArrayEquals(Direction.valueOf(6), LEFT);
        Assertions.assertArrayEquals(Direction.valueOf(7), TOP_LEFT);
    }

    @Test
    @DisplayName("Test indexOf()")
    void indexOf() {
        Assertions.assertEquals(Direction.indexOf(TOP), 0);
        Assertions.assertEquals(Direction.indexOf(TOP_RIGHT), 1);
        Assertions.assertEquals(Direction.indexOf(RIGHT), 2);
        Assertions.assertEquals(Direction.indexOf(BOTTOM_RIGHT), 3);
        Assertions.assertEquals(Direction.indexOf(BOTTOM), 4);
        Assertions.assertEquals(Direction.indexOf(BOTTOM_LEFT), 5);
        Assertions.assertEquals(Direction.indexOf(LEFT), 6);
        Assertions.assertEquals(Direction.indexOf(TOP_LEFT), 7);
    }
}