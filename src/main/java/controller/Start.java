package controller;

import loganalyze.additional.AnalyzeParser;
import map.Move;
import map.Player;
import server.MapParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class Start {

    private static final boolean OVERRIDE = true;
    private static final int PLAYER_NUMBER = 2;

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

    private static int[] selectMove (Game game, Player player) {
        int height = game.getBoard().getHeight();
        int width = game.getBoard().getWidth();

        int a = 0;
        int x, y;

        do {
            System.out.print("Please select a valid position [x, y]: ");
            Scanner scanner = new Scanner(System.in);

            x = scanner.nextInt();
            y = scanner.nextInt();

        } while((x < 0 || x >= width) || (y < 0 && x >= height));

        List<Move> legalMoves = game.getBoard().getLegalMoves(player, OVERRIDE);
        for (Move legalMove : legalMoves) {
            if (legalMove.isMove(new int[] {x, y})) {
                if (legalMove.isBonus()) {
                    do {
                        System.out.print("Please select a valid bonus [b = 20, o = 21]: ");
                        Scanner scanner = new Scanner(System.in);

                        a = scanner.nextInt();

                    } while(a < 20 || a > 21);
                } else if (legalMove.isChoice()) {
                    int playerAmount = game.getPlayers().length;
                    do {
                        System.out.print("Please select a valid player [1 - " + playerAmount + "]: ");
                        Scanner scanner = new Scanner(System.in);

                        a = scanner.nextInt() - 1;

                    } while(a < 0 || a >= playerAmount);
                }
            }
        }

        game.executeMove(x, y, PLAYER_NUMBER, a);
        System.out.println(game.getBoard());
        return new int[] {x, y, a};
    }

    private static int[] selectBombMove (Game game) {
        int height = game.getBoard().getHeight();
        int width = game.getBoard().getWidth();

        int a = 0;
        int x, y;
        char piece;

        do {
            System.out.print("Please select a valid position [x, y]: ");
            Scanner scanner = new Scanner(System.in);

            x = scanner.nextInt();
            y = scanner.nextInt();

            piece = game.getBoard().getPiece(x, y);

        } while((x < 0 || x >= width) || (y < 0 && x >= height) || piece == '#');

        game.executeBomb(x, y);
        return new int[] {x, y, a};
    }

    private static void printLegalMoves(Game game, Player player) {
        List<Move> legalMoves = game.getBoard().getLegalMoves(player, OVERRIDE);

        for (Move legalMove : legalMoves) {
            System.out.println(legalMove);
        }
        System.out.println();
    }

    public static void main(String[] args) {
        Game game = createGame("maps/game121.map");
        System.out.println(game.toString());

        Player player = game.getPlayer(PLAYER_NUMBER);

        // EXECUTE NORMAL MOVE
        printLegalMoves(game, player);
        selectMove(game, player);

        // EXECUTE BOMB MOVE
        // selectBombMove(game);
        // System.out.println(game.getBoard());
    }
}
