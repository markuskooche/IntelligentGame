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

    private int ourMoveCount;
    private boolean createdReachableFields = false;

    private static final int MULTIPLIER = 100000;
    //If opponent has less then 15 pieces, then check also for override stones
    //prevent dead-moves in start positions
    //((int) (1 / ((double) 1.5 * numPlayers))); (MARKUS)
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
        //if (ourMoveCount > 15 && ourPlayer.getOverrideStone() > 0) override = true;
        List<Move> ourMoves = board.getLegalMoves(ourPlayer, override);

        //No normal moves found, check for overrideStone-Moves
        if(ourMoves.isEmpty()) {
            ourMoves = board.getLegalMoves(ourPlayer, true);
            return onlyOverrideStones(startBoard, ourPlayer, ourMoves);
        }

        //We have only one normal Move (maybe a lot OverrideStones Moves)
        if (ourMoves.size() == 1) return ourMoves.get(0);

        move = ourMoves.get(0); //Pick the first possible Move
        LineList lineList = analyzeMoves(ourMoves, startBoard, ourPlayer);
        filterMoves(lineList, ourMoves);
        ourMoves.sort((m1, m2) -> m2.compareTo(m1)); //Sort Greedy the left moves

        //MapAnalyzer: Try to create reachable fields
        try { createReachableField(); } catch (TimeExceededException e) { return move; }



        //Get all possible Boards with out possible moves
        List<BoardMove> executedStartMoves;
        try { executedStartMoves = executeAllMoves(ourPlayer,startBoard, ourMoves); }
        catch (TimeExceededException e) { return move; }

        //Sort all possible moves
        if (orderMoves) {
            try { sortExecutedMoves(executedStartMoves, ourPlayer, ourPlayer); }
            catch (TimeExceededException e) { return move; }
            move = executedStartMoves.get(0).getMove(); //Get best Move for depth 1
        }

//        try {
//            move = getStableMove(executedStartMoves, ourPlayer);
//        } catch (TimeExceededException e) {
//            return move;
//        }
        //if(alphaBeta) return move;

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

                if (tmpMove != null) move = tmpMove;
            }
        } else {
            if (maxSearchDepth >= 2) {
                try {
                    move = startSearching(executedStartMoves, maxSearchDepth, nextPlayerNum, ourPlayerNum);
                } catch (TimeExceededException e) { }
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
        depth++;

        if(depth > maxDepth) return getEvaluationForPlayer(ourPlayer, board, move);

        // Get all possible moves for this depth
        List<BoardMove> executedMoves;
        boolean override = false;
        if (player != ourPlayer) {
            //Prevent dead because of not considered overridestone moves
            if (getAmountStones(player, board) < SMALL_OPPONENT_PIECES_LIMIT
                    || getCoinParity(ourPlayer, board) < DANGEROUS_COINPARITY_PERCENTAGE) {
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
        executedMoves = executeAllMoves(player, board, playerMoves);

        if (orderMoves && depth < (maxDepth - 1)) sortExecutedMoves(executedMoves, ourPlayer, player);

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

    private Move getStableMove(List<BoardMove> executedStartMoves, Player ourPlayer) throws TimeExceededException {

        int open = 0;
        int control = 0;
        Move move = new Move();
        for (BoardMove boardMove : executedStartMoves) {
            List<Player> myEnemys = new ArrayList<>();
            Board board = boardMove.getBoard();
            List<Move> enemyMoves = getAllEnemyMoves(ourPlayer, board, false);
            List<Move> againsMe = new ArrayList<>();
            for (Move enemyMove : enemyMoves) {
                //Get all moves against me
                if (enemyMove.getAims().indexOf(ourPlayer.getCharNumber()) > -1) {
                    againsMe.add(enemyMove);
                    Player enemy = enemyMove.getPlayer();
                    if (!myEnemys.contains(enemy)) myEnemys.add(enemy);
                }
            }

            for (Player p : myEnemys) {
                List<Move> tmp = new ArrayList<>();
                //Get all moves of one enemy player
                for (Move m : againsMe) if (m.getPlayer() == p) tmp.add(m);

                LineList lineList = analyzeMoves(tmp, board, p);
                int numBadMoves = lineList.getOpenLines().size();
                if (numBadMoves > open) {
                    open = numBadMoves;
                    move = boardMove.getMove();
                }

            }

        }
        return move;
    }

    private List<Move> getAllEnemyMoves(Player ourPlayer, Board board, boolean override) throws TimeExceededException {
        List<Move> playerMoves = new ArrayList<>();
        for (Player player : players) {
            if (timeLimited && timeToken.timeExceeded()) throw new TimeExceededException();
            if (player.isDisqualified()) continue;
            if (player != ourPlayer) {
                if (player.getOverrideStone() < 0 && override) continue;
                List<Move> tmp = board.getLegalMoves(player, override);
                for (Move move : tmp) {
                    move.setPlayer(player);
                    playerMoves.add(move);
                }
            }
        }
        return playerMoves;
    }

    private void filterMoves(LineList lineList, List<Move> moves) {

        //We take always Bonus when possible
        List<Move> bonus = lineList.getBonusMoves();
        if (!bonus.isEmpty()) {
            moves.clear();
            moves.addAll(bonus);
            return;
        }

        //Back up for moves
        List<Move> tmpMove = new ArrayList<>();
        tmpMove.addAll(moves);
        moves.clear();

        //Check for wall moves
        List<Line> wallLines = lineList.getWallLines();
        if (!wallLines.isEmpty()) {
            sortWallMoves(wallLines);
            for (Line line : wallLines) moves.add(line.getMove());
        }
        if (!moves.isEmpty()) return;

        //Check for good moves
        List<Line> goodLines = lineList.getControllLines();
        if (!goodLines.isEmpty()) {
            sortGoodLines(goodLines);
            for (Line line : goodLines) moves.add(line.getMove());
        }
        if (!moves.isEmpty()) return;

        //Check for caught moves
        List<Line> caughtLines = lineList.getCaughtLines();
        if (!caughtLines.isEmpty()) {
            for (Line line : caughtLines) moves.add(line.getMove());
        }
        if (!moves.isEmpty()) return;

        //Check for bad moves
        List<Line> badLines = lineList.getOpenLines();
        if (!badLines.isEmpty()) {
            for (Line line : badLines) moves.add(line.getMove());
        }
        if (!moves.isEmpty()) return;

        //Somthing went wrong!
        System.out.println("Move sorting went wrong!");
        moves.addAll(tmpMove);
    }

    private void sortWallMoves(List<Line> wallLines) {
        int [][] mapVal = mapAnalyzer.getField();

        int biggest = Integer.MIN_VALUE;
        for (Line line : wallLines) {
            Move move = line.getMove();
            int value = mapVal[move.getY()][move.getX()];
            if (value > biggest) biggest = value;
            line.setMoveValue(value);
        }

        for (int i = 0; i < wallLines.size(); i++) {
            Line line = wallLines.get(i);
            if (line.getMoveValue() < biggest || line.getMoveValue() < 0) {
                wallLines.remove(i);
                i--;
            }
        }
    }

    private void sortGoodLines(List<Line> goodLines) {
        int [][] mapVal = mapAnalyzer.getField();

        int biggest = Integer.MIN_VALUE;
        for (Line line : goodLines) {
            Move move = line.getMove();
            int value = mapVal[move.getY()][move.getX()];
            if (value > biggest) biggest = value;
            line.setMoveValue(value);
        }

        for (int i = 0; i < goodLines.size(); i++) {
            Line line = goodLines.get(i);
            if (line.getMoveValue() < biggest) {
                goodLines.remove(i);
                i--;
            }
        }
    }

    private LineList analyzeMoves(List<Move> moves, Board board, Player ourPlayer) {
        int size = moves.size();
        char ourNumber = ourPlayer.getCharNumber();
        LineList lineList = new LineList();
        for (int i = 0; i < size; i++) {

            Move move = moves.get(i);
            char field = board.getField()[move.getY()][move.getX()];
            Map<int[],int[]> myStone = move.getPlayerDirections();

            if (field == 'b') lineList.addBonus(move);
            if (field == 'c') lineList.addChoice(move);
            if (field == 'i') lineList.addInversion(move);

            if (myStone.size() > 1 || "bic".indexOf(field) > -1) continue;

            int[] pos = new int[2];
            int[] direction = new int[2];
            for (Map.Entry<int[], int[]> entry : myStone.entrySet()) {
                pos = entry.getKey();
                direction[0] = entry.getValue()[0];
                direction[1] = entry.getValue()[1];
            }

            List<Character> stoneRow = new ArrayList<>(); // <= our pos + new stones
            for (int j = 0; j <= move.getList().size(); j++) stoneRow.add(ourNumber);

            //Check the front stone
            int xFront = move.getX();
            int yFront = move.getY();
            int [] dirFront = {direction[0] * (-1), direction[1] * (-1)};
            int [] dirBack = {direction[0], direction[1]};

            //build the line
            boolean wallFront = buildLine(xFront, yFront, dirFront, board, stoneRow, true, ourPlayer);
            boolean wallBack = buildLine(pos[0], pos[1], dirBack, board, stoneRow, false, ourPlayer);


            if (wallFront) {
                lineList.add(new Line(move, stoneRow), LineState.WALL);
                continue; // We will get corner/wall place -> safe
            }
            int lastInLines = stoneRow.size() - 1;
            if (stoneRow.get(0) == ourNumber && stoneRow.get(lastInLines) == ourNumber) {
                //We will control this line, we are first and last player
                move.setFrontDirection(dirFront); //Safe the direction -> later can filter
                lineList.add(new Line(move, stoneRow), LineState.CONTROLL);
                continue;
            }

            if (stoneRow.get(0) == stoneRow.get(lastInLines)) {
                //We are trapped between the enemy but can safely move in between
                lineList.add(new Line(move, stoneRow), LineState.CAUGHT);
                continue;
            }

            lineList.add(new Line(move, stoneRow), LineState.OPEN);
        }


        return lineList;
    }

    private boolean buildLine(int x, int y, int[] direction, Board board, List<Character> lines,  boolean front, Player ourPlayer) {
        char [][] boardField = board.getField();
        int maxX  = board.getWidth() - 1;
        int maxY = board.getHeight() - 1;
        char stone = boardField[y][x];
        char tmp;
        if(front) stone = ourPlayer.getCharNumber();

        int xStart = x;
        int yStart = y;
        while ("-0bic".indexOf(stone) == -1) {
            x += direction [0];
            y += direction [1];
            if (x > maxX || y > maxY || x < 0 || y < 0) { //Check for transition
                x = x - direction[0];
                y = y - direction[1];
                Transition transition = board.getTransition(x, y, Direction.indexOf(direction));
                if (transition == null) {
                    if (front) lines.add(boardField[y][x]);
                    else lines.add(0, boardField[y][x]);

                    if (stone == ourPlayer.getCharNumber()) return true;
                    return false;
                } else {
                    // Get new direction and new position
                    direction[0] = Direction.valueOf(transition.getR())[0] * (-1);
                    direction[1] = Direction.valueOf(transition.getR())[1] * (-1);
                    x = transition.getX();
                    y = transition.getY();
                }
            }
            tmp = stone;
            stone = boardField[y][x];
            if (stone == '-') {
                x = x - direction[0];
                y = y - direction[1];
                Transition transition = board.getTransition(x, y, Direction.indexOf(direction));
                if (transition == null) {
                    if (tmp == ourPlayer.getCharNumber()) return true;
                    return false;
                }
                direction[0] = Direction.valueOf(transition.getR())[0] * (-1);
                direction[1] = Direction.valueOf(transition.getR())[1] * (-1);
                x = transition.getX();
                y = transition.getY();
                stone = boardField[y][x];
            }
            if ("-0bic".indexOf(stone) > -1) break;
            if (front)  lines.add(stone);
            else lines.add(0, stone);
            if (xStart == x && yStart == y) break; // Loop
        }
        return false;
    }

    private Move onlyOverrideStones(Board board, Player player, List<Move> myMoves) {
        Move move = myMoves.get(0); //Pick the first possible Move
        List<BoardMove> executedStartMoves;
        try {
            executedStartMoves = executeAllMoves(player,board, myMoves);
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
            //TODO im Auge behalten
           // System.out.println(mapAnalyzer.getBoardValues());
        }
        createdReachableFields = true;
    }

    /**
     * Special field decision
     */
    private int getAdditionalInfo(Move move, Player player, Board board) {
        int info = 0;
        if (move.isBonus()) info = 21; // we take allways a OverrideStone
        if (move.isChoice()) {
           info =  getBestPlayer(player.getIntNumber(), board);
        }
        return info;
    }

    public int getEvaluationForPlayer(Player player, Board board, Move move) {
        int mapValue = getMapValue(player, board);
        int coinParity = getCoinParity(player, board);
        int mobility = getMobility(player, board);
        //System.out.println("MapValue: " + mapValue + " || CoinParity: " +
                //coinParity + " || Mobility: " + mobility + " -> " + (mapValue+coinParity+mobility));
        return mapValue + coinParity + mobility;
    }

    public int getMapValue(Player player, Board tmpBoard) {
        return mapAnalyzer.calculateScoreForPlayers(player, tmpBoard, players, MULTIPLIER);
    }

    public int getCoinParity(Player player, Board board) {
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

    public int getBestPlayer(int ourPlayer, Board board) {
        int bestPlayer = ourPlayer;
        int bestEvaluation = Integer.MIN_VALUE;

        for (Player player : players) {
            if (!player.isDisqualified()) {
                Move emptyMove = new Move();
                int evaluation = getEvaluationForPlayer(player, board, emptyMove);

                if (evaluation > bestEvaluation) {
                    bestEvaluation = evaluation;
                    bestPlayer = player.getIntNumber();
                }
            }
        }

        return bestPlayer;
    }
}
