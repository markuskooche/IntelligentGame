package controller;

import heuristic.BombPosition;
import heuristic.Heuristics;
import heuristic.TimeExceededException;
import loganalyze.additional.AnalyzeParser;
import map.Board;
import map.Move;
import map.Player;
import map.Transition;
import mapanalyze.MapAnalyzer;

import java.util.*;

/**
 * The Game class creates a new instance of a game when all important information is passed,
 * such as the playing field, the number of players, bomb radius, transitions and much more.
 * Subsequently, a game can control all steps of an action until disqualification or until
 * one has won or lost. This is a class that can control all other classes directly or indirectly.
 *
 * @author Benedikt Halbritter
 * @author Iwan Eckert
 * @author Markus Koch
 */
public class Game {

    private static final int ADDITIONAL_OVERRIDE = 21;
    private static AnalyzeParser analyzeParser;

    private Player[] players;
    private Board board;
    private Heuristics heuristics;
    private MapAnalyzer mapAnalyzer;
    private int ourPlayerNumber;

    public Game(List<String> initMap, AnalyzeParser analyzeParser) {
        long stopTime = System.currentTimeMillis();
        Game.analyzeParser = analyzeParser;

        createPlayers(initMap);
        createBoard(initMap);
        mapAnalyzer = new MapAnalyzer(board, players.length, analyzeParser);
        heuristics = new Heuristics(board, players, mapAnalyzer, analyzeParser);
        mapAnalyzer.createVisibleField('1');
        //System.out.println(mapAnalyzer.getBoardValues());
    }

    public void setOurPlayerNumber(int ourPlayerNumber) {
        this.ourPlayerNumber = ourPlayerNumber;
    }

    public int[] executeOurMoveTime(int time, boolean alphaBeta, boolean moveSorting) {
        Player ourPlayer = players[ourPlayerNumber - 1];
        Move move = heuristics.getMoveByTime(ourPlayer, time, alphaBeta, moveSorting);
        int additional = getAdditional(move);

        board.colorizeMove(move, ourPlayer, additional);
        return new int[] {move.getX(), move.getY(), additional};
    }

    public int[] executeOurMoveDepth(int depth, boolean alphaBeta, boolean moveSorting) {
        Player ourPlayer = players[ourPlayerNumber - 1];
        Move move = heuristics.getMoveByDepth(ourPlayer, depth, alphaBeta, moveSorting);
        int additional = getAdditional(move);

        board.colorizeMove(move, ourPlayer, additional);
        return new int[] {move.getX(), move.getY(), additional};
    }

    /**
     *
     *
     * @param move
     * @return
     */
    private int getAdditional(Move move) {
        int additional = 0;

        if (move.isChoice()) {
            additional = heuristics.getBestPlayer(ourPlayerNumber);
            mapAnalyzer.activateSpecialStone(move.getX(), move.getY(), 'c');
        } else if (move.isBonus()) {
            // always choosing an overridestone
            additional = ADDITIONAL_OVERRIDE;
            mapAnalyzer.activateSpecialStone(move.getX(), move.getY(), 'b');
        } else if (move.isInversion()) {
            mapAnalyzer.activateSpecialStone(move.getX(), move.getY(), 'i');
        }

        return additional;
    }

    private void createPlayers(List<String> initMap) {
        String playerAmountString = initMap.get(0).replaceAll(" ", "");
        int playerAmount = Integer.parseInt(playerAmountString);
        String overrideStoneString = initMap.get(1).replaceAll(" ", "");
        int overrideStone = Integer.parseInt(overrideStoneString);

        String[] bombInfo = initMap.get(2).split(" ");
        int bombAmount = Integer.parseInt(bombInfo[0]);

        players = new Player[playerAmount];
        for (int i = 0; i < playerAmount; i++) {
            players[i] = new Player((i+1), bombAmount, overrideStone);
        }
    }

    private void createBoard(List<String> initMap) {
        String[] bombInfo = initMap.get(2).split(" ");
        int bombRadius = Integer.parseInt(bombInfo[1]);

        String[] sizeInfo = initMap.get(3).split(" ");
        int height = Integer.parseInt(sizeInfo[0]);
        int width = Integer.parseInt(sizeInfo[1]);

        char[][] mapField = new char[height][width];

        // Create Field
        for (int y = 0; y < height; y++) {
            String[] fields = initMap.get(y + 4).split(" ");

            for (int x = 0; x < width; x++) {
                mapField [y][x] = fields[x].charAt(0);
            }
        }

        // Create Transitions
        List<String> initMapTransitions = initMap.subList(height + 4, initMap.size());
        HashMap<Integer, Transition> transitions = createTransitions(initMapTransitions);

        board = new Board(mapField, transitions, players.length, bombRadius);
    }

    private HashMap<Integer, Transition> createTransitions(List<String> initMapTransitions) {
        HashMap<Integer, Transition> transitions = new HashMap<>();
        int x1, y1, r1, x2, y2, r2;

        for (String line : initMapTransitions) {
            String[] fields = line.split(" ");

            x1 = Integer.parseInt(fields[0]);
            y1 = Integer.parseInt(fields[1]);
            r1 = Integer.parseInt(fields[2]);
            x2 = Integer.parseInt(fields[4]);
            y2 = Integer.parseInt(fields[5]);
            r2 = Integer.parseInt(fields[6]);

            int transitionKeyOne = Transition.hash(x1, y1, r1);
            Transition transitionOne = new Transition(x2, y2, r2);
            transitions.put(transitionKeyOne, transitionOne);

            int transitionKeyTwo = Transition.hash(x2, y2, r2);
            Transition transitionTwo = new Transition(x1, y1, r1);
            transitions.put(transitionKeyTwo, transitionTwo);
        }

        return transitions;
    }

    /**
     * Executes a move for a passed position and a passed player.
     *
     * @param x integer of the x coordinate
     * @param y integer of the y coordinate
     * @param player char representation of the player
     *
     * @see Board
     */
    public void executeMove(int x, int y, int player, int additionalOperation) {
        Player currentPlayer = getPlayer(player);

        // get all legal moves of the player
        List<Move> legalMoves = board.getLegalMoves(currentPlayer, true);
        int[] selectedMove = new int[] {x, y};

        // searches for the selected move
        for (Move legalMove : legalMoves) {
            // colorize board if it is the correct move
            if (legalMove.isMove(selectedMove)) {
                board.colorizeMove(legalMove, currentPlayer, additionalOperation);
                break;
            }
        }
    }

    /**
     * Returns the player class by the passed number.
     *
     * @param number number of the player
     *
     * @return a player class
     *
     * @see Player
     */
    public Player getPlayer(int number) {
        if (number <= players.length) {
            return players[number - 1];
        }

        return null;
    }

    /**
     * Execute a bomb move.
     *
     * @param x x coordinate
     * @param y y coordinate
     */
    public void executeBomb(int x, int y) {
        board.executeBomb(x, y);
    }

    public int[] executeOurBomb() {
        int radius = board.getBombRadius();
        Player player = getPlayer(ourPlayerNumber);
        char playerNumber = player.getNumber();

        BombPosition bomb = new BombPosition(board.getField(), playerNumber, radius);
        int[] position = bomb.getBestBombPosition();
        board.executeBomb(position[0], position[1]);
        player.decreaseBomb();

        return position;
    }

    /**
     * Get the current mobility of a specific player.
     *
     * @param player the number of a player
     * @returns the mobility value
     *
     * @see Heuristics
     */
    public int getMobility(int player) {
        Player selectedPlayer = getPlayer(player);
        if (selectedPlayer == null) {
            return Integer.MIN_VALUE;
        }

        return heuristics.getMobility(selectedPlayer, board);
    }

    /**
     * Get the current coin parity of a specific player.
     *
     * @param player the number of a player
     * @returns the coin parity
     *
     * @see Heuristics
     */
    public int getCoinParity(int player) {
        Player selectedPlayer = getPlayer(player);
        if (selectedPlayer == null) {
            return Integer.MIN_VALUE;
        }

        return heuristics.getCoinParity(selectedPlayer, board);
    }

    /**
     * Get the current map value of a specific player.
     *
     * @param player the number of a player
     * @returns the map value
     *
     * @see Heuristics
     */
    public int getMapValue(int player) {
        Player selectedPlayer = getPlayer(player);
        if (selectedPlayer == null) {
            return Integer.MIN_VALUE;
        }

        return heuristics.getMapValue(selectedPlayer, board);
    }

    /**
     * Get the current whole heuristic value of a specific player.
     *
     * @param player the number of a player
     * @returns the heuristic value
     *
     * @see Heuristics
     */
    public int getHeuristic(int player) {
        Player selectedPlayer = getPlayer(player);
        if (selectedPlayer == null) {
            return Integer.MIN_VALUE;
        }

        Move emptyMove = new Move();
        return heuristics.getEvaluationForPlayer(selectedPlayer, board, emptyMove);
    }


    public void decreasePlayerNumber(){
        mapAnalyzer.setPlayerNumber(mapAnalyzer.getPlayerNumber()-1);
    }

    /**
     * Returns a list of all players.
     *
     * @return a list of all players
     *
     * @see Player
     */
    public Player[] getPlayers() {
        return players;
    }

    /**
     * Returns the current board.
     *
     * @return the current board
     *
     * @see Board
     */
    public Board getBoard() {
        return board;
    }

    /**
     * Get the 2D reachable field from the current map.
     *
     * @return 2D int array from the reachable field
     */
    public int[][] getReachableField() {
        return mapAnalyzer.getReachableField();
    }

    public void initializeReachableField() {
        try {
            mapAnalyzer.startReachableField(false, null);
        }
        catch (TimeExceededException ignored) {
        }
    }

    /**
     * Check if the reachable field could be created.
     *
     * @return true if reachable field has been created
     */
    public boolean isReachableFinished() {
        return mapAnalyzer.isReachableFinished();
    }

    /**
     * Returns an array with all transitions.
     *
     * @return String array with all connected transitions
     */
    public String[] getTransitions() {
        // gets a collection of all transition values
        Collection<Transition> transitions = board.getAllTransitions().values();

        // creates a string array to store all transitions
        String[] returnTransitions = new String[transitions.size() / 2];

        // a list to safe read transitions
        ArrayList<Transition> list = new ArrayList<>();
        int counter = 0;

        // iterations over all transitions
        for (Transition transition : transitions) {
            int x = transition.getX();
            int y = transition.getY();
            int r = transition.getR();

            // receives the associated transition
            Transition opposite = board.getTransition(x, y, r);

            // if none of these transitions were handled, they will be added
            if (!list.contains(transition) || !list.contains(opposite)) {
                // added the string to the array
                returnTransitions[counter++] = (transition + " <-> " + opposite);

                // stores read transitions
                list.add(transition);
                list.add(opposite);
            }
        }

        return returnTransitions;
    }


    @Override
    public String toString() {
        StringBuilder gameString = new StringBuilder();

        gameString.append(String.format("Anzahl der Spieler: %s\n", players.length));
        gameString.append(String.format("Anzahl der Überschreibsteine: %s\n", players[0].getOverrideStone()));
        gameString.append(String.format("Anzahl der Bomben: %s\n", players[0].getBomb()));
        gameString.append(String.format("Stärke der Bomben: %s\n", board.getBombRadius()));

        gameString.append(String.format("\nZeilenanzahl des Spielfelds: %s\n", board.getHeight()));
        gameString.append(String.format("Spaltenanzahl des Spielfelds: %s\n\n", board.getWidth()));

        gameString.append(String.format("%s\n", board.toString()));

        for (String transition : getTransitions()) {
            gameString.append(transition);
        }

        return gameString.toString();
    }
}
