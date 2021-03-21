package client;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;

class BoardTest {

    private Board board;

    @BeforeEach
    void setUp() {
        board = new Board(4, 4);

        board.setPiece(0, 0, '0');
        board.setPiece(0, 1, '0');
        board.setPiece(0, 2, '0');
        board.setPiece(0, 3, '0');

        board.setPiece(1, 0, '0');
        board.setPiece(1, 1, '1');
        board.setPiece(1, 2, '2');
        board.setPiece(1, 3, '0');

        board.setPiece(2, 0, '0');
        board.setPiece(2, 1, '2');
        board.setPiece(2, 2, '1');
        board.setPiece(2, 3, '0');

        board.setPiece(3, 0, '0');
        board.setPiece(3, 1, '0');
        board.setPiece(3, 2, '0');
        board.setPiece(3, 3, '0');

        board.setTransition(3, 0, 4, 0, 0, 0);

        board.setTransition(3, 2, 3, 0, 1, 1);
        board.setTransition(3, 2, 4, 0, 2, 0);
        board.setTransition(3, 2, 5, 0, 3, 7);
    }

    @Test
    void testWidth() {
        Assertions.assertEquals(4, board.getWidth());
        Assertions.assertNotEquals(6, board.getWidth());
    }

    @Test
    void testHeight() {
        Assertions.assertEquals(4, board.getHeight());
        Assertions.assertNotEquals(6, board.getHeight());
    }

    @Test
    void testGetPiece() {
        Assertions.assertEquals('1', board.getPiece(1, 1));
        Assertions.assertEquals('2', board.getPiece(2, 1));
        Assertions.assertEquals('0', board.getPiece(3, 3));

        Assertions.assertNotEquals('2', board.getPiece(1, 1));
        Assertions.assertNotEquals('c', board.getPiece(2, 0));
        Assertions.assertNotEquals('i', board.getPiece(3, 3));
    }

    @Test
    void testGetTransition() {
        LinkedList<String> one = new LinkedList<>();
        one.add("0 0 4");

        LinkedList<String> two = new LinkedList<>();
        two.add("3 0 0");

        Assertions.assertEquals(one, board.getTransition(3, 0));
        Assertions.assertEquals(two, board.getTransition(0, 0));

        Assertions.assertNull(board.getTransition(2, 3));


        LinkedList<String> multiple = new LinkedList<>();
        multiple.add("0 1 3");
        multiple.add("0 2 4");
        multiple.add("0 3 5");

        Assertions.assertEquals(multiple, board.getTransition(3, 2));
    }

    @Test
    void testToString() {
        String output = "0 0 0 0 \n0 1 2 0 \n0 2 1 0 \n0 0 0 0 \n";

        Assertions.assertEquals(board.toString(), output);
    }

    @Test
    void testInversion() {
        board.inversion(2);

        String output = "0 0 0 0 \n0 2 1 0 \n0 1 2 0 \n0 0 0 0 \n";
        Assertions.assertEquals(board.toString(), output);
    }

    @Test
    void testChoice() {
        board.choice('1', '2');

        String output = "0 0 0 0 \n0 2 1 0 \n0 1 2 0 \n0 0 0 0 \n";
        Assertions.assertEquals(board.toString(), output);
    }
}