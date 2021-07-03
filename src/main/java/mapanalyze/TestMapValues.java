package mapanalyze;

import controller.Game;
import map.Player;
import loganalyze.additional.AnalyzeParser;
import map.Move;
import map.Player;
import map.Transition;
import server.MapParser;
import server.ServerConnection;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLOutput;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class TestMapValues {

    private static Game createGame(String filename) {
        Game game = null;
        Path path = Paths.get(filename);

        try {
            byte[] bytes = Files.readAllBytes(path);
            List<String> file = MapParser.createMap(bytes);
            AnalyzeParser analyzeParser =  new AnalyzeParser(1,false, true);
            game = new Game(file, analyzeParser);
            game.initializeReachableField();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return game;
    }



    public static void main(String[] args){

        for(int i  = 1; i <= 11; i++){
            Game game = createGame("maps/testMaps/testMapValues/map"+ i + ".map");
            System.out.println("This is the Map" + i);
            System.out.println("Player Amount: " + game.getPlayers().length);
            System.out.println("Transition Count: " + game.getTransitions().length);
            System.out.println("Game Map:");
            System.out.println(game.getBoard().toString());
            System.out.println("MapValues for Current Game: ");
            System.out.println(game.getMapAnalyzer().getBoardValues(true));
        }



    }

}
