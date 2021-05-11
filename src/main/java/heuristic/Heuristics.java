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
    private int numPlayers;
    private MapAnalyzer mapAnalyzer;

    public Heuristics(Board board, Player[] players, MapAnalyzer mapAnalyzer) {
        this.board = board;
        this.players = players;
        numPlayers = players.length;
        this.mapAnalyzer = mapAnalyzer;
    }

    public Move getMoveParanoid(Player player, int searchDepth, boolean alphaBeta) {
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
        int maxLoop = 0;
        Move move = new Move();
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;
        for (BoardMove boardMove : executedStartMoves) {
            mapsAnalyzed++;
            if (depth != searchDepth) {
                int tmpValue  =  searchParanoid(currPlayer, ourPlayer, boardMove.getBoard(), depth, searchDepth, maxLoop,
                        alpha, beta, alphaBeta);
                if (tmpValue > value) {
                    value = tmpValue;
                    move = boardMove.getMove();
                    if (alphaBeta) {
                        alpha = tmpValue;
                    }
                }
                depth = 1;
            } else { // Search depth = 1
                int tmpValue = getEvaluationForPlayer(player, boardMove.getBoard(), boardMove.getMove());
                if (tmpValue > value) {
                    value = tmpValue;
                    move = boardMove.getMove();
                }
            }
        }
        //System.out.println("Analyzed Maps: " + mapsAnalyzed);
        System.out.println("XT01-98-AM-" + mapsAnalyzed);
//        System.out.println("OUR PICKED MOVE (PARANOID): " + move);
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
//        System.out.println("POSSIBLE MOVES (OVERRIDE): " + executedStartMoves.size());
//        System.out.println("OUR PICKED MOVE (OVERRIDE): " + move);
        return move;
    }

    private int searchParanoid(int currPlayer, int ourPlayerNum, Board board, int depth, int maxDepth, int maxLoop,
                               int alpha ,int beta, boolean alphaBeta) {
        Player player = players[currPlayer - 1];
        Player ourPlayer = players[ourPlayerNum - 1];
        int value = 0;
        depth++;
        List<BoardMove> executedMoves = executeAllMoves(player, board, false);

        // No moves for given player -> another player should move
        // TODO: überspringe disqualifizierte spieler im suchbaum!!!
        if (executedMoves.isEmpty()) {
            int nextPlayer = (currPlayer % numPlayers) + 1;
            if(maxLoop < numPlayers) {
                maxLoop++;
                return searchParanoid(nextPlayer, ourPlayerNum, board, depth - 1, maxDepth, maxLoop,
                        alpha, beta, alphaBeta);
            } else {
                return 0; //No player has any move (perhaps only override)
            }
        }

        int tmpValue = 0;
        if (depth < maxDepth) {
            int nextPlayer = (currPlayer % numPlayers) + 1;

            if (player == ourPlayer) { //MAX
                value = Integer.MIN_VALUE;
                for (BoardMove boardMove : executedMoves) {
                    mapsAnalyzed++;
                    tmpValue = searchParanoid(nextPlayer, ourPlayerNum, boardMove.getBoard(), depth, maxDepth, maxLoop,
                            alpha, beta, alphaBeta);
                    if (tmpValue > value) value = tmpValue;

                    if (alphaBeta) {
                        if (tmpValue > alpha) alpha = tmpValue;
                        if (tmpValue >= beta) {
                            return value; //Cut off
                        }
                    }
                }
            } else { //MIN
                value = Integer.MAX_VALUE;
                for (BoardMove boardMove : executedMoves) {
                    mapsAnalyzed++;
                    tmpValue = searchParanoid(nextPlayer, ourPlayerNum, boardMove.getBoard(), depth, maxDepth, maxLoop,
                            alpha, beta, alphaBeta);
                    if (tmpValue < value) value = tmpValue;

                    if (alphaBeta) {
                        if (tmpValue < beta) beta = tmpValue;
                        if (tmpValue <= alpha) {
                            return value; // Cut off
                        }
                    }
                }
            }
        } else { // Here is the end of the search tree -> pick value based on MAX / MIN
            // TODO: code verdopplung entfernen (siehe if else)
            if (player == ourPlayer) { //MAX
                value = Integer.MIN_VALUE;
                for (BoardMove boardMove : executedMoves) {
                    mapsAnalyzed++;
                    tmpValue = getEvaluationForPlayer(ourPlayer, boardMove.getBoard(), boardMove.getMove());
                    if (tmpValue > value) value = tmpValue;
                }
            } else { //MIN
                value = Integer.MAX_VALUE;
                for (BoardMove boardMove : executedMoves) {
                    mapsAnalyzed++;
                    tmpValue = getEvaluationForPlayer(ourPlayer, boardMove.getBoard(), boardMove.getMove());
                    if (tmpValue < value) value = tmpValue;
                }
            }
        }
        return value;
    }

    private List<BoardMove> executeAllMoves(Player player, Board board, boolean overrideMoves) {
        List<Move> myMoves = board.getLegalMoves(player, overrideMoves);
//        System.out.println("ALL POSSIBLE MOVES: " + myMoves.size());
        List<BoardMove> executedMoves = new ArrayList<>();
        for (Move m : myMoves) {
            Board newBoard = new Board(board);
            int x = m.getX();
            int y = m.getY();
            int additionalInformation = getAdditionalInfo(m, player);
            newBoard.executeMove(x, y, player, additionalInformation, overrideMoves);
            //executeMove() will decrease if override = true -> but this is only an assumption
            if(overrideMoves) player.increaseOverrideStone();
            if(m.isBonus()) player.decreaseOverrideStone();
            //-------------------------------------------------
            executedMoves.add(new BoardMove(newBoard, m, player));
        }
//        System.out.println("RETURNED POSSIBLE MOVES: " + executedMoves.size());
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
        //System.out.println("map.Player " + player.getNumber() + " |MapValue: " + mapValue + " CoinParity: " + coinParity + " Mobility: " + mobility);
        return mapValue + coinParity + mobility + specialField;
    }

    public int getEvaluationForPlayerStatistic(Player player, Board board) {
        int mapValue = getMapValue(player, board);
        int coinParity = getCoinParity(player, board);
        int mobility = getMobility(player, board);
        //System.out.println("map.Player " + player.getNumber() + " |MapValue: " + mapValue + " CoinParity: " + coinParity + " Mobility: " + mobility);
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

    public int getMapValue(Player player, Board tmpBoard) {
        long myMapValue = mapAnalyzer.calculateScoreForPlayer2(player.getNumber(), tmpBoard);

        long mapValueAll = 0;
        for (int i = 0; i < numPlayers; i++) {
            mapValueAll += mapAnalyzer.calculateScoreForPlayer2(players[i].getNumber(), tmpBoard);
        }

        double tmpMapValue = ((double) myMapValue) / mapValueAll;
        return (int) (tmpMapValue * 100);
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
