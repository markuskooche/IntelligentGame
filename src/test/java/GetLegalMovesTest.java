import controller.Game;
import loganalyze.additional.AnalyzeParser;
import map.Move;
import map.Player;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import server.MapParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class GetLegalMovesTest {

    private Game createGame(String filename){
        Game game = null;
        Path path = Paths.get(filename);

        try {
            byte[] bytes = Files.readAllBytes(path);
            List<String> file = MapParser.createMap(bytes);
            AnalyzeParser analyzeParser =  new AnalyzeParser(1, false, true);
            game = new Game(file, analyzeParser);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return game;
    }

    @Test
    @DisplayName("Tests if the number of moves matches")
    void testOneMove() {
        Game game = createGame("maps/testMaps/transitions/map05.map");
        Player player = game.getPlayer(1);
        List<Move> legalMoves = game.getBoard().getLegalMoves(player, false);

        Assertions.assertEquals(1, legalMoves.size());

        Move legalMove = legalMoves.get(0);
        Assertions.assertTrue(legalMove.isMove(new int[] {5, 5}));
    }

    @Test
    @DisplayName("Tests if the number of moves matches and picks moves and tests it")
    void testBombMapMove() {
        Game game = createGame("maps/initialMaps/bomb.map");
        Player player = game.getPlayer(1);

        List<Move> legalMovesNoOverride = game.getBoard().getLegalMoves(player, false);
        Assertions.assertEquals(5, legalMovesNoOverride.size());

        List<Move> legalMovesWithOverride = game.getBoard().getLegalMoves(player, true);
        Assertions.assertEquals(21, legalMovesWithOverride.size());

        int testedAssertions = 0;
        for (Move move : legalMovesWithOverride) {
            if (move.isMove(new int[] {8, 1})) {
                Assertions.assertEquals(2, move.size());
                Assertions.assertFalse(move.isInversion());
                Assertions.assertFalse(move.isOverride());
                Assertions.assertFalse(move.isChoice());
                Assertions.assertFalse(move.isBonus());
                testedAssertions++;
            } else if (move.isMove(new int[] {7, 4})) {
                Assertions.assertEquals(2, move.size());
                Assertions.assertFalse(move.isInversion());
                Assertions.assertFalse(move.isOverride());
                Assertions.assertFalse(move.isChoice());
                Assertions.assertFalse(move.isBonus());
                testedAssertions++;
            } else if (move.isMove(new int[] {1, 5})) {
                Assertions.assertEquals(1, move.size());
                Assertions.assertTrue(move.isOverride());
                Assertions.assertFalse(move.isInversion());
                Assertions.assertFalse(move.isChoice());
                Assertions.assertFalse(move.isBonus());
                testedAssertions++;
            }
        }

        // tests if all Assertions has been tested
        Assertions.assertEquals(3, testedAssertions);
    }

    @Test
    @DisplayName("Tests if the number of moves matches before and after override decrease")
    void testWormholeMapMove() {
        Game game = createGame("maps/initialMaps/wormhole.map");
        Player player = game.getPlayer(1);

        List<Move> legalMovesNoOverride = game.getBoard().getLegalMoves(player, false);
        Assertions.assertEquals(4, legalMovesNoOverride.size());

        List<Move> legalMovesWithOverride = game.getBoard().getLegalMoves(player, true);
        Assertions.assertEquals(8, legalMovesWithOverride.size());

        int amount = player.getOverrideStone();
        for (int i = 0; i < amount; i++) {
            player.decreaseOverrideStone();
        }

        List<Move> legalMovesWithoutOverride = game.getBoard().getLegalMoves(player, true);
        Assertions.assertEquals(4, legalMovesWithoutOverride.size());
    }
}
