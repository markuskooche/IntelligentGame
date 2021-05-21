package heuristic;

import loganalyze.additional.AnalyzeParser;
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
    private static AnalyzeParser analyzeParser;

    private Token timeToken;
    private boolean timeLimited;
    private int maxSearchDepth;
    private long maxTimeForMove;

    private static final int MULTIPLIER = 100000;
    //If opponent has less then 15 pieces, then check also for override stones
    //prevent dead-moves in start positions
    //((int) (1 / ((double) 1.5 * numPlayers))); (MARKUS)
    private static final int SMALL_OPPONENT_PIECES_LIMIT = 15;
    private static final int DANGEROUS_COINPARITY_PERCENTAGE = 15 * MULTIPLIER;

    private boolean createdReachableFields = false;

    public Heuristics(Board board, Player[] players, MapAnalyzer mapAnalyzer, AnalyzeParser analyzeParser) {
        this.board = board;
        this.players = players;
        numPlayers = players.length;
        this.mapAnalyzer = mapAnalyzer;
        this.height = board.getHeight();
        this.width = board.getWidth();
        this.timeLimited = false;
        timeToken = new Token();
        Heuristics.analyzeParser = analyzeParser;
    }

    public Move getMoveByDepth(Player ourPlayer, int searchDepth, boolean orderMoves, boolean alphaBeta) {
        timeLimited = false;
        this.maxSearchDepth = searchDepth;
        return getMove(ourPlayer, orderMoves, alphaBeta);
    }

    public Move getMoveByTime(Player ourPlayer, long maxTimeForMove, boolean orderMoves, boolean alphaBeta) {
        timeLimited = true;
        this.maxTimeForMove = maxTimeForMove;
        return getMove(ourPlayer, orderMoves, alphaBeta);
    }

    private void startTimer(long maxTimeForMove) {
        timeToken.start();
        Thread t = new Thread(new ThreadTimer(timeToken));
        Timer timer = new Timer();
        timer.schedule(new TimeOutTask(t, timer), (int)(maxTimeForMove * 0.70));
        t.start();
    }

    private Move getMove(Player ourPlayer, boolean orderMoves, boolean alphaBeta) {
        if (timeLimited) startTimer(maxTimeForMove);
        Move move;
        int ourPlayerNum = ourPlayer.getNumber() - '0';
        int nextPlayerNum = (ourPlayerNum % numPlayers) + 1;
        Board startBoard = new Board(board.getField(), board.getAllTransitions(), numPlayers, board.getBombRadius());

        List<Move> myMoves = board.getLegalMoves(ourPlayer, false);

        //No normal moves found, check for overrideStone-Moves
        if(myMoves.isEmpty()) {
            myMoves = board.getLegalMoves(ourPlayer, true);
            return onlyOverrideStones(startBoard, ourPlayer, myMoves);
        }

        //We have only one normal Move (maybe a lot OverrideStones Moves)
        if (myMoves.size() == 1) return myMoves.get(0);

        move = myMoves.get(0); //Pick the first possible Move
        if(!createdReachableFields) {
            try {
                mapAnalyzer.startReachableField(timeLimited, timeToken);
            } catch (TimeExceededException e) { return move; }
            createdReachableFields = true;
        }


        //Get all possible Boards with out possible moves
        List<BoardMove> executedStartMoves;
        try {
            executedStartMoves = executeAllMoves(ourPlayer,startBoard, myMoves, false);
        } catch (TimeExceededException e) { return move; }

        //Sort all possible moves
        if (orderMoves) {
            try {
                sortExecutedMoves(executedStartMoves, ourPlayer, ourPlayer);
            } catch (TimeExceededException e) { return move; }
        }

        //Get best Move for depth 1
        int pickedBoardValue = Integer.MIN_VALUE;
        for (BoardMove boardMove : executedStartMoves) {
            if (timeLimited && timeToken.timeExceeded()) return move;
            Board b = boardMove.getBoard();
            Move m = boardMove.getMove();
            int tmpValue = getEvaluationForPlayer(ourPlayer, b, m);
            if (tmpValue > pickedBoardValue) {
                pickedBoardValue = tmpValue;
                move = boardMove.getMove();
            }
        }

        if (timeLimited) {
            int searchDepth = 2;
            while(!timeToken.timeExceeded()) {
                mapsAnalyzed = 0;
                if (searchDepth == numPlayers * 3) return move;
                Move tmpMove;
                try {
                    tmpMove = startSearching(executedStartMoves, searchDepth, nextPlayerNum, ourPlayerNum,
                            alphaBeta, orderMoves);
                    searchDepth ++;
                    analyzeParser.searchDepth(searchDepth);
                } catch (TimeExceededException e) {
                    return move;
                }
                if (tmpMove != null) move = tmpMove;
            }
        } else {
            if (maxSearchDepth >= 2) {
                try {
                    move = startSearching(executedStartMoves, maxSearchDepth, nextPlayerNum, ourPlayerNum,
                            alphaBeta, orderMoves);
                } catch (TimeExceededException e) { }
            }
        }
        return move;
    }

    private Move startSearching(List<BoardMove> executedStartMoves, int searchDepth, int nextPlayerNum, int ourPlayerNum,
                                boolean alphaBeta, boolean orderMoves) throws TimeExceededException {
        Move move = new Move();
        int depth = 1;
        int value = Integer.MIN_VALUE;
        int maxLoop = 0;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;
        for (BoardMove boardMove : executedStartMoves) {
            mapsAnalyzed++;
            int tmpValue = searchMove(nextPlayerNum, ourPlayerNum, boardMove.getBoard(), boardMove.getMove(), depth, searchDepth, maxLoop,
                    alpha, beta, alphaBeta, orderMoves);
            if (tmpValue > value) {
                value = tmpValue;
                move = boardMove.getMove();
                if (alphaBeta) alpha = tmpValue;
            }
            depth = 1;
        }
        return move;
    }

    private int searchMove(int currPlayer, int ourPlayerNum, Board board, Move move, int depth, int maxDepth, int maxLoop,
                           int alpha , int beta, boolean alphaBeta, boolean orderMoves) throws TimeExceededException {

        if (timeLimited && timeToken.timeExceeded()) throw new TimeExceededException();

        Player player = players[currPlayer - 1];
        Player ourPlayer = players[ourPlayerNum - 1];
        depth++;

        if(depth > maxDepth) return getEvaluationForPlayer(ourPlayer, board, move);

        // Get all possible moves for this depth
        List<BoardMove> executedMoves;
        if(player != ourPlayer && getAmountStones(player, board) < SMALL_OPPONENT_PIECES_LIMIT) {
            List<Move> myMoves = board.getLegalMoves(ourPlayer, true);
            executedMoves = executeAllMoves(player, board, myMoves, true);
        } else if(player != ourPlayer && getCoinParity(ourPlayer, board) < DANGEROUS_COINPARITY_PERCENTAGE){
            List<Move> myMoves = board.getLegalMoves(ourPlayer, true);
            executedMoves = executeAllMoves(player, board, myMoves, true);
        } else{
            List<Move> myMoves = board.getLegalMoves(ourPlayer, false);
            executedMoves = executeAllMoves(player, board, myMoves, false);
        }

        // No moves for given player -> another player should move
        // Or the player is disqualified
        if (executedMoves.isEmpty() || player.isDisqualified()) {
            if (player == ourPlayer && getAmountStones(ourPlayer, board) == 0) {
                return Integer.MIN_VALUE; //In this situation we are dead
            }
            int nextPlayer = (currPlayer % numPlayers) + 1;
            if(maxLoop < numPlayers) {
                maxLoop++;
                return searchMove(nextPlayer, ourPlayerNum, board, move,depth - 1, maxDepth, maxLoop,
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
            tmpValue = searchMove(nextPlayer, ourPlayerNum, boardMove.getBoard(), boardMove.getMove(), depth, maxDepth, maxLoop,
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
            if (timeLimited && timeToken.timeExceeded()) throw new TimeExceededException();
        }

        return value;
    }

    private Move onlyOverrideStones(Board board, Player player, List<Move> myMoves) {
        Move move = myMoves.get(0); //Pick the first possible Move
        List<BoardMove> executedStartMoves;
        try {
            executedStartMoves = executeAllMoves(player,board, myMoves, true);
        } catch (TimeExceededException e) { return move; }

        int value = Integer.MIN_VALUE;
        for (BoardMove boardMove : executedStartMoves) {
            if (timeLimited && timeToken.timeExceeded()) return move;
            mapsAnalyzed++;
            int tmpValue = getCoinParity(player, boardMove.getBoard());
            if (tmpValue >= value) {
                value = tmpValue;
                move = boardMove.getMove();
            }
        }

        return move;
    }

    private void sortExecutedMoves(List<BoardMove> executedMoves, Player ourPlayer, Player currPlayer) throws TimeExceededException {

        Map<BoardMove, Integer> evaluatedBoards = new LinkedHashMap<>();
        for (BoardMove boardMove : executedMoves) {
            if (timeLimited && timeToken.timeExceeded()) throw new TimeExceededException();

            Board b = boardMove.getBoard();
            Move m = boardMove.getMove();
            int value = getEvaluationForPlayer(ourPlayer, b, m);
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

    private List<BoardMove> executeAllMoves(Player player, Board board, List<Move> myMoves, boolean overrideMoves) throws TimeExceededException {
        List<BoardMove> executedMoves = new ArrayList<>();
        for (Move m : myMoves) {
            if (timeLimited && timeToken.timeExceeded()) throw new TimeExceededException();

            Board newBoard = new Board(board);
            int additionalInformation = getAdditionalInfo(m, player);
            newBoard.colorizeMove(m, player, additionalInformation);

            //executeMove() will decrease if override = true -> but this is only an assumption
            // similar with bonus
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
        int mapValue = getMapValue(player, board);
        int coinParity = getCoinParity(player, board);
        int mobility = getMobility(player, board);
        int specialField = getSpecialFieldValue(move);
        return mapValue + coinParity + mobility + specialField;
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
        return value * MULTIPLIER;
    }

    public int getMapValue(Player player, Board tmpBoard) {
        /*
        // TODO: [IWAN] Methoden an erforderlichen Stellen aufrufen
        long myMapValue = tmpBoard.getPlayerScores()[(Integer.parseInt(String.valueOf(player.getNumber()))-1)];

        long mapValueAll = 0;
        for (int i = 0; i < numPlayers; i++) {
            mapValueAll += tmpBoard.getPlayerScores()[i];
        }
        */

        // TODO: LÖSCHEN
        long myMapValue = mapAnalyzer.calculateScoreForPlayerOLD(player.getNumber(), tmpBoard);

        long mapValueAll = 0;
        for (int i = 0; i < numPlayers; i++) {
            mapValueAll += mapAnalyzer.calculateScoreForPlayerOLD(players[i].getNumber(), tmpBoard);
        }
        // TODO: BIS HIER HIN

        double tmpMapValue = ((double) myMapValue) / mapValueAll;
        return (int) (tmpMapValue * MULTIPLIER);
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
        double result = myStonesAmount / allStones;
        return (int) (result * MULTIPLIER);
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
        double result = myMovesAmount / allMoves;

        return (int) (result * MULTIPLIER * 0.7);
    }

    private int getAmountStones(Player player, Board board) {
        List<int[]> myStones = new ArrayList<>();

        char field[][] = board.getField();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (field[y][x] == player.getNumber()) {
                    myStones.add(new int[] {x, y});
                }
            }
        }
        return myStones.size();
    }

    public int getBestPlayer(int ourPlayer) {
        int bestPlayer = ourPlayer;
        int bestEvaluation = Integer.MIN_VALUE;

        for (Player player : players) {
            if (!player.isDisqualified()) {
                Move emptyMove = new Move();
                int evaluation = getEvaluationForPlayer(player, board, emptyMove);

                if (evaluation > bestEvaluation) {
                    bestEvaluation = evaluation;
                    bestPlayer = (player.getNumber() - '0');
                }
            }
        }

        return bestPlayer;
    }
}
