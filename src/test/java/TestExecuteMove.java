import org.junit.jupiter.api.Assertions;
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
        game.getBoard().executeMove(8 , 4, '1');
        //check if the board is still the same
        assertArrayEquals(game.getBoard().getField(),finalArray);

        //check for moving to a field, already
        game.getBoard().executeMove(4 , 1, '1');
        //check if the board is still the same
        assertArrayEquals(game.getBoard().getField(),finalArray);

        //checks whats happens if the wrong player is called
         game.getBoard().executeMove(5 , 1, '1');
        assertArrayEquals(game.getBoard().getField(),finalArray);

        //check for input that has no stone of another player in between
        game.getBoard().executeMove(4 , 0, '1');
        assertArrayEquals(game.getBoard().getField(),finalArray);

        //check for input that has no stone of another player in between
        game.getBoard().executeMove(6 , 3, '1');
        assertArrayEquals(game.getBoard().getField(),finalArray);

    }

    @Test
    @DisplayName("Simulates a few turns of a game to see if the board is updated properly")
    void simulateGame() {

        Game game = createGame("maps/benesTestMaps/EasyTestMap.map");

        //Move 1
        game.getBoard().executeMove(4 , 3, '1');

        char[][] expectedResult1 = {{'0', '0', '0', '0', '0', '0', '0', '0', '0'},
                                    {'0', '0', '0', '0', '1', '2', '0', '0', '0'},
                                    {'0', '0', '0', '0', '1', '1', '0', '0', '0'},
                                    {'0', '0', '0', '0', '1', '0', '0', '0', '0'},
                                    {'0', '0', '0', '0', '0', '0', '0', '0', '0'}};

        Assertions.assertArrayEquals(game.getBoard().getField(),expectedResult1);

        //Move 2
        game.getBoard().executeMove(3 , 3, '2');

        char[][] expectedResult2 = {{'0', '0', '0', '0', '0', '0', '0', '0', '0'},
                                    {'0', '0', '0', '0', '1', '2', '0', '0', '0'},
                                    {'0', '0', '0', '0', '2', '1', '0', '0', '0'},
                                    {'0', '0', '0', '2', '1', '0', '0', '0', '0'},
                                    {'0', '0', '0', '0', '0', '0', '0', '0', '0'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult2);

        //Move 3
        game.getBoard().executeMove(5 , 0, '1');

        char[][] expectedResult3 = {{'0', '0', '0', '0', '0', '1', '0', '0', '0'},
                                    {'0', '0', '0', '0', '1', '1', '0', '0', '0'},
                                    {'0', '0', '0', '0', '2', '1', '0', '0', '0'},
                                    {'0', '0', '0', '2', '1', '0', '0', '0', '0'},
                                    {'0', '0', '0', '0', '0', '0', '0', '0', '0'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult3);

        //Move 4
        game.getBoard().executeMove(6 , 0, '2');

        char[][] expectedResult4 = {{'0', '0', '0', '0', '0', '1', '2', '0', '0'},
                                    {'0', '0', '0', '0', '1', '2', '0', '0', '0'},
                                    {'0', '0', '0', '0', '2', '1', '0', '0', '0'},
                                    {'0', '0', '0', '2', '1', '0', '0', '0', '0'},
                                    {'0', '0', '0', '0', '0', '0', '0', '0', '0'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult4);

        //Move 5
        game.getBoard().executeMove(7 , 0, '1');

        char[][] expectedResult5 = {{'0', '0', '0', '0', '0', '1', '1', '1', '0'},
                                    {'0', '0', '0', '0', '1', '2', '0', '0', '0'},
                                    {'0', '0', '0', '0', '2', '1', '0', '0', '0'},
                                    {'0', '0', '0', '2', '1', '0', '0', '0', '0'},
                                    {'0', '0', '0', '0', '0', '0', '0', '0', '0'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult5);

        //Move 6
        game.getBoard().executeMove(4 , 4, '2');

        char[][] expectedResult6 = {{'0', '0', '0', '0', '0', '1', '1', '1', '0'},
                                    {'0', '0', '0', '0', '1', '2', '0', '0', '0'},
                                    {'0', '0', '0', '0', '2', '1', '0', '0', '0'},
                                    {'0', '0', '0', '2', '2', '0', '0', '0', '0'},
                                    {'0', '0', '0', '0', '2', '0', '0', '0', '0'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult6);

        //Move 7
        game.getBoard().executeMove(2 , 4, '1');

        char[][] expectedResult7 = {{'0', '0', '0', '0', '0', '1', '1', '1', '0'},
                                    {'0', '0', '0', '0', '1', '1', '0', '0', '0'},
                                    {'0', '0', '0', '0', '1', '1', '0', '0', '0'},
                                    {'0', '0', '0', '1', '2', '0', '0', '0', '0'},
                                    {'0', '0', '1', '0', '2', '0', '0', '0', '0'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult7);

        //Move 8 This move is invalid because there is a 0 between the stones
        game.getBoard().executeMove(1 , 4, '2');

        assertArrayEquals(game.getBoard().getField(),expectedResult7);

    }

    @Test
    @DisplayName("Test if the turns over holes are invalid")
    void testTurnWithHolesInBetween() {

        Game game = createGame("maps/benesTestMaps/TestMapForHoles.map");

        char[][] expectedResult = {{'0', '0', '0', '0', '0', '0', '0', '0', '0'},
                                   {'0', '0', '0', '0', '0', '0', '0', '0', '0'},
                                   {'0', '0', '0', '0', '0', '0', '0', '0', '0'},
                                   {'0', '0', '0', '1', '0', '0', '0', '0', '0'},
                                   {'0', '0', '1', '-', '2', '0', '0', '0', '0'}};

        game.getBoard().executeMove(1 , 4, '2');
        assertArrayEquals(game.getBoard().getField(),expectedResult);
    }

    @Test
    @DisplayName("Test what happens if a player with no overridestones trys to override something")
    void testOverrideWithNoOverridestones() {
        Game game = createGame("maps/benesTestMaps/TestOverrideStonesWithNoStones.map");

        game.getBoard().executeMove(2 , 2, '1');

        char[][] expectedResult = {{'0', '0', '0', '0', '0', '0', '0', '0', '0'},
                                   {'0', '0', '0', '0', '0', '0', '0', '0', '0'},
                                   {'0', '0', '2', '1', '0', '0', '0', '0', '0'},
                                   {'0', '0', '0', '0', '0', '0', '0', '0', '0'},
                                   {'0', '0', '0', '0', '0', '0', '0', '0', '0'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult);
    }

    @Test
    @DisplayName("Test what happens if a player with no overridestones trys to override something")
    void testOverride() {
        Game game = createGame("maps/benesTestMaps/TestOverrideStones.map");

        game.getBoard().executeMove(2 , 2, '1');

        char[][] expectedResult = {{'0', '0', '0', '0', '0', '0', '0', '0', '0'},
                                   {'0', '0', '0', '0', '0', '0', '0', '0', '0'},
                                   {'0', '0', '1', '1', '0', '0', '0', '0', '0'},
                                   {'0', '0', '0', '0', '0', '0', '0', '0', '0'},
                                   {'0', '0', '0', '0', '0', '0', '0', '0', '0'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult);
    }

    @Test
    @DisplayName("Test for all directions")
    void testAllDirection() {

        Game game = createGame("maps/benesTestMaps/AllDirectionsTestMap.map");

        game.getBoard().executeMove(3 , 2, '2');

        char[][] expectedResult = {{'0', '2', '2', '2', '2', '2', '0', '0', '0'},
                                   {'0', '2', '2', '2', '2', '2', '0', '0', '0'},
                                   {'0', '2', '2', '2', '2', '2', '0', '0', '0'},
                                   {'0', '2', '2', '2', '2', '2', '0', '0', '0'},
                                   {'0', '2', '2', '2', '2', '2', '0', '0', '0'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult);

    }

    @Test
    @DisplayName("Test for all directions while using an Override stone")
    void testAllDirectionsWithOverrideStones() {

        Game game = createGame("maps/benesTestMaps/TestOverrideStonesAllDirections.map");

        game.getBoard().executeMove(2 , 3, '2');

        char[][] expectedResult = {{'0', '2', '2', '2', '2', '2', '0', '0', '0'},
                                   {'0', '2', '2', '1', '2', '2', '0', '0', '0'},
                                   {'0', '2', '2', '2', '1', '2', '0', '0', '0'},
                                   {'0', '2', '2', '2', '2', '2', '0', '0', '0'},
                                   {'0', '2', '2', '2', '2', '2', '0', '0', '0'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult);
    }

    @Test
    @DisplayName("Test the first evil Map")
    void TestEvilMap1() {

        Game game = createGame("maps/evilMaps/boeseMap01.map");
        System.out.println(game.toString());
        game.getBoard().executeMove(3 , 0, '1');

        char[][] expectedResult = {{'2', '2', '2', '0', '2', '2', '2', '2', '2', '2'},
                                   {'-', '-', '-', '-', '-', '1', '2', '0', '-', '-'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult);

    }

    @Test
    @DisplayName("Test the second evil Map")
    void TestEvilMap2() {

        Game game = createGame("maps/evilMaps/boeseMap02.map");
        game.getBoard().executeMove(4 , 0, '1');

        char[][] expectedResult = {{'1', '1', '1', '1', '1', '1', '1', '1', '1', '1'},
                                   {'-', '-', '-', '-', '0', '1', '2', '0', '-', '-'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult);

    }

    @Test
    @DisplayName("Test the 3th evil Map")
    void TestEvilMap3() {

        Game game = createGame("maps/evilMaps/boeseMap03.map");
        game.getBoard().executeMove(2 , 0, '1');

        char[][] expectedResult = {{'0', '0', '1'},
                                   {'0', '1', '1'},
                                   {'1', '0', '1'},
                                   {'1', '1', '1'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult);

    }

    @Test
    @DisplayName("Test the 4th evil Map")
    void TestEvilMap4() {

        Game game = createGame("maps/evilMaps/boeseMap04.map");
        game.getBoard().executeMove(0 , 2, '1');


        char[][] expectedResult = {{'1', '1', '1'},
                                   {'0', '1', '1'},
                                   {'1', '0', '1'},
                                   {'1', '1', '1'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult);

    }

    @Test
    @DisplayName("Test the 5th evil Map")
    void TestEvilMap5() {

        Game game = createGame("maps/evilMaps/boeseMap05.map");
        game.getBoard().executeMove(0 , 2, '1');

        char[][] expectedResult = {{'1', '1', '1'},
                                   {'0', '1', '2'},
                                   {'1', '0', '2'},
                                   {'1', '1', '1'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult);

    }

    @Test
    @DisplayName("Test the 6th evil Map")
    void TestEvilMap6() {

        Game game = createGame("maps/evilMaps/boeseMap06.map");
        game.getBoard().executeMove(4 , 2, '1');

        char[][] expectedResult = {{'-', '-', '-', '-', '-', '-', '-', '-', '-'},
                                   {'-', '-', '1', '-', '-', '-', '1', '-', '-'},
                                   {'-', '1', '1', '1', '1', '1', '1', '1', '-'},
                                   {'-', '-', '1', '-', '-', '-', '1', '-', '-'},
                                   {'-', '-', '1', '-', '-', '-', '1', '-', '-'},
                                   {'-', '-', '1', '-', '-', '-', '1', '-', '-'},
                                   {'-', '-', '1', '-', '-', '-', '1', '-', '-'},
                                   {'-', '-', '-', '-', '-', '-', '-', '-', '-'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult);

    }

    @Test
    @DisplayName("Test the 7th evil Map")
    void TestEvilMap7() {

        Game game = createGame("maps/evilMaps/boeseMap07.map");
        game.getBoard().executeMove(6 , 0, '1');

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
    @DisplayName("Test the 8th evil Map")
    void TestEvilMap8() {
        //TODO ADD OverrideStones
        Game game = createGame("maps/evilMaps/boeseMap08.map");
        game.getBoard().executeMove(6 , 2, '1');

        char[][] expectedResult = {{'-', '-', '-', '-', '-', '-', '1', '-', '-', '-', '-'},
                                   {'-', '-', '-', '-', '-', '-', '2', '-', '-', '-', '-'},
                                   {'-', '2', '2', '2', '2', '2', '2', '2', '1', '1', '0'},
                                   {'-', '-', '-', '-', '-', '-', '2', '-', '-', '-', '-'},
                                   {'-', '-', '-', '-', '-', '-', '2', '-', '-', '-', '-'},
                                   {'-', '-', '-', '-', '-', '-', '2', '-', '-', '-', '-'},
                                   {'-', '-', '-', '-', '-', '-', '2', '-', '-', '-', '-'},
                                   {'-', '-', '-', '-', '-', '-', '2', '-', '-', '-', '-'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult);

    }

    @Test
    @DisplayName("Test the 9th evil Map")
    void TestEvilMap9() {

        Game game = createGame("maps/evilMaps/boeseMap09.map");
        game.getBoard().executeMove(4 , 4, '1');

        //TODO choise take stones of player 2 makes no sense but do it anyways

        char[][] expectedResult2 = {{'-', '-', '-', '-', '-', '-', '-', '-'},
                                   {'-', '2', '0', '0', '2', '0', '0', '2'},
                                   {'-', '0', '2', '0', '2', '0', '2', '0'},
                                   {'-', '0', '0', '2', '2', '2', '0', '0'},
                                   {'-', '2', '2', '2', '2', '2', '2', '2'},
                                   {'-', '0', '0', '2', '2', '2', '0', '0'},
                                   {'-', '0', '2', '0', '2', '0', '2', '0'},
                                   {'-', '2', '0', '0', '2', '0', '0', '2'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult2);

    }

    @Test
    @DisplayName("Test the 10th evil Map")
    void TestEvilMap10() {

        Game game = createGame("maps/evilMaps/boeseMap10.map");
        game.getBoard().executeMove(4 , 4, '1');

        char[][] expectedResult = {{'-', '-', '-', '-', '-', '-', '-', '-'},
                                   {'-', '1', '0', '0', '1', '0', '0', '1'},
                                   {'-', '0', '1', '0', '1', '0', '1', '0'},
                                   {'-', '0', '0', '1', '1', '1', '0', '0'},
                                   {'-', '1', '1', '1', '1', '1', '1', '1'},
                                   {'-', '0', '0', '1', '1', '1', '0', '0'},
                                   {'-', '0', '1', '0', '1', '0', '1', '0'},
                                   {'-', '1', '0', '0', '1', '0', '0', '1'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult);

        //TODO invertion player 1 becomes player 2

        char[][] expectedResult2 = {{'-', '-', '-', '-', '-', '-', '-', '-'},
                                    {'-', '2', '0', '0', '2', '0', '0', '2'},
                                    {'-', '0', '2', '0', '2', '0', '2', '0'},
                                    {'-', '0', '0', '2', '2', '2', '0', '0'},
                                    {'-', '2', '2', '2', '2', '2', '2', '2'},
                                    {'-', '0', '0', '2', '2', '2', '0', '0'},
                                    {'-', '0', '2', '0', '2', '0', '2', '0'},
                                    {'-', '2', '0', '0', '2', '0', '0', '2'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult2);

    }

    @Test
    @DisplayName("Test the 11th evil Map")
    void TestEvilMap11() {

        Game game = createGame("maps/evilMaps/boeseMap11.map");
        game.getBoard().executeMove(3 , 2, '1');

        char[][] expectedResult = {{'-', '-', '-', '-'},
                                   {'-', '-', '-', '2'},
                                   {'-', '-', '-', '1'},
                                   {'-', '-', '-', '1'},
                                   {'-', '-', '-', '1'},
                                   {'-', '-', '-', '0'},
                                   {'-', '-', '-', '0'},
                                   {'-', '-', '-', '0'},
                                   {'-', '-', '-', '0'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult);

    }

    @Test
    @DisplayName("Test 1st transitionsMap")
    void TestTransitionsMap1() {

        Game game = createGame("maps/testMaps/transitions/map01.map");
        game.getBoard().executeMove(2 , 2, '1');

        char[][] expectedResult = {{'1', '1', '1', '1', '1'},
                                   {'1', '1', '1', '1', '1'},
                                   {'1', '1', '1', '1', '1'},
                                   {'1', '1', '1', '1', '1'},
                                   {'1', '1', '1', '1', '1'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult);

    }

    @Test
    @DisplayName("Test 2st transitionsMap")
    void TestTransitionsMap2() {

        Game game = createGame("maps/testMaps/transitions/map02.map");
        game.getBoard().executeMove(0 , 2, '1');

        char[][] expectedResult = {{'1', '1', '1', '1'},
                                   {'0', '1', '1', '0'},
                                   {'1', '1', '1', '1'},
                                   {'1', '0', '0', '1'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult);

    }

    @Test
    @DisplayName("Test 3st transitionsMap")
    void TestTransitionsMap3() {

        Game game = createGame("maps/testMaps/transitions/map03.map");
        game.getBoard().executeMove(5 , 5, '1');

        char[][] expectedResult = {{'1', '-', '-', '-', '-', '1', '-', '-', '-', '-', '1'},
                                   {'-', '1', '-', '-', '-', '1', '-', '-', '-', '1', '-'},
                                   {'-', '-', '-', '-', '-', '-', '-', '-', '-', '-', '-'},
                                   {'-', '-', '-', '-', '-', '-', '-', '-', '-', '-', '-'},
                                   {'-', '-', '-', '-', '-', '-', '-', '-', '-', '-', '-'},
                                   {'1', '1', '-', '-', '-', '1', '-', '-', '-', '1', '1'},
                                   {'-', '-', '-', '-', '-', '-', '-', '-', '-', '-', '-'},
                                   {'-', '-', '-', '-', '-', '-', '-', '-', '-', '-', '-'},
                                   {'-', '-', '-', '-', '-', '-', '-', '-', '-', '-', '-'},
                                   {'-', '1', '-', '-', '-', '1', '-', '-', '-', '1', '-'},
                                   {'1', '-', '-', '-', '-', '1', '-', '-', '-', '-', '1'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult);

    }

    @Test
    @DisplayName("Test 4st transitionsMap")
    void TestTransitionsMap4() {

        Game game = createGame("maps/testMaps/transitions/map04.map");
        game.getBoard().executeMove(5 , 5, '1');

        char[][] expectedResult = {{'1', '-', '-', '-', '-', '1', '-', '-', '-', '-', '1'},
                                   {'-', '1', '-', '-', '-', '1', '-', '-', '-', '1', '-'},
                                   {'-', '-', '-', '-', '-', '-', '-', '-', '-', '-', '-'},
                                   {'-', '-', '-', '-', '-', '-', '-', '-', '-', '-', '-'},
                                   {'-', '-', '-', '-', '-', '-', '-', '-', '-', '-', '-'},
                                   {'1', '1', '-', '-', '-', '1', '-', '-', '-', '1', '1'},
                                   {'-', '-', '-', '-', '-', '-', '-', '-', '-', '-', '-'},
                                   {'-', '-', '-', '-', '-', '-', '-', '-', '-', '-', '-'},
                                   {'-', '-', '-', '-', '-', '-', '-', '-', '-', '-', '-'},
                                   {'-', '1', '-', '-', '-', '1', '-', '-', '-', '1', '-'},
                                   {'1', '-', '-', '-', '-', '1', '-', '-', '-', '-', '1'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult);

    }

    @Test
    @DisplayName("Test 5st transitionsMap")
    void TestTransitionsMap5() {

        Game game = createGame("maps/testMaps/transitions/map05.map");
        game.getBoard().executeMove(5 , 5, '1');

        char[][] expectedResult = {{'1', '-', '-', '-', '-', '1', '-', '-', '-', '-', '1'},
                                   {'-', '2', '-', '-', '-', '1', '-', '-', '-', '2', '-'},
                                   {'-', '-', '-', '-', '-', '-', '-', '-', '-', '-', '-'},
                                   {'-', '-', '-', '-', '-', '-', '-', '-', '-', '-', '-'},
                                   {'-', '-', '-', '-', '-', '-', '-', '-', '-', '-', '-'},
                                   {'1', '1', '-', '-', '-', '1', '-', '-', '-', '1', '1'},
                                   {'-', '-', '-', '-', '-', '-', '-', '-', '-', '-', '-'},
                                   {'-', '-', '-', '-', '-', '-', '-', '-', '-', '-', '-'},
                                   {'-', '-', '-', '-', '-', '-', '-', '-', '-', '-', '-'},
                                   {'-', '2', '-', '-', '-', '1', '-', '-', '-', '2', '-'},
                                   {'1', '-', '-', '-', '-', '1', '-', '-', '-', '-', '1'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult);

    }


    @Test
    @DisplayName("Test 6st transitionsMap")
    void TestTransitionsMap6() {

        Game game = createGame("maps/testMaps/transitions/map06.map");
        game.getBoard().executeMove(5 , 5, '1');

        char[][] expectedResult = {{'1', '-', '-', '-', '-', '1', '-', '-', '-', '-', '1'},
                                   {'-', '1', '-', '-', '-', '2', '-', '-', '-', '1', '-'},
                                   {'-', '-', '-', '-', '-', '-', '-', '-', '-', '-', '-'},
                                   {'-', '-', '-', '-', '-', '-', '-', '-', '-', '-', '-'},
                                   {'-', '-', '-', '-', '-', '-', '-', '-', '-', '-', '-'},
                                   {'1', '2', '-', '-', '-', '1', '-', '-', '-', '2', '1'},
                                   {'-', '-', '-', '-', '-', '-', '-', '-', '-', '-', '-'},
                                   {'-', '-', '-', '-', '-', '-', '-', '-', '-', '-', '-'},
                                   {'-', '-', '-', '-', '-', '-', '-', '-', '-', '-', '-'},
                                   {'-', '1', '-', '-', '-', '2', '-', '-', '-', '1', '-'},
                                   {'1', '-', '-', '-', '-', '1', '-', '-', '-', '-', '1'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult);

    }

    @Test
    @DisplayName("Test 7st transitionsMap")
    void TestTransitionsMap7() {

        Game game = createGame("maps/testMaps/transitions/map07.map");
        game.getBoard().executeMove(5 , 5, '1');

        char[][] expectedResult = {{'1', '-', '-', '-', '-', '1', '-', '-', '-', '-', '1'},
                                   {'-', '1', '-', '-', '-', '1', '-', '-', '-', '1', '-'},
                                   {'-', '-', '1', '-', '-', '-', '-', '-', '1', '-', '-'},
                                   {'-', '-', '-', '1', '-', '-', '-', '1', '-', '-', '-'},
                                   {'-', '-', '-', '-', '1', '-', '1', '-', '-', '-', '-'},
                                   {'1', '1', '-', '-', '-', '1', '-', '-', '-', '1', '1'},
                                   {'-', '-', '-', '-', '1', '-', '1', '-', '-', '-', '-'},
                                   {'-', '-', '-', '1', '-', '-', '-', '1', '-', '-', '-'},
                                   {'-', '-', '1', '-', '-', '-', '-', '-', '1', '-', '-'},
                                   {'-', '1', '-', '-', '-', '1', '-', '-', '-', '1', '-'},
                                   {'1', '-', '-', '-', '-', '1', '-', '-', '-', '-', '1'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult);

    }

    @Test
    @DisplayName("Test 8st transitionsMap")
    void TestTransitionsMap8() {

        Game game = createGame("maps/testMaps/transitions/map08.map");
        game.getBoard().executeMove(5 , 5, '1');

        char[][] expectedResult = {{'1', '-', '-', '-', '-', '1', '-', '-', '-', '-', '1'},
                                   {'-', '1', '-', '-', '-', '1', '-', '-', '-', '1', '-'},
                                   {'-', '-', '-', '-', '-', '-', '-', '-', '-', '-', '-'},
                                   {'-', '-', '-', '-', '-', '-', '-', '-', '-', '-', '-'},
                                   {'-', '-', '-', '-', '-', '-', '-', '-', '-', '-', '-'},
                                   {'1', '1', '1', '1', '1', '1', '1', '1', '1', '1', '1'},
                                   {'-', '-', '-', '-', '-', '-', '-', '-', '-', '-', '-'},
                                   {'-', '-', '-', '-', '-', '-', '-', '-', '-', '-', '-'},
                                   {'-', '-', '-', '-', '-', '-', '-', '-', '-', '-', '-'},
                                   {'-', '1', '-', '-', '-', '1', '-', '-', '-', '1', '-'},
                                   {'1', '-', '-', '-', '-', '1', '-', '-', '-', '-', '1'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult);

    }


}
