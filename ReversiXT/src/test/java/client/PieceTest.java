package client;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;

class PieceTest {

    private Piece piece;

    @BeforeEach
    void setUp() {
        piece = new Piece();
    }

    @Test
    void testPiece() {
        piece.setPiece('1');
        Assertions.assertEquals('1', piece.getPiece());
        Assertions.assertNotEquals('2', piece.getPiece());
    }

    @Test
    void testHasTransition() {
        Assertions.assertFalse(piece.hasTransition());
        piece.setTransition("1 2 3");
        Assertions.assertTrue(piece.hasTransition());
    }

    @Test
    void testGetTransition() {
        Assertions.assertNull(piece.getTransition());

        piece.setTransition("1 2 3");
        Assertions.assertNotNull(piece.getTransition());

        LinkedList<String> list = new LinkedList<>();
        list.add("1 2 3");
        Assertions.assertEquals(list, piece.getTransition());
    }
}