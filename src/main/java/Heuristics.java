import java.util.ArrayList;
import java.util.List;

public class Heuristics {

    private char [] players = {'1' , '2', '3', '4', '5', '6' , '7' , '8'};
    private Board board;
    private MapAnalyzer mapAnalyzer;
    public Heuristics(Board board, MapAnalyzer mapAnalyzer) {
        this.board = board;
        this.mapAnalyzer = mapAnalyzer;
    }

    public int getEvaluationForPlayer(Player player) {
        int mapValue = mapAnalyzer.calculateScoreForPlayer(player.getNumber());
        int coinParity = getCoinParity(player);
        int mobility = getMobility(player);
        return mapValue + coinParity + mobility;
    }

    public int getCoinParity(Player player) {
        List<int[]> myStones = new ArrayList<>();
        List<List<int[]>> playerStones = new ArrayList<>();
        for (char p : players) {
            List<int[]> stones = board.getStonesOfPlayer(p);
            if (player.getNumber() == p) myStones = stones;
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
        List<Moves> myMoves = new ArrayList<>();
        List<List<Moves>> playerMoves = new ArrayList<>();
        for (char p : players) {
            List<Moves> moves = board.getLegalMoves(p);
            if (player.getNumber() == p) myMoves = moves;
            playerMoves.add(moves);
        }

        double allMoves = 0;
        for (List<Moves> move : playerMoves) {
            allMoves += move.size();
        }

        double myMovesAmount = myMoves.size();
        double result = myMovesAmount / allMoves * 100;

        return (int) result;
    }
}
