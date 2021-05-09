package controller;

import loganalyze.additional.AnalyzeParser;
import map.Player;
import server.MapParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Start {

    private static Game createGame(String filename){
        Game game = null;
        Path path = Paths.get(filename);

        try {
            byte[] bytes = Files.readAllBytes(path);
            List<String> file = MapParser.createMap(bytes);
            AnalyzeParser analyzeParser =  new AnalyzeParser(1,true, false);
            game = new Game(file, analyzeParser);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return game;
    }

    public static void main(String[] args) {
        Game game = createGame("maps/fancyMaps/qr.map");
        System.out.println(game.toString());

        Player player = game.getPlayer(1);

        System.out.println(player);
        System.out.println(game.getMapValue(1));
        game.getBoard().executeMoveManually(player, true);
        System.out.println(player);
    }
}
