/*
package heuristic.montecarlo;

import map.Player;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NodeTest {

    @Test
    @DisplayName("Tests if the number of moves matches")
    void testGetQ() {
        Player[] players = {
                new Player(0, 0, 0),
                new Player(0, 0, 0),
                new Player(0, 0, 0),
                new Player(0, 0, 0)
        };

        State state = new State(null, null, null, players, 3);
        Node node = new Node(state, null, 4);

        int[] scores = {10, 30, 20, 15};
        node.increaseQ(scores);

        int evaluation1 = node.getQ(3);
        Assertions.assertEquals(evaluation1, -1);

        int evaluation2 = node.getQ(2);
        Assertions.assertEquals(evaluation2, 1);
    }

}
*/