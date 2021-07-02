package heuristic;

import map.Board;
import map.Move;
import map.Player;
import mapanalyze.MapAnalyzer;

import java.util.ArrayList;
import java.util.List;

public class PlayerEvaluation {

    private static final int MULTIPLIER = 100000;
    private static final double INVERSION_TOLERANCE = 0.95;

    private MapAnalyzer mapAnalyzer;
    private Player[] players;

    public PlayerEvaluation(MapAnalyzer mapAnalyzer, Player[] players) {
        this.mapAnalyzer = mapAnalyzer;
        this.players = players;
    }

    public boolean takeInversion(Player ourPlayer, Board board) {
        boolean take = false;
        int nextPlayerNum = ourPlayer.getIntNumber() % players.length + 1;
        Player nextPlayer = players[nextPlayerNum - 1];

        int valueNext = getEvaluationForPlayer(nextPlayer, board);
        int valueOur = getEvaluationForPlayer(ourPlayer, board);

        if (valueNext >= valueOur * INVERSION_TOLERANCE) take = true;
        return take;
    }

    public int getEvaluationForPlayer(Player player, Board board) {
        int mapValue = getMapValue(player, board);
        int coinParity = getCoinParity(player, board);
        int mobility = getMobility(player, board);
        return mapValue + coinParity + mobility;
    }

    public int getMapValue(Player player, Board tmpBoard) {
        return mapAnalyzer.calculateScoreForPlayers(player, tmpBoard, players, MULTIPLIER);
    }

    public int getCoinParity(Player player, Board board) {

        int height = board.getHeight();
        int width = board.getWidth();

        List<int[]> myStones = new ArrayList<>();
        List<int[]> playerStones = new ArrayList<>();
        String playersNum = "";
        for(Player p : players) {
            playersNum += p.getCharNumber();
        }

        char field[][] = board.getField();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (field[y][x] == player.getCharNumber()) {
                    myStones.add(new int[] {x, y});
                }
                if (playersNum.indexOf(field[y][x]) != -1) {
                    playerStones.add(new int[] {x, y});
                }
            }
        }

        double allStones = playerStones.size();
        double myStonesAmount = myStones.size();
        double result = myStonesAmount / allStones;
        return (int) (result * MULTIPLIER*0.5);
    }

    public int getMobility(Player player, Board board) {
        List<Move> myMoves = new ArrayList<>();
        List<List<Move>> playerMoves = new ArrayList<>();
        for (Player p : players) {
            List<Move> moves = board.getLegalMoves(p, false);
            if (player.getCharNumber() == p.getCharNumber()) myMoves = moves;
            playerMoves.add(moves);
        }

        double allMoves = 0;
        for (List<Move> move : playerMoves) {
            allMoves += move.size();
        }

        double myMovesAmount = myMoves.size();
        double result = myMovesAmount / allMoves;

        return (int) (result * MULTIPLIER *0.5);
    }

    public int getBestPlayer(int ourPlayer, Board board) {
        int bestPlayer = ourPlayer;
        int bestEvaluation = Integer.MIN_VALUE;

        for (Player player : players) {
            if (!player.isDisqualified()) {
                int evaluation = getEvaluationForPlayer(player, board);

                if (evaluation > bestEvaluation) {
                    bestEvaluation = evaluation;
                    bestPlayer = player.getIntNumber();
                }
            }
        }

        return bestPlayer;
    }
}
