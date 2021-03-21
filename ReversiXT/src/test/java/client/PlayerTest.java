package client;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PlayerTest {

    private Player playerOne;
    private Player playerTwo;

    @BeforeEach
    void setUp() {
        playerOne = new Player(4, 2);
        playerTwo = new Player(8, 6);
    }

    @Test
    void testBomb() {
        Assertions.assertEquals(2, playerOne.getBombs());
        Assertions.assertEquals(6, playerTwo.getBombs());

        playerOne.setBombs(playerOne.getBombs() + 1);
        playerTwo.setBombs(playerTwo.getBombs() - 1);

        Assertions.assertEquals(3, playerOne.getBombs());
        Assertions.assertEquals(5, playerTwo.getBombs());
    }

    @Test
    void testOverrideStones() {
        Assertions.assertEquals(4, playerOne.getOverridesStones());
        Assertions.assertEquals(8, playerTwo.getOverridesStones());

        playerOne.setOverridesStones(playerOne.getOverridesStones() + 1);
        playerTwo.setOverridesStones(playerTwo.getOverridesStones() - 1);

        Assertions.assertEquals(5, playerOne.getOverridesStones());
        Assertions.assertEquals(7, playerTwo.getOverridesStones());
    }
}