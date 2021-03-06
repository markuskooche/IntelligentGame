import controller.Game;
import loganalyze.additional.AnalyzeParser;
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

import static org.junit.jupiter.api.Assertions.*;

public class ExecuteMoveTest {


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
    @DisplayName("Check for the detection of false inputs without transactions or overrideStones")
    void checkFalseInputs() {

        // this is the expected output of the map after all moves
        char[][] finalArray = {{'0', '0', '0', '0', '0', '0', '0', '0', '0'},
                               {'0', '0', '0', '0', '1', '2', '0', '0', '0'},
                               {'0', '0', '0', '0', '2', '1', '0', '0', '0'},
                               {'0', '0', '0', '0', '0', '0', '0', '0', '0'},
                               {'0', '0', '0', '0', '0', '0', '0', '0', '0'}};

        Game game = createGame("maps/testMaps/standard/EasyTestMap.map");
        //checks if a move to a Edge is valid
        game.executeMove(8 , 4, 1, 0);
        //check if the board is still the same
        assertArrayEquals(game.getBoard().getField(),finalArray);

        //check for moving to a field, already
        game.executeMove(4 , 1, 1, 0);
        //check if the board is still the same
        assertArrayEquals(game.getBoard().getField(),finalArray);

        //checks whats happens if the wrong player is called
         game.executeMove(5 , 1, 1, 0);
        assertArrayEquals(game.getBoard().getField(),finalArray);

        //check for input that has no stone of another player in between
        game.executeMove(4 , 0, 1, 0);
        assertArrayEquals(game.getBoard().getField(),finalArray);

        //check for input that has no stone of another player in between
        game.executeMove(6 , 3, 1, 0);
        assertArrayEquals(game.getBoard().getField(),finalArray);

    }

    @Test
    @DisplayName("Simulates a few turns of a game to see if the board is updated properly")
    void simulateGame() {

        Game game = createGame("maps/testMaps/standard/EasyTestMap.map");

        //map.Move 1
        game.executeMove(4 , 3, 1, 0);

        char[][] expectedResult1 = {{'0', '0', '0', '0', '0', '0', '0', '0', '0'},
                                    {'0', '0', '0', '0', '1', '2', '0', '0', '0'},
                                    {'0', '0', '0', '0', '1', '1', '0', '0', '0'},
                                    {'0', '0', '0', '0', '1', '0', '0', '0', '0'},
                                    {'0', '0', '0', '0', '0', '0', '0', '0', '0'}};

        Assertions.assertArrayEquals(game.getBoard().getField(),expectedResult1);

        //map.Move 2
        game.executeMove(3 , 3, 2, 0);

        char[][] expectedResult2 = {{'0', '0', '0', '0', '0', '0', '0', '0', '0'},
                                    {'0', '0', '0', '0', '1', '2', '0', '0', '0'},
                                    {'0', '0', '0', '0', '2', '1', '0', '0', '0'},
                                    {'0', '0', '0', '2', '1', '0', '0', '0', '0'},
                                    {'0', '0', '0', '0', '0', '0', '0', '0', '0'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult2);

        //map.Move 3
        game.executeMove(5 , 0, 1, 0);

        char[][] expectedResult3 = {{'0', '0', '0', '0', '0', '1', '0', '0', '0'},
                                    {'0', '0', '0', '0', '1', '1', '0', '0', '0'},
                                    {'0', '0', '0', '0', '2', '1', '0', '0', '0'},
                                    {'0', '0', '0', '2', '1', '0', '0', '0', '0'},
                                    {'0', '0', '0', '0', '0', '0', '0', '0', '0'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult3);

        //map.Move 4
        game.executeMove(6 , 0, 2, 0);

        char[][] expectedResult4 = {{'0', '0', '0', '0', '0', '1', '2', '0', '0'},
                                    {'0', '0', '0', '0', '1', '2', '0', '0', '0'},
                                    {'0', '0', '0', '0', '2', '1', '0', '0', '0'},
                                    {'0', '0', '0', '2', '1', '0', '0', '0', '0'},
                                    {'0', '0', '0', '0', '0', '0', '0', '0', '0'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult4);

        //map.Move 5
        game.executeMove(7 , 0, 1, 0);

        char[][] expectedResult5 = {{'0', '0', '0', '0', '0', '1', '1', '1', '0'},
                                    {'0', '0', '0', '0', '1', '2', '0', '0', '0'},
                                    {'0', '0', '0', '0', '2', '1', '0', '0', '0'},
                                    {'0', '0', '0', '2', '1', '0', '0', '0', '0'},
                                    {'0', '0', '0', '0', '0', '0', '0', '0', '0'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult5);

        //map.Move 6
        game.executeMove(4 , 4, 2, 0);

        char[][] expectedResult6 = {{'0', '0', '0', '0', '0', '1', '1', '1', '0'},
                                    {'0', '0', '0', '0', '1', '2', '0', '0', '0'},
                                    {'0', '0', '0', '0', '2', '1', '0', '0', '0'},
                                    {'0', '0', '0', '2', '2', '0', '0', '0', '0'},
                                    {'0', '0', '0', '0', '2', '0', '0', '0', '0'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult6);

        //map.Move 7
        game.executeMove(2 , 4, 1, 0);

        char[][] expectedResult7 = {{'0', '0', '0', '0', '0', '1', '1', '1', '0'},
                                    {'0', '0', '0', '0', '1', '1', '0', '0', '0'},
                                    {'0', '0', '0', '0', '1', '1', '0', '0', '0'},
                                    {'0', '0', '0', '1', '2', '0', '0', '0', '0'},
                                    {'0', '0', '1', '0', '2', '0', '0', '0', '0'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult7);

        //map.Move 8 This move is invalid because there is a 0 between the stones
        game.executeMove(1 , 4, 2, 0);

        assertArrayEquals(game.getBoard().getField(),expectedResult7);

    }

    @Test
    @DisplayName("Test if the turns over holes are invalid")
    void testTurnWithHolesInBetween() {

        Game game = createGame("maps/testMaps/standard/TestMapForHoles.map");

        char[][] expectedResult = {{'0', '0', '0', '0', '0', '0', '0', '0', '0'},
                                   {'0', '0', '0', '0', '0', '0', '0', '0', '0'},
                                   {'0', '0', '0', '0', '0', '0', '0', '0', '0'},
                                   {'0', '0', '0', '1', '0', '0', '0', '0', '0'},
                                   {'0', '0', '1', '-', '2', '0', '0', '0', '0'}};

        game.executeMove(1 , 4, 2, 0);
        assertArrayEquals(game.getBoard().getField(),expectedResult);
    }

    @Test
    @DisplayName("Test what happens if a player with no overridestones trys to override something")
    void testOverrideWithNoOverridestones() {
        Game game = createGame("maps/testMaps/standard/TestOverrideStonesWithNoStones.map");

        game.executeMove(2 , 2, 1, 0);

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
        Game game = createGame("maps/testMaps/standard/TestOverrideStones.map");

        game.executeMove(2 , 2, 1, 0);

        char[][] expectedResult = {{'0', '0', '0', '0', '0', '0', '0', '0', '0'},
                                   {'0', '0', '0', '0', '0', '0', '0', '0', '0'},
                                   {'0', '0', '2', '1', '0', '0', '0', '0', '0'},
                                   {'0', '0', '0', '0', '0', '0', '0', '0', '0'},
                                   {'0', '0', '0', '0', '0', '0', '0', '0', '0'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult);
    }

    @Test
    @DisplayName("Test for all directions")
    void testAllDirection() {

        Game game = createGame("maps/testMaps/standard/AllDirectionsTestMap.map");

        game.executeMove(3 , 2, 2, 0);

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

        Game game = createGame("maps/testMaps/standard/TestOverrideStonesAllDirections.map");

        game.executeMove(2 , 3, 2, 0);

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
        game.executeMove(3 , 0, 1, 0);

        char[][] expectedResult = {{'2', '2', '2', '0', '2', '2', '2', '2', '2', '2'},
                                   {'-', '-', '-', '-', '-', '1', '2', '0', '-', '-'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult);

    }

    @Test
    @DisplayName("Test the second evil Map")
    void TestEvilMap2() {

        Game game = createGame("maps/evilMaps/boeseMap02.map");
        game.executeMove(4 , 0, 1, 0);

        char[][] expectedResult = {{'1', '1', '1', '1', '1', '1', '1', '1', '1', '1'},
                                   {'-', '-', '-', '-', '0', '1', '2', '0', '-', '-'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult);

    }

    @Test
    @DisplayName("Test the 3th evil Map")
    void TestEvilMap3() {

        Game game = createGame("maps/evilMaps/boeseMap03.map");
        game.executeMove(2 , 0, 1, 0);

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
        game.executeMove(0 , 2, 1, 0);


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
        game.executeMove(0 , 2, 1, 0);

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
        game.executeMove(4 , 2, 1, 0);

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
        game.executeMove(6 , 0, 1, 0);

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
        Game game = createGame("maps/evilMaps/boeseMap08.map");
        game.executeMove(6 , 2, 1, 0);

        char[][] expectedResult = {{'-', '-', '-', '-', '-', '-', '1', '-', '-', '-', '-'},
                                   {'-', '-', '-', '-', '-', '-', '1', '-', '-', '-', '-'},
                                   {'-', '2', '2', '2', '2', '2', '1', '1', '1', '1', '0'},
                                   {'-', '-', '-', '-', '-', '-', '2', '-', '-', '-', '-'},
                                   {'-', '-', '-', '-', '-', '-', '2', '-', '-', '-', '-'},
                                   {'-', '-', '-', '-', '-', '-', '2', '-', '-', '-', '-'},
                                   {'-', '-', '-', '-', '-', '-', '2', '-', '-', '-', '-'},
                                   {'-', '-', '-', '-', '-', '-', '2', '-', '-', '-', '-'}};

        System.out.println(game.getBoard());
        assertArrayEquals(game.getBoard().getField(),expectedResult);

    }

    @Test
    @DisplayName("Test the 8th evil Map (override themself)")
    void TestEvilMap8OverrideThemself() {
        Game game = createGame("maps/evilMaps/boeseMap08.map");
        game.executeMove(6 , 0, 1, 0);

        char[][] expectedResult = {{'-', '-', '-', '-', '-', '-', '1', '-', '-', '-', '-'},
                {'-', '-', '-', '-', '-', '-', '1', '-', '-', '-', '-'},
                {'-', '1', '1', '1', '1', '1', '1', '1', '1', '1', '0'},
                {'-', '-', '-', '-', '-', '-', '1', '-', '-', '-', '-'},
                {'-', '-', '-', '-', '-', '-', '1', '-', '-', '-', '-'},
                {'-', '-', '-', '-', '-', '-', '1', '-', '-', '-', '-'},
                {'-', '-', '-', '-', '-', '-', '1', '-', '-', '-', '-'},
                {'-', '-', '-', '-', '-', '-', '1', '-', '-', '-', '-'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult);

    }

    @Test
    @DisplayName("Test the 9th evil Map")
    void TestEvilMap9() {

        Game game = createGame("maps/evilMaps/boeseMap09.map");
        game.executeMove(4 , 4, 1, 2);

        //choice take stones of player 2 makes no sense but do it anyways

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
        game.executeMove(4 , 4, 1, 0);

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
        game.executeMove(3 , 2, 1, 0);

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
        game.executeMove(2 , 2, 1, 0);

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
        game.executeMove(0 , 2, 1, 0);

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
        game.executeMove(5 , 5, 1, 0);

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
        game.executeMove(5 , 5, 1, 0);

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
        game.executeMove(5 , 5, 1, 0);

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
        game.executeMove(5 , 5, 1, 0);

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
        game.executeMove(5 , 5, 1, 0);

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
        game.executeMove(5 , 5, 1, 0);

        char[][] expectedResult = {
                {'1', '-', '-', '-', '-', '1', '-', '-', '-', '-', '1'},
                {'-', '1', '-', '-', '-', '1', '-', '-', '-', '1', '-'},
                {'-', '-', '-', '-', '-', '1', '-', '-', '-', '-', '-'},
                {'-', '-', '-', '-', '-', '1', '-', '-', '-', '-', '-'},
                {'-', '-', '-', '-', '-', '1', '-', '-', '-', '-', '-'},
                {'1', '1', '1', '1', '1', '1', '1', '1', '1', '1', '1'},
                {'-', '-', '-', '-', '-', '1', '-', '-', '-', '-', '-'},
                {'-', '-', '-', '-', '-', '1', '-', '-', '-', '-', '-'},
                {'-', '-', '-', '-', '-', '1', '-', '-', '-', '-', '-'},
                {'-', '1', '-', '-', '-', '1', '-', '-', '-', '1', '-'},
                {'1', '-', '-', '-', '-', '1', '-', '-', '-', '-', '1'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult);

    }

    @Test
    @DisplayName("Test bonus selection (bomb)")
    void TestBonusMapBomb() {

        Game game = createGame("maps/testMaps/standard/BonusMap.map");
        game.executeMove(1 , 3, 1, 20);

        char[][] expectedResult = {
                {'0', '0', '0', '0'},
                {'0', '1', '2', '0'},
                {'0', '1', '1', '0'},
                {'0', '1', '0', '0'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult);

        Player player = game.getPlayer(1);
        int bombAmount = player.getBomb();
        int overrideAmount = player.getOverrideStone();

        assertEquals(bombAmount, 1);
        assertEquals(overrideAmount ,0);
    }

    @Test
    @DisplayName("Test bonus selection (override)")
    void TestBonusMapOverride() {

        Game game = createGame("maps/testMaps/standard/BonusMap.map");
        game.executeMove(1 , 3, 1, 21);

        char[][] expectedResult = {
                {'0', '0', '0', '0'},
                {'0', '1', '2', '0'},
                {'0', '1', '1', '0'},
                {'0', '1', '0', '0'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult);

        Player player = game.getPlayer(1);
        int bombAmount = player.getBomb();
        int overrideAmount = player.getOverrideStone();

        assertEquals(bombAmount, 0);
        assertEquals(overrideAmount ,1);
    }

    @Test
    @DisplayName("Test expansion move")
    void TestExpansionMove() {

        Game game = createGame("maps/testMaps/standard/ExpansionMove.map");
        game.executeMove(8 , 4, 1, 0);

        char[][] expectedResult1 = {
                {'0', '0', '0', '0', '0', '0', '0', '0', 'x'},
                {'0', '1', '2', '0', '0', '0', '0', '0', '0'},
                {'0', '2', '1', '0', '0', '0', '0', '0', '0'},
                {'0', '0', '0', '0', '0', '0', '0', '0', '0'},
                {'0', '0', '0', '0', '0', '1', '1', '1', '1'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult1);

        game.executeMove(8 , 0, 1, 0);

        char[][] expectedResult2 = {
                {'0', '0', '0', '0', '0', '0', '0', '0', '1'},
                {'0', '1', '2', '0', '0', '0', '0', '0', '0'},
                {'0', '2', '1', '0', '0', '0', '0', '0', '0'},
                {'0', '0', '0', '0', '0', '0', '0', '0', '0'},
                {'0', '0', '0', '0', '0', '1', '1', '1', '1'}};

        assertArrayEquals(game.getBoard().getField(),expectedResult2);
    }
}
