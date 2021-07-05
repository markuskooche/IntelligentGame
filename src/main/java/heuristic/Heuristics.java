package heuristic;

import loganalyze.additional.AnalyzeParser;
import map.*;
import mapanalyze.MapAnalyzer;
import timelimit.ThreadTimer;
import timelimit.TimeExceededException;
import timelimit.TimeOutTask;
import timelimit.Token;

import java.util.*;

public class Heuristics {

    private Board board;
    private Player[] players;
    private int mapsAnalyzed;
    private int numPlayers;
    private MapAnalyzer mapAnalyzer;
    private int height;
    private int width;
    private static AnalyzeParser analyzeParser;

    private Token timeToken;
    private boolean timeLimited;
    private int maxSearchDepth;
    private long maxTimeForMove;
    private boolean orderMoves;
    private boolean alphaBeta;

    private List<Move> ourPickedMoves;
    private boolean edgeMoves;

    private int ourMoveCount;
    private boolean createdReachableFields = false;

    private static final int MULTIPLIER = 100000;
    private static final int SMALL_OPPONENT_PIECES_LIMIT = 15;
    private static final int DANGEROUS_COINPARITY_PERCENTAGE = (int) (0.15 * MULTIPLIER);

    public Heuristics(Board board, Player[] players, MapAnalyzer mapAnalyzer, AnalyzeParser analyzeParser) {
        this.board = board;
        this.players = players;
        this.numPlayers = players.length;
        this.mapAnalyzer = mapAnalyzer;
        this.height = board.getHeight();
        this.width = board.getWidth();
        this.timeLimited = false;
        timeToken = new Token();
        Heuristics.analyzeParser = analyzeParser;

        ourPickedMoves = new ArrayList<>();
        ourMoveCount = 0;
    }

    public Move getMoveByDepth(Player ourPlayer, int searchDepth, boolean orderMoves, boolean alphaBeta) {
        timeLimited = false;
        this.maxSearchDepth = searchDepth;
        this.orderMoves = orderMoves;
        this.alphaBeta = alphaBeta;
        return getMove(ourPlayer);
    }

    public Move getMoveByTime(Player ourPlayer, long maxTimeForMove, boolean orderMoves, boolean alphaBeta) {
        timeLimited = true;
        this.maxTimeForMove = maxTimeForMove;
        this.orderMoves = orderMoves;
        this.alphaBeta = alphaBeta;
        return getMove(ourPlayer);
    }

    private void startTimer(long maxTimeForMove) {
        timeToken.start();
        Thread t = new Thread(new ThreadTimer(timeToken));
        Timer timer = new Timer();
        timer.schedule(new TimeOutTask(t, timer), (int)(maxTimeForMove * 0.70));
        t.start();
    }

    private Move getMove(Player ourPlayer) {
        if (timeLimited) startTimer(maxTimeForMove);
        ourMoveCount++;
        Move move;
        int ourPlayerNum = ourPlayer.getIntNumber();
        int nextPlayerNum = (ourPlayerNum % numPlayers) + 1;
        Board startBoard = new Board(board.getField(), board.getAllTransitions(), numPlayers, board.getBombRadius());

        boolean override = false;
        List<Move> ourMoves = board.getLegalMoves(ourPlayer, override);

        //No normal moves found, check for overrideStone-Moves
        if(ourMoves.isEmpty()) {
            ourMoves = board.getLegalMoves(ourPlayer, true);
            if (ourMoves.isEmpty()) System.out.println(board.toString());
            return onlyOverrideStones(startBoard, ourPlayer, ourMoves);
        }

        //We have only one normal Move (maybe a lot OverrideStones Moves)
        if (ourMoves.size() == 1) {
            Move m = ourMoves.get(0);
            if (m.isChoice()) {
                PlayerEvaluation evaluation = new PlayerEvaluation(mapAnalyzer, players);
                int choicePlayer = evaluation.getBestPlayer(ourPlayer.getIntNumber(), board);
                m.setChoicePlayer(choicePlayer);
            }
            return ourMoves.get(0);
        }

        move = ourMoves.get(0); //Pick the first possible Move
        LineList lineList;
        try {
            lineList = new LineFinder(mapAnalyzer).findLines(ourMoves, startBoard, ourPlayer, timeLimited, timeToken);
        } catch (TimeExceededException e) { return move; }

        new MoveFilter(mapAnalyzer, players, board).filterMoves(lineList, ourMoves, ourPlayer);
        if (ourMoves.size() == 1) return ourMoves.get(0);

        ourMoves.sort((m1, m2) -> m2.compareToMoveValue(m1)); //Sort Greedy the left moves
        move = ourMoves.get(0);

        //MapAnalyzer: Try to create reachable fields
        try { createReachableField(); } catch (TimeExceededException e) { return move; }

        //Get all possible Boards with out possible moves
        List<BoardMove> executedStartMoves;
        try { executedStartMoves = executeAllMoves(ourPlayer,startBoard, ourMoves); }
        catch (TimeExceededException e) { return move; }


        ourPickedMoves = ourMoves;
        if (lineList.getEdgeLines().size() > 0) edgeMoves = true;
        else edgeMoves = false;

        if (timeLimited) {
            int searchDepth = 2;
            while(!timeToken.timeExceeded()) {
                mapsAnalyzed = 0;
                if (searchDepth == numPlayers * 2 + 1) return move;
                Move tmpMove;
                try {
                    tmpMove = startSearching(executedStartMoves, searchDepth, nextPlayerNum, ourPlayerNum);
                    searchDepth ++;
                    analyzeParser.searchDepth(searchDepth);
                } catch (TimeExceededException e) { return move; }

                if (tmpMove != null || !tmpMove.isEmpty()) {
                    int searValue = tmpMove.getSearchValue();
                    if (searValue > 100000) {
                        System.out.println(searValue);
                        move = tmpMove;
                    }
                }
            }
        } else {
            if (maxSearchDepth >= 2) {
                try {
                    Move tmpMove = startSearching(executedStartMoves, maxSearchDepth, nextPlayerNum, ourPlayerNum);

                    int searchValue = tmpMove.getSearchValue();
                    if (searchValue > 100000) {
                        move = tmpMove;
                    }
                } catch (TimeExceededException ignored) { }
            }
        }
        return move;
    }

    private Move startSearching(List<BoardMove> executedStartMoves, int searchDepth, int nextPlayerNum,
                                int ourPlayerNum) throws TimeExceededException {
        Move move = new Move();
        int depth = 1;
        int value = Integer.MIN_VALUE;
        int maxLoop = 0;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;
        for (BoardMove boardMove : executedStartMoves) {
            mapsAnalyzed++;
            int tmpValue = searchMove(nextPlayerNum, ourPlayerNum, boardMove.getBoard(), boardMove.getMove(), depth, searchDepth, maxLoop,
                    alpha, beta);
            if (tmpValue > value) {
                value = tmpValue;
                move = boardMove.getMove();
                move.setSearchValue(value);
                if (alphaBeta) alpha = tmpValue;
            }
            depth = 1;
        }
        return move;
    }

    private int searchMove(int currPlayer, int ourPlayerNum, Board board, Move move, int depth, int maxDepth, int maxLoop,
                           int alpha , int beta) throws TimeExceededException {

        if (timeLimited && timeToken.timeExceeded()) throw new TimeExceededException();

        Player player = players[currPlayer - 1];
        Player ourPlayer = players[ourPlayerNum - 1];
        PlayerEvaluation evaluation = new PlayerEvaluation(mapAnalyzer, players);
        depth++;

        if(depth > maxDepth) return evaluateSituation(board, move);

        // Get all possible moves for this depth
        List<BoardMove> executedMoves;
        boolean override = false;
        if (player != ourPlayer) {
            //Prevent dead because of not considered overridestone moves
            if (getAmountStones(player, board) < SMALL_OPPONENT_PIECES_LIMIT
                    || evaluation.getCoinParity(ourPlayer, board) < DANGEROUS_COINPARITY_PERCENTAGE) {
                override = true;
            }
        }
        List<Move> playerMoves = board.getLegalMoves(player, override);

        // No moves for given player -> another player should move
        // Or the player is disqualified
        if (playerMoves.isEmpty() || player.isDisqualified()) {
            //We are dead
            if (player == ourPlayer && getAmountStones(ourPlayer, board) == 0) return -80000;

            //Maybe the Player can do overridestone moves
            boolean noMoves = true;
            if(player.getOverrideStone() > 0){
                playerMoves = board.getLegalMoves(player, true);
                if (!playerMoves.isEmpty()) noMoves = false;
            }

            if (noMoves) {
                int nextPlayer = (currPlayer % numPlayers) + 1;
                if(maxLoop < numPlayers) {
                    maxLoop++;
                    return searchMove(nextPlayer, ourPlayerNum, board, move,depth - 1, maxDepth, maxLoop,
                            alpha, beta);
                } else { return 0; } //No player has any moves
            }
        }
        notAgainstUs(playerMoves, player, ourPlayer);

        if (player == ourPlayer) {
            LineList lineList = new LineFinder(mapAnalyzer).findLines(playerMoves, board, player, timeLimited, timeToken);
            new MoveFilter(mapAnalyzer, players, board).filterMoves(lineList, playerMoves, player);
        }

        executedMoves = executeAllMoves(player, board, playerMoves);

        int value = Integer.MAX_VALUE; //MIN
        if (player == ourPlayer) value = Integer.MIN_VALUE; //MAX
        int nextPlayer = (currPlayer % numPlayers) + 1;
        for (BoardMove boardMove : executedMoves) {

            mapsAnalyzed++;
            int tmpValue = searchMove(nextPlayer, ourPlayerNum, boardMove.getBoard(), boardMove.getMove(), depth, maxDepth, maxLoop,
                    alpha, beta);

            if (player == ourPlayer) { //MAX
                if (tmpValue > value) value = tmpValue;
                if (alphaBeta) {
                    if (tmpValue > alpha) alpha = tmpValue;
                    if (tmpValue >= beta) return value; //Cut off
                }
            } else { //MIN
                if (tmpValue < value) value = tmpValue;
                if (alphaBeta) {
                    if (tmpValue < beta) beta = tmpValue;
                    if (tmpValue <= alpha) return value; // Cut off
                }
            }
            if (timeLimited && timeToken.timeExceeded()) throw new TimeExceededException();
        }

        return value;
    }

    private int evaluateSituation(Board board, Move move ) {
        int value = move.getMoveValue();
        Player player = move.getPlayer();
        if (move.isBonus()) {
            return 500000;
        }
        if (move.isChoice()) {
            return 499999;
        }

        for (Move m : ourPickedMoves) {
            // We can make now this move
            // We are looking for new good moves, not for one we already have
            if (m.getX() == move.getX() && m.getY() == m.getY()) {
                int kills = move.getList().size();
                return kills;
            }
        }

        List<int[]> corners = mapAnalyzer.getInterestingCornerFieldList();
        for (int[] corner : corners) {
            if (corner[0] == move.getX() && corner[1] == move.getY()) {
                int cornerValue = move.getMoveValue();
                return 400000 + cornerValue;
            }
        }

        if (!edgeMoves) {
            List<int[]> edges = mapAnalyzer.getInterestingEdgeFieldList();
            for (int[] edge : edges) {
                if (edge[0] == move.getX() && edge[1] == move.getY()) {
                    int edgeValue = move.getMoveValue();
                    int kills = move.getList().size();
                    return 100000 + edgeValue + kills;
                }
            }
        }

        if (value == Integer.MIN_VALUE) {
            PlayerEvaluation evaluation = new PlayerEvaluation(mapAnalyzer, players);
            value = evaluation.getEvaluationForPlayer(player, board);
        }

        return value;
    }

    private void notAgainstUs(List<Move> playerMoves, Player player, Player ourPlayer) {
        if (player == ourPlayer) return;
        List<Move> againstMe = new ArrayList<>();
        for (Move m : playerMoves) {
            //Get all moves against me
            if (m.getAims().indexOf(ourPlayer.getCharNumber()) > -1) {
                againstMe.add(m);
            }
        }
        if (againstMe.isEmpty()) {
            Move move = playerMoves.get(0);
            playerMoves.clear();
            playerMoves.add(move);
        }
    }

    private Move onlyOverrideStones(Board board, Player player, List<Move> myMoves) {
        Move move = myMoves.get(0); //Pick the first possible Move
        List<BoardMove> executedStartMoves;
        try {
            executedStartMoves = executeAllMoves(player,board, myMoves);
        } catch (TimeExceededException e) { return move; }

        int value = Integer.MIN_VALUE;
        PlayerEvaluation evaluation = new PlayerEvaluation(mapAnalyzer, players);
        for (BoardMove boardMove : executedStartMoves) {
            if (timeLimited && timeToken.timeExceeded()) return move;
            mapsAnalyzed++;
            int tmpValue = evaluation.getCoinParity(player, boardMove.getBoard());
            if (tmpValue >= value) {
                value = tmpValue;
                move = boardMove.getMove();
            }
        }
        return move;
    }

    private void sortExecutedMoves(List<BoardMove> executedMoves, Player ourPlayer, Player currPlayer) throws TimeExceededException {

        PlayerEvaluation evaluation = new PlayerEvaluation(mapAnalyzer, players);
        Map<BoardMove, Integer> evaluatedBoards = new LinkedHashMap<>();
        for (BoardMove boardMove : executedMoves) {
            if (timeLimited && timeToken.timeExceeded()) throw new TimeExceededException();

            Board b = boardMove.getBoard();
            int value = evaluation.getEvaluationForPlayer(ourPlayer, b);
            evaluatedBoards.put(boardMove, value);
        }

        List<Map.Entry<BoardMove, Integer>> boardEntryList = new ArrayList<>(evaluatedBoards.entrySet());
        boardEntryList.sort(Map.Entry.comparingByValue()); //Sorting ascending

        executedMoves.clear();
        for (Map.Entry<BoardMove, Integer> entry : boardEntryList) {
            if (timeLimited && timeToken.timeExceeded()) throw new TimeExceededException();

            if (ourPlayer == currPlayer) { //MAX
                executedMoves.add(0, entry.getKey());
            } else { //MIN
                executedMoves.add(entry.getKey());
            }
        }
    }

    private List<BoardMove> executeAllMoves(Player player, Board board, List<Move> myMoves) throws TimeExceededException {
        List<BoardMove> executedMoves = new ArrayList<>();
        for (Move m : myMoves) {
            if (timeLimited && timeToken.timeExceeded()) throw new TimeExceededException();

            Board newBoard = new Board(board);
            int additionalInformation = getAdditionalInfo(m, player, newBoard);
            newBoard.colorizeMove(m, player, additionalInformation);

            //executeMove() will decrease if override = true -> but this is only an assumption
            // similar with bonus
            if(m.isOverride()) player.increaseOverrideStone();
            if(m.isBonus()) player.decreaseOverrideStone();
            //-------------------------------------------------
            executedMoves.add(new BoardMove(newBoard, m, player));
        }
        return executedMoves;
    }

    private void createReachableField() throws TimeExceededException {
        if(!createdReachableFields && !mapAnalyzer.failedToSetup()) {
            mapAnalyzer.startReachableField(timeLimited, timeToken);
            mapAnalyzer.createField();
            createdReachableFields = true;
            //TODO im Auge behalten
           // System.out.println("Geschafft_______________________________________________________________");
           // System.out.println(mapAnalyzer.getBoardValues());
        }

    }

    /**
     * Special field decision
     */
    private int getAdditionalInfo(Move move, Player player, Board board) {
        int info = 0;
        if (move.isBonus()) info = 21; // we take allways a OverrideStone
        if (move.isChoice()) {
           PlayerEvaluation evaluation = new PlayerEvaluation(mapAnalyzer, players);
           info =  evaluation.getBestPlayer(player.getIntNumber(), board);
        }
        return info;
    }

    private int getAmountStones(Player player, Board board) {
        List<int[]> myStones = new ArrayList<>();

        char field[][] = board.getField();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (field[y][x] == player.getCharNumber()) {
                    myStones.add(new int[] {x, y});
                }
            }
        }
        return myStones.size();
    }
}
