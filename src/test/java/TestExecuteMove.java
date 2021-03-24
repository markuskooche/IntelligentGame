import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class TestExecuteMove {


    private Game createGame(String filename){
        Game game = null;
        Path path = Paths.get(filename);

        try {
            List<String> file = Files.lines(path).collect(Collectors.toList());
            game = new Game(file);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return game;
    }

    @Test
    @DisplayName("Check for the detection of false inputs without transactions or overrideStones")
    void checkFalseInputs() {

        // this is the expected output of the map after all moves
        char[][] finalArray = {{'0', '0', '0', '0', '0', '0', '0', '0', '0'},
                               {'0', '0', '0', '0', '1', '2', '0', '0', '0'},
                               {'0', '0', '0', '0', '2', '1', '0', '0', '0'},
                               {'0', '0', '0', '0', '0', '0', '0', '0', '0'},
                               {'0', '0', '0', '0', '0', '0', '0', '0', '0'}};

        Game game = createGame("maps/benesTestMaps/EasyTestMap.map");
        //checks if a move to a Edge is valid
        boolean moveIsValid = game.getBoard().executeMove(8 , 4, 1);
        assertFalse(moveIsValid);
        //check if the board is still the same
        assertArrayEquals(game.getBoard().getField(),finalArray);

        //check for moving to a field, already
        moveIsValid = game.getBoard().executeMove(4 , 1, 1);
        assertFalse(moveIsValid);
        //check if the board is still the same
        assertArrayEquals(game.getBoard().getField(),finalArray);

        //checks whats happens if the wrong player is called
        moveIsValid = game.getBoard().executeMove(5 , 1, 3);
        assertFalse(moveIsValid);
        assertArrayEquals(game.getBoard().getField(),finalArray);

        //check for input that has no stone of another player in between
        moveIsValid = game.getBoard().executeMove(4 , 0, 1);
        assertFalse(moveIsValid);
        assertArrayEquals(game.getBoard().getField(),finalArray);

        //check for input that has no stone of another player in between
        moveIsValid = game.getBoard().executeMove(6 , 3, 1);
        assertFalse(moveIsValid);
        assertArrayEquals(game.getBoard().getField(),finalArray);

    }

    @Test
    @DisplayName("Simulates a few turns of a game to see if the board is updated properly")
    void simulateGame() {

        Game game = createGame("maps/benesTestMaps/EasyTestMap.map");

        //Move 1
        boolean moveIsValid = game.getBoard().executeMove(4 , 3, 1);
        assertTrue(moveIsValid);

        char[][] expectedResult1 = {{'0', '0', '0', '0', '0', '0', '0', '0', '0'},
                                    {'0', '0', '0', '0', '1', '2', '0', '0', '0'},
                                    {'0', '0', '0', '0', '1', '1', '0', '0', '0'},
                                    {'0', '0', '0', '0', '1', '0', '0', '0', '0'},
                                    {'0', '0', '0', '0', '0', '0', '0', '0', '0'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult1);

        //Move 2
        moveIsValid = game.getBoard().executeMove(3 , 3, 2);
        assertTrue(moveIsValid);

        char[][] expectedResult2 = {{'0', '0', '0', '0', '0', '0', '0', '0', '0'},
                                    {'0', '0', '0', '0', '1', '2', '0', '0', '0'},
                                    {'0', '0', '0', '0', '2', '1', '0', '0', '0'},
                                    {'0', '0', '0', '2', '1', '0', '0', '0', '0'},
                                    {'0', '0', '0', '0', '0', '0', '0', '0', '0'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult2);

        //Move 3
        moveIsValid = game.getBoard().executeMove(5 , 1, 1);
        assertTrue(moveIsValid);

        char[][] expectedResult3 = {{'0', '0', '0', '0', '0', '1', '0', '0', '0'},
                                    {'0', '0', '0', '0', '1', '1', '0', '0', '0'},
                                    {'0', '0', '0', '0', '2', '1', '0', '0', '0'},
                                    {'0', '0', '0', '2', '1', '0', '0', '0', '0'},
                                    {'0', '0', '0', '0', '0', '0', '0', '0', '0'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult3);

        //Move 4
        moveIsValid = game.getBoard().executeMove(6 , 0, 2);
        assertTrue(moveIsValid);

        char[][] expectedResult4 = {{'0', '0', '0', '0', '0', '1', '2', '0', '0'},
                                    {'0', '0', '0', '0', '1', '2', '0', '0', '0'},
                                    {'0', '0', '0', '0', '2', '1', '0', '0', '0'},
                                    {'0', '0', '0', '2', '1', '0', '0', '0', '0'},
                                    {'0', '0', '0', '0', '0', '0', '0', '0', '0'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult4);

        //Move 5
        moveIsValid = game.getBoard().executeMove(7 , 0, 1);
        assertTrue(moveIsValid);

        char[][] expectedResult5 = {{'0', '0', '0', '0', '0', '1', '1', '1', '0'},
                                    {'0', '0', '0', '0', '1', '2', '0', '0', '0'},
                                    {'0', '0', '0', '0', '2', '1', '0', '0', '0'},
                                    {'0', '0', '0', '2', '1', '0', '0', '0', '0'},
                                    {'0', '0', '0', '0', '0', '0', '0', '0', '0'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult5);

        //Move 6
        moveIsValid = game.getBoard().executeMove(4 , 4, 2);
        assertTrue(moveIsValid);

        char[][] expectedResult6 = {{'0', '0', '0', '0', '0', '1', '1', '1', '0'},
                                    {'0', '0', '0', '0', '1', '2', '0', '0', '0'},
                                    {'0', '0', '0', '0', '2', '1', '0', '0', '0'},
                                    {'0', '0', '0', '2', '2', '0', '0', '0', '0'},
                                    {'0', '0', '0', '0', '2', '0', '0', '0', '0'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult6);

        //Move 7
        moveIsValid = game.getBoard().executeMove(2 , 4, 1);
        assertTrue(moveIsValid);

        char[][] expectedResult7 = {{'0', '0', '0', '0', '0', '1', '1', '1', '0'},
                                    {'0', '0', '0', '0', '1', '1', '0', '0', '0'},
                                    {'0', '0', '0', '0', '1', '1', '0', '0', '0'},
                                    {'0', '0', '0', '1', '2', '0', '0', '0', '0'},
                                    {'0', '0', '1', '0', '2', '0', '0', '0', '0'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult7);

        //Move 8 This move is invalid because there is a 0 between the stones
        moveIsValid = game.getBoard().executeMove(1 , 4, 1);
        assertFalse(moveIsValid);

        assertArrayEquals(game.getBoard().getField(),expectedResult7);

    }

    @Test
    @DisplayName("Test if the turns over holes are invalid")
    void testTurnWithHolesInBetween() {

        Game game = createGame("maps/benesTestMaps/TestMapForHoles.map");

        boolean moveIsValid = game.getBoard().executeMove(1 , 4, 2);
        assertFalse(moveIsValid);
    }

    @Test
    @DisplayName("Test for all directions")
    void testAllDirection() {

        Game game = createGame("maps/benesTestMaps/AllDirectionsTestMap.map");

        boolean moveIsValid = game.getBoard().executeMove(3 , 2, 2);
        assertTrue(moveIsValid);

        char[][] expectedResult = {{'0', '2', '2', '2', '2', '2', '0', '0', '0'},
                                   {'0', '2', '2', '2', '2', '2', '0', '0', '0'},
                                   {'0', '2', '2', '2', '2', '2', '0', '0', '0'},
                                   {'0', '2', '2', '2', '2', '2', '0', '0', '0'},
                                   {'0', '2', '2', '2', '2', '2', '0', '0', '0'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult);

    }

    @Test
    @DisplayName("Test the 1. evil Map")
    void TestEvilMap1() {

        Game game = createGame("maps/evilMaps/boeseMap01.map");
        boolean moveIsValid = game.getBoard().executeMove(3 , 0, 1);

        assertFalse(moveIsValid);

    }

    @Test
    @DisplayName("Test the 2. evil Map")
    void TestEvilMap2() {

        Game game = createGame("maps/evilMaps/boeseMap02.map");
        boolean moveIsValid = game.getBoard().executeMove(4 , 0, 1);

        assertTrue(moveIsValid);

        char[][] expectedResult = {{'1', '1', '1', '1', '1', '1', '1', '1', '1', '1'},
                                   {'-', '-', '-', '-', '0', '1', '2', '0', '-', '-'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult);

    }

    @Test
    @DisplayName("Test the 3. evil Map")
    void TestEvilMap3() {

        Game game = createGame("maps/evilMaps/boeseMap03.map");
        boolean moveIsValid = game.getBoard().executeMove(2 , 0, 1);

        assertTrue(moveIsValid);

        char[][] expectedResult = {{'0', '0', '1'},
                                   {'0', '1', '1'},
                                   {'1', '0', '1'},
                                   {'1', '1', '1'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult);

    }

    @Test
    @DisplayName("Test the 4. evil Map")
    void TestEvilMap4() {

        Game game = createGame("maps/evilMaps/boeseMap04.map");
        boolean moveIsValid = game.getBoard().executeMove(0 , 2, 1);

        assertTrue(moveIsValid);

        char[][] expectedResult = {{'1', '1', '1'},
                                   {'0', '1', '1'},
                                   {'1', '0', '1'},
                                   {'1', '1', '1'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult);

    }

    @Test
    @DisplayName("Test the 5. evil Map")
    void TestEvilMap5() {

        Game game = createGame("maps/evilMaps/boeseMap05.map");
        boolean moveIsValid = game.getBoard().executeMove(0 , 2, 1);

        assertTrue(moveIsValid);

        char[][] expectedResult = {{'1', '1', '1'},
                                   {'0', '1', '2'},
                                   {'1', '0', '2'},
                                   {'2', '2', '1'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult);

    }

    @Test
    @DisplayName("Test the 6. evil Map")
    void TestEvilMap6() {

        Game game = createGame("maps/evilMaps/boeseMap06.map");
        boolean moveIsValid = game.getBoard().executeMove(4 , 2, 1);

        assertTrue(moveIsValid);

        char[][] expectedResult = {{'-', '-', '-', '-', '-', '-', '-', '-'},
                                   {'-', '-', '1', '-', '-', '1', '-', '-'},
                                   {'-', '1', '1', '1', '1', '1', '1', '-'},
                                   {'-', '-', '1', '-', '-', '1', '-', '-'},
                                   {'-', '-', '1', '-', '-', '1', '-', '-'},
                                   {'-', '-', '1', '-', '-', '1', '-', '-'},
                                   {'-', '-', '1', '-', '-', '1', '-', '-'},
                                   {'-', '-', '-', '-', '-', '-', '-', '-'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult);

    }

    @Test
    @DisplayName("Test the 7. evil Map")
    void TestEvilMap7() {

        Game game = createGame("maps/evilMaps/boeseMap07.map");
        boolean moveIsValid = game.getBoard().executeMove(6 , 0, 1);

        assertTrue(moveIsValid);

        char[][] expectedResult = {{'-', '-', '-', '-', '-', '-', '1', '-', '-', '-', '-'},
                                   {'-', '-', '-', '-', '-', '-', '1', '-', '-', '-', '-'},
                                   {'-', '1', '1', '1', '1', '1', '1', '1', '1', '0', '0'},
                                   {'-', '-', '-', '-', '-', '-', '1', '-', '-', '-', '-'},
                                   {'-', '-', '-', '-', '-', '-', '1', '-', '-', '-', '-'},
                                   {'-', '-', '-', '-', '-', '-', '1', '-', '-', '-', '-'},
                                   {'-', '-', '-', '-', '-', '-', '1', '-', '-', '-', '-'},
                                   {'-', '-', '-', '-', '-', '-', '1', '-', '-', '-', '-'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult);

    }

    @Test
    @DisplayName("Test the 8. evil Map")
    void TestEvilMap8() {

        Game game = createGame("maps/evilMaps/boeseMap07.map");
        boolean moveIsValid = game.getBoard().executeMove(10 , 2, 2);

        assertTrue(moveIsValid);

        char[][] expectedResult = {{'-', '-', '-', '-', '-', '-', '1', '-', '-', '-', '-'},
                                   {'-', '-', '-', '-', '-', '-', '2', '-', '-', '-', '-'},
                                   {'-', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2'},
                                   {'-', '-', '-', '-', '-', '-', '2', '-', '-', '-', '-'},
                                   {'-', '-', '-', '-', '-', '-', '2', '-', '-', '-', '-'},
                                   {'-', '-', '-', '-', '-', '-', '2', '-', '-', '-', '-'},
                                   {'-', '-', '-', '-', '-', '-', '2', '-', '-', '-', '-'},
                                   {'-', '-', '-', '-', '-', '-', '2', '-', '-', '-', '-'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult);

    }

    @Test
    @DisplayName("Test the 9 n . evil Map")
    void TestEvilMap9() {

        Game game = createGame("maps/evilMaps/boeseMap07.map");
        boolean moveIsValid = game.getBoard().executeMove(10 , 2, 2);

        assertTrue(moveIsValid);

        char[][] expectedResult = {{'-', '-', '-', '-', '-', '-', '1', '-', '-', '-', '-'},
                {'-', '-', '-', '-', '-', '-', '2', '-', '-', '-', '-'},
                {'-', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2'},
                {'-', '-', '-', '-', '-', '-', '2', '-', '-', '-', '-'},
                {'-', '-', '-', '-', '-', '-', '2', '-', '-', '-', '-'},
                {'-', '-', '-', '-', '-', '-', '2', '-', '-', '-', '-'},
                {'-', '-', '-', '-', '-', '-', '2', '-', '-', '-', '-'},
                {'-', '-', '-', '-', '-', '-', '2', '-', '-', '-', '-'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult);

    }
}
