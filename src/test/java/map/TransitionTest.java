package map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TransitionTest {

    private final int x = 2;
    private final int y = 4;
    private final int r = 6;

    private final Transition transition = new Transition(x, y, r);

    @Test
    @DisplayName("Test destination")
    void getDestination() {
        Assertions.assertArrayEquals(transition.getDestination(), new int[] {x, y, r});
    }

    @Test
    @DisplayName("Test getX")
    void getX() {
        Assertions.assertEquals(transition.getX(), x);
    }

    @Test
    @DisplayName("Test getX")
    void getY() {
        Assertions.assertEquals(transition.getY(), y);
    }

    @Test
    @DisplayName("Test getR")
    void getR() {
        Assertions.assertEquals(transition.getR(), r);
    }

    @Test
    @DisplayName("Test hash function")
    void hash() {
        Assertions.assertEquals(Transition.hash(x, y, r), 2046);
        Assertions.assertEquals(Transition.hash(49, 48, 7), 49487);
    }
}