package client.enums;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DirectionTest {

    @Test
    void testIndex() {
        Assertions.assertEquals(5, Direction.getIndex(Direction.BOTTOM_LEFT));
        Assertions.assertEquals(2, Direction.getIndex("RIGHT"));

        Assertions.assertNotEquals(6, Direction.getIndex(Direction.BOTTOM_LEFT));
        Assertions.assertNotEquals(1, Direction.getIndex("RIGHT"));
    }

    @Test
    void testName() {
        Assertions.assertEquals(Direction.BOTTOM, Direction.getName(4));
        Assertions.assertNotEquals(Direction.BOTTOM, Direction.getName(5));
    }

    @Test
    void testEqual() {
        Assertions.assertSame(Direction.BOTTOM_LEFT, Direction.getName(5));
        Assertions.assertTrue(Direction.isEqual(Direction.BOTTOM, 4));

        Assertions.assertNotSame(Direction.BOTTOM_LEFT, Direction.getName(6));
        Assertions.assertFalse(Direction.isEqual(Direction.BOTTOM, 5));
    }

}