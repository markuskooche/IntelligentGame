package map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    @Test
    @DisplayName("Test correct player initialisation")
    void testPlayerInitialisation() {
        Player player1 = new Player(1, 0, 0);
        Assertions.assertEquals(player1.getCharNumber(), '1');
        Assertions.assertEquals(player1.getIntNumber(), 1);

        Player player2 = new Player(2, 0, 0);
        Assertions.assertEquals(player2.getCharNumber(), '2');
        Assertions.assertEquals(player2.getIntNumber(), 2);

        Player player3 = new Player(3, 0, 0);
        Assertions.assertEquals(player3.getCharNumber(), '3');
        Assertions.assertEquals(player3.getIntNumber(), 3);

        Player player4 = new Player(4, 0, 0);
        Assertions.assertEquals(player4.getCharNumber(), '4');
        Assertions.assertEquals(player4.getIntNumber(), 4);

        Player player5 = new Player(5, 0, 0);
        Assertions.assertEquals(player5.getCharNumber(), '5');
        Assertions.assertEquals(player5.getIntNumber(), 5);

        Player player6 = new Player(6, 0, 0);
        Assertions.assertEquals(player6.getCharNumber(), '6');
        Assertions.assertEquals(player6.getIntNumber(), 6);

        Player player7 = new Player(7, 0, 0);
        Assertions.assertEquals(player7.getCharNumber(), '7');
        Assertions.assertEquals(player7.getIntNumber(), 7);

        Player player8 = new Player(8, 0, 0);
        Assertions.assertEquals(player8.getCharNumber(), '8');
        Assertions.assertEquals(player8.getIntNumber(), 8);
    }

    @Test
    @DisplayName("Test player overridestone")
    void testPlayerOverrideStone() {
        Player player = new Player(1, 2, 2);

        assertTrue(player.hasOverrideStone());
        Assertions.assertEquals(player.getOverrideStone(), 2);
        player.increaseOverrideStone();
        Assertions.assertEquals(player.getOverrideStone(), 3);

        player.decreaseOverrideStone();
        player.decreaseOverrideStone();
        player.decreaseOverrideStone();

        assertFalse(player.hasOverrideStone());
        Assertions.assertEquals(player.getOverrideStone(), 0);
    }

    @Test
    @DisplayName("Test player bomb")
    void testPlayerBomb() {
        Player player = new Player(1, 2, 2);

        assertTrue(player.hasBomb());
        Assertions.assertEquals(player.getBomb(), 2);
        player.increaseBomb();
        Assertions.assertEquals(player.getBomb(), 3);

        player.decreaseBomb();
        player.decreaseBomb();
        player.decreaseBomb();

        assertFalse(player.hasBomb());
        Assertions.assertEquals(player.getBomb(), 0);
    }
}