package heuristic;

import map.Board;
import map.Move;
import map.Player;
import mapanalyze.MapAnalyzer;

import java.util.ArrayList;
import java.util.List;

public class Heuristics {

//    private char [] players = {'1' , '2', '3', '4', '5', '6' , '7' , '8'};
    private Board board;
    private MapAnalyzer mapAnalyzer;
    private Player[] players;
    public Heuristics(Board board, Player[] players, MapAnalyzer mapAnalyzer) {
        this.board = board;
        this.mapAnalyzer = mapAnalyzer;
        this.players = players;
    }

    public int getEvaluationForPlayer(Player player) {
        int mapValue = mapAnalyzer.calculateScoreForPlayer(player.getNumber());
        int coinParity = getCoinParity(player);
        int mobility = getMobility(player);
        System.out.println("map.Player " + player.getNumber() + " |MapValue: " + mapValue + " CoinParity: " + coinParity + " Mobility: " + mobility);
        return mapValue + coinParity + mobility;
    }

    public int getCoinParity(Player player) {
        List<int[]> myStones = new ArrayList<>();
        List<List<int[]>> playerStones = new ArrayList<>();
        for (Player p : players) {
            List<int[]> stones = board.getPlayerPositions(p.getNumber());
            if (player.getNumber() == p.getNumber()) myStones = stones;
            playerStones.add(stones);
        }

        double allStones = 0;
        for (List<int[]> stones : playerStones) {
            allStones += stones.size();
        }

        double myStonesAmount = myStones.size();

        double result = myStonesAmount / allStones * 100;

        return (int) result;
    }

    public int getMobility(Player player) {
        List<Move> myMoves = new ArrayList<>();
        List<List<Move>> playerMoves = new ArrayList<>();
        for (Player p : players) {
            List<Move> moves = board.getLegalMoves(p, false);
            if (player.getNumber() == p.getNumber()) myMoves = moves;
            playerMoves.add(moves);
        }

        double allMoves = 0;
        for (List<Move> move : playerMoves) {
            allMoves += move.size();
        }

        double myMovesAmount = myMoves.size();
        double result = myMovesAmount / allMoves * 100;

        return (int) result;
    }
}
