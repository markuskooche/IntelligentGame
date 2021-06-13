package map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MoveTest {

    private Move move;

    @BeforeEach
    void initializeMove() {
        move = new Move();
    }


    @Test
    @DisplayName("Test add()")
    void add() {
        int[] position = {1, 2};
        move.add(position);

        Assertions.assertEquals(move.getList().size(), 1);
        Assertions.assertArrayEquals(move.getList().get(0), position);
    }

    @Test
    @DisplayName("Test add()")
    void getList() {
        Assertions.assertEquals(move.getList().size(), 0);
    }

    @Test
    @DisplayName("Test add()")
    void isBonus() {
        Assertions.assertFalse(move.isBonus());

        move.setBonus();
        Assertions.assertTrue(move.isBonus());
    }

    @Test
    @DisplayName("Test isChoice()")
    void isChoice() {
        Assertions.assertFalse(move.isChoice());

        move.setChoice();
        Assertions.assertTrue(move.isChoice());
    }

    @Test
    @DisplayName("Test isInversion()")
    void isInversion() {
        Assertions.assertFalse(move.isInversion());

        move.setInversion();
        Assertions.assertTrue(move.isInversion());
    }

    @Test
    @DisplayName("Test isOverride()")
    void isOverride() {
        Assertions.assertFalse(move.isOverride());

        move.setOverride();
        Assertions.assertTrue(move.isOverride());
    }

    @Test
    @DisplayName("Test isEmpty()")
    void isEmpty() {
        Assertions.assertTrue(move.isEmpty());

        int[] position = {1, 2};
        move.add(position);
        Assertions.assertFalse(move.isEmpty());
    }

    @Test
    @DisplayName("Test contains()")
    void contains() {
        int x = 1;
        int y = 2;
        int[] position = {x, y};
        move.add(position);

        Assertions.assertTrue(move.contains(x, y));
    }

    @Test
    @DisplayName("Test size()")
    void size() {
        Assertions.assertEquals(move.size(), 0);

        int[] position1 = {1, 2};
        move.add(position1);
        Assertions.assertEquals(move.size(), 1);

        int[] position2 = {3, 4};
        move.add(position2);
        Assertions.assertEquals(move.size(), 2);
    }

    @Test
    @DisplayName("Test isMove()")
    void isMove() {
        int[] position = {1, 2};
        move.add(position);
        Assertions.assertTrue(move.isMove(position));
        Assertions.assertFalse(move.isMove(new int[] {3, 4}));
    }

    @Test
    @DisplayName("Test getX()")
    void getX() {
        int[] position = {1, 2};
        move.add(position);
        Assertions.assertEquals(move.getX(), 1);
    }

    @Test
    @DisplayName("Test getY()")
    void getY() {
        int[] position = {1, 2};
        move.add(position);
        Assertions.assertEquals(move.getY(), 2);
    }

    @Test
    @DisplayName("Test merge()")
    void merge() {
        move.add(new int[] {1, 2});

        Move second = new Move();
        second.add(new int[] {2, 3});

        move.merge(second);

        Assertions.assertEquals(move.getX(), 1);
        Assertions.assertEquals(move.getY(), 2);
        Assertions.assertEquals(move.size(), 2);
    }

    @Test
    @DisplayName("Test getPlayer()")
    void getPlayer() {
        Player player = new Player(1, 0, 0);
        move.setPlayer(player);

        Assertions.assertEquals(move.getPlayer(), player);
    }
}