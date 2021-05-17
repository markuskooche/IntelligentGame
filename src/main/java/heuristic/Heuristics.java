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
    private long spentTime;
    private long zeit;
    private static AnalyzeParser analyzeParser;

    private Token timeToken;

    public Heuristics(Board board, Player[] players, MapAnalyzer mapAnalyzer, AnalyzeParser analyzeParser) {
        this.board = board;
        this.players = players;
        numPlayers = players.length;
        this.mapAnalyzer = mapAnalyzer;
        this.height = board.getHeight();
        this.width = board.getWidth();
        Heuristics.analyzeParser = analyzeParser;
    }

    private void startTimer(long maxTimeForMove) {
        timeToken = new Token();
        Thread t = new Thread(new ThreadTimer(timeToken));
        Timer timer = new Timer();
        timer.schedule(new TimeOutTask(t, timer), (int)(maxTimeForMove * 0.9));
        t.start();
    }

    public Move getMoveTimeLimited(Player ourPlayer, long maxTimeForMove, boolean orderMoves, boolean alphaBeta) {
        startTimer(maxTimeForMove);
        Move move;
        int searchDepth = 2;
        int ourPlayerNum = ourPlayer.getNumber() - '0';
        int nextPlayerNum = (ourPlayerNum % numPlayers) + 1;

        Board startBoard = new Board(board.getField(), board.getAllTransitions(), numPlayers, board.getBombRadius());
        List<BoardMove> executedStartMoves = executeAllMoves(ourPlayer,startBoard, false);

        if (executedStartMoves.isEmpty()) {
            //No normal moves found, check for overrideStone-Moves
            return onlyOverrideStoneTimeLimited(startBoard, ourPlayer);
        }
        if (executedStartMoves.size() == 1) { //OverrideStones are not included
            return executedStartMoves.get(0).getMove();
        }
        move = executedStartMoves.get(0).getMove(); //Pick the first possible Move

        if (orderMoves) sortExecutedMoves(executedStartMoves, ourPlayer, ourPlayer);

        //Get best Move for depth 1
        SearchNode root = new SearchNode(null, startBoard, executedStartMoves, ourPlayer, true, 1);
        int pickedBoardValue = Integer.MIN_VALUE;
        for (BoardMove boardMove : executedStartMoves) {
            Board b = boardMove.getBoard();
            Move m = boardMove.getMove();
            int tmpValue = getEvaluationForPlayer(ourPlayer, b, m);
            if (tmpValue > pickedBoardValue) {
                pickedBoardValue = tmpValue;
                move = boardMove.getMove();
            }
            if (timeToken.timeExceeded()) return move;
        }
        root.setPickedBoardValue(pickedBoardValue);

        while(!timeToken.timeExceeded()) {
            mapsAnalyzed = 0;
            Move tmpMove = new Move();
            try {
                int depth = 1;
                int value = Integer.MIN_VALUE;
                int maxLoop = 0;

                int alpha = Integer.MIN_VALUE;
                int beta = Integer.MAX_VALUE;
                for (BoardMove boardMove : executedStartMoves) {
                    mapsAnalyzed++;
                    int tmpValue  = 0;
                    tmpValue = searchParanoidTimeLimited(root, nextPlayerNum, ourPlayerNum, boardMove.getBoard(), boardMove.getMove(), depth, searchDepth, maxLoop,
                            alpha, beta, alphaBeta, orderMoves);

                    if (tmpValue > value) {
                        value = tmpValue;
                        tmpMove = boardMove.getMove();
                        if (alphaBeta) alpha = tmpValue;
                    }
                    depth = 1;
                }

                move = tmpMove;
                searchDepth ++;
            } catch (TimeExceededException e) {
                return move;
            }
        }
        return move;
    }

    private int searchParanoidTimeLimited(SearchNode root, int currPlayer, int ourPlayerNum, Board board, Move move, int depth, int maxDepth, int maxLoop,
                               int alpha ,int beta, boolean alphaBeta, boolean orderMoves) throws TimeExceededException {

        if (timeToken.timeExceeded()) throw new TimeExceededException();

        Player player = players[currPlayer - 1];
        Player ourPlayer = players[ourPlayerNum - 1];
        SearchNode node = null;
        depth++;

        if(depth > maxDepth) return getEvaluationForPlayer(ourPlayer, board, move);

        // Check if we already have all moves for this depth and board
        List<BoardMove> executedMoves = new ArrayList<>();
        if (!root.getNextChilds().isEmpty()) {
            List<SearchNode> nextChilds = root.getNextChilds();
            for (SearchNode child: nextChilds) {
                if (child.getBoard() == board) {
                    executedMoves = child.getExecutedMoves();
                    node = child;
                    break;
                }
            }
        }
        if (node == null) executedMoves = executeAllMoves(player, board, false);


        // No moves for given player -> another player should move
        // Or the player is disqualified
        if (executedMoves.isEmpty() || player.isDisqualified()) {
            int nextPlayer = (currPlayer % numPlayers) + 1;
            if(maxLoop < numPlayers) {
                maxLoop++;
                return searchParanoidTimeLimited(root, nextPlayer, ourPlayerNum, board, move,depth - 1, maxDepth, maxLoop,
                        alpha, beta, alphaBeta, orderMoves);
            } else {
                return 0; //No player has any move (perhaps only override)
            }
        }

        if (orderMoves && depth < (maxDepth - 1)) sortExecutedMoves(executedMoves, ourPlayer, player);

        int value;
        if (player == ourPlayer) {
            value = Integer.MIN_VALUE; //MAX
            if (node == null) {
                node = new SearchNode(root, board, executedMoves, player, true, depth);
                root.addNextChild(node);
            }
        } else {
            value = Integer.MAX_VALUE; //MIN
            if (node == null) {
                node = new SearchNode(root, board, executedMoves, player, false, depth);
                root.addNextChild(node);
            }
        }

        int tmpValue;
        int nextPlayer = (currPlayer % numPlayers) + 1;
        for (BoardMove boardMove : executedMoves) {
            mapsAnalyzed++;
            tmpValue = searchParanoidTimeLimited(node, nextPlayer, ourPlayerNum, boardMove.getBoard(), boardMove.getMove(), depth, maxDepth, maxLoop,
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
            if (timeToken.timeExceeded()) throw new TimeExceededException();
        }

        return value;
    }

    public Move getMoveParanoid(Player player, int searchDepth, boolean alphaBeta, boolean orderMoves) {
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
        // TODO: [IWAN] bitte in AnalyseParser aufrufen [[ System.out.println("Analyzed Maps: " + mapsAnalyzed); ]]
        // TODO: [IWAN] bitte in AnalyseParser aufrufen [[ System.out.println("XT01-98-AM-" + mapsAnalyzed); ]]
        return move;
    }

    private Move onlyOverrideStone(Board board, Player player) {
        List<BoardMove> executedStartMoves = executeAllMoves(player,board, true);
        int value = Integer.MIN_VALUE;
        Move move = new Move();
        for (BoardMove boardMove : executedStartMoves) {
            mapsAnalyzed++;
            int tmpValue = getCoinParity(player, boardMove.getBoard());
            if (tmpValue >= value) {
                value = tmpValue;
                move = boardMove.getMove();
            }
        }

        return move;
    }

    private Move onlyOverrideStoneTimeLimited(Board board, Player player) {
        Move move;
        List<BoardMove> executedStartMoves = executeAllMoves(player,board, true);
        move = executedStartMoves.get(0).getMove(); //Pick the first possible Move
        int value = Integer.MIN_VALUE;

        for (BoardMove boardMove : executedStartMoves) {
            if (timeToken.timeExceeded()) return move;
            mapsAnalyzed++;
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
            int additionalInformation = getAdditionalInfo(m, player);
            newBoard.colorizeMove(m, player, additionalInformation);

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
        return value;
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
        return (int) (tmpMapValue * 100000);
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
        return (int) (result * 100000);
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

        return (int) (result * 100000);
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
