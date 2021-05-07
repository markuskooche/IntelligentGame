package heuristic;

import map.Board;
import map.Move;
import map.Player;
import mapanalyze.MapAnalyzer;

import java.util.*;

public class Heuristics {

    private Board board;
    private Player[] players;
    private int mapsAnalyzed;
    private int numPlayers;
    private MapAnalyzer mapAnalyzer;
    private int height;
    private int width;

    public Heuristics(Board board, Player[] players, MapAnalyzer mapAnalyzer) {
        this.board = board;
        this.players = players;
        numPlayers = players.length;
        this.mapAnalyzer = mapAnalyzer;
        this.height = board.getHeight();
        this.width = board.getWidth();
    }

    public Move getMoveParanoid(Player player, int searchDepth, boolean alphaBeta, boolean orderMoves) {
        int numPlayers = players.length;
        int ourPlayerNum = player.getNumber() - '0';
        Player ourPlayer = players[ourPlayerNum - 1];

        Board tmpBoardStart = new Board(board.getField(), board.getAllTransitions(), numPlayers, board.getBombRadius());

        List<BoardMove> executedStartMoves = executeAllMoves(player,tmpBoardStart, false);
        if (executedStartMoves.isEmpty()) {
            return onlyOverrideStone(tmpBoardStart, player);
        }
        if (executedStartMoves.size() == 1) {
            return executedStartMoves.get(0).getMove();
        }

        if (orderMoves) sortExecutedMoves(executedStartMoves, ourPlayer, ourPlayer);

        mapsAnalyzed = 0;
        int depth = 1;
        int nextPlayerNum = (ourPlayerNum % numPlayers) + 1;
        int value = Integer.MIN_VALUE;
        int maxLoop = 0;
        Move move = new Move();
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;
        for (BoardMove boardMove : executedStartMoves) {
            mapsAnalyzed++;
            if (depth < searchDepth) {
                int tmpValue  =  searchParanoid(nextPlayerNum, ourPlayerNum, boardMove.getBoard(), boardMove.getMove(), depth, searchDepth, maxLoop,
                        alpha, beta, alphaBeta, orderMoves);
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
        System.out.println("Analyzed Maps: " + mapsAnalyzed);
        System.out.println("XT01-98-AM-" + mapsAnalyzed);
        return move;
    }

    private Move onlyOverrideStone(Board board, Player player) {
        List<BoardMove> executedStartMoves = executeAllMoves(player,board, true);
        int value = Integer.MIN_VALUE;
        Move move = new Move();
        for (BoardMove boardMove : executedStartMoves) {
            mapsAnalyzed++;
            //int tmpValue = getEvaluationForPlayer(player, boardMove.getBoard(), boardMove.getMove());
            int tmpValue = getCoinParity(player, boardMove.getBoard());
            if (tmpValue >= value) {
                value = tmpValue;
                move = boardMove.getMove();
            }
        }

        return move;
    }

    private int searchParanoid(int currPlayer, int ourPlayerNum, Board board, Move move, int depth, int maxDepth, int maxLoop,
                               int alpha ,int beta, boolean alphaBeta, boolean orderMoves) {
        Player player = players[currPlayer - 1];
        Player ourPlayer = players[ourPlayerNum - 1];
        depth++;

        if(depth > maxDepth) return getEvaluationForPlayer(ourPlayer, board, move);

        List<BoardMove> executedMoves = executeAllMoves(player, board, false);

        // No moves for given player -> another player should move
        // Or the player is disqualified
        if (executedMoves.isEmpty() || player.isDisqualified()) {
            int nextPlayer = (currPlayer % numPlayers) + 1;
            if(maxLoop < numPlayers) {
                maxLoop++;
                return searchParanoid(nextPlayer, ourPlayerNum, board, move,depth - 1, maxDepth, maxLoop,
                        alpha, beta, alphaBeta, orderMoves);
            } else {
                return 0; //No player has any move (perhaps only override)
            }
        }

        if (orderMoves && depth < (maxDepth - 1)) sortExecutedMoves(executedMoves, ourPlayer, player);

        int value;
        if (player == ourPlayer) {
            value = Integer.MIN_VALUE; //MAX
        } else {
            value = Integer.MAX_VALUE; //MIN
        }

        int tmpValue;
        int nextPlayer = (currPlayer % numPlayers) + 1;
        for (BoardMove boardMove : executedMoves) {
            mapsAnalyzed++;
            tmpValue = searchParanoid(nextPlayer, ourPlayerNum, boardMove.getBoard(), boardMove.getMove(), depth, maxDepth, maxLoop,
                    alpha, beta, alphaBeta, orderMoves);

            if (player == ourPlayer) { //MAX
                if (tmpValue > value) value = tmpValue;
                if (alphaBeta) {
                    if (tmpValue > alpha) alpha = tmpValue;
                    if (tmpValue >= beta) {
                        return value; //Cut off
                    }
                }
               } else { //MIN
                if (tmpValue < value) value = tmpValue;
                if (alphaBeta) {
                    if (tmpValue < beta) beta = tmpValue;
                    if (tmpValue <= alpha) {
                        return value; // Cut off
                    }
                }
            }
        }

        return value;
    }


    private void sortExecutedMoves(List<BoardMove> executedMoves, Player ourPlayer, Player currPlayer) {

        Map<BoardMove, Integer> evaluatedBoards = new LinkedHashMap<>();

        for (BoardMove boardMove : executedMoves) {
            Board b = boardMove.getBoard();
            Move m = boardMove.getMove();
            int value = getEvaluationForPlayer(ourPlayer, b, m);
            evaluatedBoards.put(boardMove, value);
        }

        List<Map.Entry<BoardMove, Integer>> boardEntryList = new ArrayList<>(evaluatedBoards.entrySet());
        boardEntryList.sort(Map.Entry.comparingByValue()); //Sorting ascending

        executedMoves.clear();
        for (Map.Entry<BoardMove, Integer> entry : boardEntryList) {
            if (ourPlayer == currPlayer) { //MAX
                executedMoves.add(0, entry.getKey());
            } else { //MIN
                executedMoves.add(entry.getKey());
            }
        }
    }

    private List<BoardMove> executeAllMoves(Player player, Board board, boolean overrideMoves) {
        List<Move> myMoves = board.getLegalMoves(player, overrideMoves);
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
        //int mapValue = getMapValue(player, board);
        int mapValue2 = getMapValue2(player, board);
        int coinParity = getCoinParity(player, board);
        int mobility = getMobility(player, board);
        int specialField = getSpecialFieldValue(move);
        //System.out.println("Map: " + mapValue + " Coins: " + coinParity + " Mobility: " + mobility);
        return mapValue2 + coinParity + mobility + specialField;
    }

    public int getEvaluationForPlayerStatistic(Player player, Board board) {
        int mapValue = getMapValue(player, board);
        int coinParity = getCoinParity(player, board);
        int mobility = getMobility(player, board);
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
        return (int) (tmpMapValue * 100000 * 1.15);
    }
    public int getMapValue2(Player player, Board tmpBoard) {
        int factor = (int) (100000 * 1.15);
        return mapAnalyzer.calculateScoreForPlayers(player, tmpBoard, players, factor);
    }

    public int getCoinParity(Player player, Board board) {
        List<int[]> myStones = new ArrayList<>();
        List<int[]> playerStones = new ArrayList<>();
        String playersNum = "";
        for(Player p : players) {
            playersNum += p.getNumber();
        }

        char field[][] = board.getField();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (field[y][x] == player.getNumber()) {
                    myStones.add(new int[] {x, y});
                }
                if (playersNum.indexOf(field[y][x]) != -1) {
                    playerStones.add(new int[] {x, y});
                }
            }
        }

        double allStones = playerStones.size();
        double myStonesAmount = myStones.size();
        double result = myStonesAmount / allStones * 100000;
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
        double result = myMovesAmount / allMoves * 100000;

        return (int) result;
    }
}
