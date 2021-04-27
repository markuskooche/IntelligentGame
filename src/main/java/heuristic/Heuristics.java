package heuristic;

import map.Board;
import map.Move;
import map.Player;
import mapanalyze.MapAnalyzer;

import java.util.ArrayList;
import java.util.List;

public class Heuristics {

    private Board board;
    private Player[] players;
    private int mapsAnalyzed;

    public Heuristics(Board board, Player[] players) {
        this.board = board;
        this.players = players;
    }

    public Move getMoveParanoid(Player player, int searchDepth) {
        int numPlayers = players.length;
        Board tmpBoardStart = new Board(board.getField(), board.getAllTransitions(), numPlayers, board.getBombRadius());

        List<BoardMove> executedStartMoves = executeAllMoves(player,tmpBoardStart, false);
        if (executedStartMoves.isEmpty()) {
            return onlyOverrideStone(tmpBoardStart, player);
        }
        if (executedStartMoves.size() == 1) {
            return executedStartMoves.get(0).getMove();
        }

        mapsAnalyzed = 0;
        int depth = 1;
        int ourPlayer = player.getNumber() - '0';
        int currPlayer = (ourPlayer % numPlayers) + 1;
        int value = Integer.MIN_VALUE;
        Move move = new Move();
        for (BoardMove boardMove : executedStartMoves) {
            mapsAnalyzed++;
            if (depth != searchDepth) {
                int tmpValue  =  searchParanoid(currPlayer, ourPlayer, boardMove.getBoard(), depth, searchDepth);
                if (tmpValue >= value) {
                    value = tmpValue;
                    move = boardMove.getMove();
                }
                depth = 1;
            } else {
                int tmpValue = getEvaluationForPlayer(player, boardMove.getBoard(), boardMove.getMove());
                if (tmpValue > value) {
                    value = tmpValue;
                    move = boardMove.getMove();
                }
            }
        }
        System.out.println("Analyzed Maps: " + mapsAnalyzed);
        System.out.println("XT01-98-AM-" + mapsAnalyzed);
        System.out.println("OUR PICKED MOVE (PARANOID): " + move);
        return move;
    }

    private Move onlyOverrideStone(Board board, Player player) {
        List<BoardMove> executedStartMoves = executeAllMoves(player,board, true);
        int value = Integer.MIN_VALUE;
        Move move = new Move();
        for (BoardMove boardMove : executedStartMoves) {
            mapsAnalyzed++;
            int tmpValue = getEvaluationForPlayer(player, boardMove.getBoard(), boardMove.getMove());
            if (tmpValue >= value) {
                value = tmpValue;
                move = boardMove.getMove();
            }
        }
        System.out.println("POSSIBLE MOVES (OVERRIDE): " + executedStartMoves.size());
        System.out.println("OUR PICKED MOVE (OVERRIDE): " + move);
        return move;
    }

    private int searchParanoid(int currPlayer, int ourPlayerNum, Board board, int depth, int maxDepth) {
        int numPlayers = players.length;
        Player player = players[currPlayer - 1];
        Player ourPlayer = players[ourPlayerNum - 1];
        int value = 0;
        depth++;

        List<BoardMove> executedMoves = executeAllMoves(player, board, false);
        List<Integer> results = new ArrayList<>();
        int tmpValue = 0;
        if (depth != maxDepth) {
            int nextPlayer = (currPlayer % numPlayers) + 1;
            for (BoardMove boardMove : executedMoves) {
                mapsAnalyzed++;
                tmpValue = searchParanoid(nextPlayer, ourPlayerNum, boardMove.getBoard(), depth, maxDepth);
                results.add(tmpValue);
            }
        } else {
            for (BoardMove boardMove : executedMoves) {
                mapsAnalyzed++;
                tmpValue = getEvaluationForPlayer(ourPlayer, boardMove.getBoard(), boardMove.getMove());
                results.add(tmpValue);
            }
        }

        if (player == ourPlayer) {
            if (results.isEmpty()) {
                value = Integer.MIN_VALUE;
            } else {
                value = results.stream().max(Integer::compareTo).get();
            }
        } else {
            if (results.isEmpty()) {
                value = Integer.MAX_VALUE;
                //value = 0;
            } else {
                value = results.stream().min(Integer::compareTo).get();
            }
        }
        return value;
    }

    private List<BoardMove> executeAllMoves(Player player, Board board, boolean overrideMoves) {
        List<Move> myMoves = board.getLegalMoves(player, overrideMoves);
        //System.out.println("ALL POSSIBLE MOVES: " + myMoves.size());
        List<BoardMove> executedMoves = new ArrayList<>();
        for (Move m : myMoves) {
            Board newBoard = new Board(board);
            int x = m.getX();
            int y = m.getY();
            int additionalInformation = getAdditionalInfo(m, player);
            newBoard.executeMove(x, y, player, additionalInformation, overrideMoves);
            executedMoves.add(new BoardMove(newBoard, m, player));
        }
        //System.out.println("RETURNED POSSIBLE MOVES: " + executedMoves.size());
        return executedMoves;
    }

    /**
     * Special field decision
     */
    private int getAdditionalInfo(Move move, Player player) {
        int info = 0;
        if (move.isBonus()) info = 21; // we take allways a OverrideStone
        if (move.isChoice()) info = player.getNumber(); //Current: never Change our Colour
        return info;
    }

    public int getEvaluationForPlayer(Player player, Board board, Move move) {
        int mapValue = getMapValue(player, board);
        int coinParity = getCoinParity(player, board);
        int mobility = getMobility(player, board);
        int specialField = getSpecialFieldValue(move);
//        System.out.println("map.Player " + player.getNumber() + " |MapValue: " + mapValue + " CoinParity: " + coinParity + " Mobility: " + mobility);
        return mapValue + coinParity + mobility + specialField;
    }

    public int getEvaluationForPlayerStatistic(Player player, Board board) {
        int mapValue = getMapValue(player, board);
        int coinParity = getCoinParity(player, board);
        int mobility = getMobility(player, board);
//        System.out.println("map.Player " + player.getNumber() + " |MapValue: " + mapValue + " CoinParity: " + coinParity + " Mobility: " + mobility);
        return mapValue + coinParity + mobility;
    }

    public int getSpecialFieldValue(Move move) {
        int value = 0;
        if (move.isBonus()) {
            value += 70;
        }
        if (move.isChoice()) {
            value += 70;
        }
        return value;
    }

    public int getMapValue(Player player, Board board) {
        MapAnalyzer mAnalyze = new MapAnalyzer(board);
        return mAnalyze.calculateScoreForPlayer(player.getNumber());
    }

    public int getCoinParity(Player player, Board board) {
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

    public int getMobility(Player player, Board board) {
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
