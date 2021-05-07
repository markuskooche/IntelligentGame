package controller;

import heuristic.Heuristics;
import loganalyze.additional.AnalyzeParser;
import map.Board;
import map.Move;
import map.Player;
import map.Transition;
import mapanalyze.MapAnalyzer;

import java.util.*;

/**
 * The controller.Game class creates a new instance of a game when all important information is passed,
 * such as the playing field, the number of players, bomb radius, transitions and much more.
 * Subsequently, a game can control all steps of an action until disqualification or until
 * one has won or lost. This is a class that can control all other classes directly or indirectly.
 *
 * @author Benedikt Halbritter
 * @author Iwan Eckert
 * @author Markus Koch
 */
public class Game {

    private static AnalyzeParser analyzeParser;

    private Player[] players;
    private Board board;
    private Heuristics heuristics;
    private MapAnalyzer mapAnalyzer;
    private int ourPlayerNumber;

    /*
    public Game(List<String> initMap, int ourPlayerNumber) {
        createPlayers(initMap);
        createBoard(initMap);
        this.ourPlayerNumber = ourPlayerNumber;
        mapAnalyzer = new MapAnalyzer(board, players.length);
        heuristics = new Heuristics(board, players, mapAnalyzer);
        // TODO: [Benedikt] System.out.println(mapAnalyzer.toString());
        // TODO: [Benedikt] System.out.println(mapAnalyzer.getBoardValues());
        //executeOurMove(1);
    }*/

    public Game(List<String> initMap, AnalyzeParser analyzeParser) {
        Game.analyzeParser = analyzeParser;

        createPlayers(initMap);
        createBoard(initMap);
        mapAnalyzer = new MapAnalyzer(board, players.length, analyzeParser);
        heuristics = new Heuristics(board, players, mapAnalyzer, analyzeParser);
        // TODO: [Benedikt] System.out.println(mapAnalyzer.toString());
        // TODO: [Benedikt] System.out.println(mapAnalyzer.getBoardValues());
    }

    public void setOurPlayerNumber(int ourPlayerNumber) {
        this.ourPlayerNumber = ourPlayerNumber;
    }

    public int[] executeOurMoveDepth(int depth, boolean alphaBeta) {
        Player ourPlayer = players[ourPlayerNumber - 1];
        int [] ourMove = new int[3];
        long time = System.currentTimeMillis();
        Move move = heuristics.getMoveParanoid(ourPlayer, depth, alphaBeta);
        // TODO: [Iwan] System.out.println("Time for Move: " + (System.currentTimeMillis() - time) + " ms");
        ourMove[0] = move.getX();
        ourMove[1] = move.getY();

        if (move.isChoice()) {
            Random r = new Random();
            ourMove[2] = r.nextInt(players.length - 1) + 1;
        } else if (move.isBonus()) {
            ourMove[2] = 21; //Extra Overridestone
        } else {
            ourMove[2] = 0; // Just a normal move
        }
        // TODO: [Iwan] System.out.println("X: " + ourMove[0] + " Y: " + ourMove[1] + " special: " + ourMove[2]);

        if (move.isOverride()) {
            board.executeMove(ourMove[0], ourMove[1], ourPlayer, ourMove[2], true);
        } else {
            board.executeMove(ourMove[0], ourMove[1], ourPlayer, ourMove[2], false);
        }
        return ourMove;
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
        board.executeMove(x, y, players[player - 1], additionalOperation, true);
    }

    /**
     * Returns the player class by the passed number.
     *
     * @param number char representation of the player
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

    public void executeBomb(int x, int y) {
        board.executeBomb(x, y);
    }

    public int getMobility(int player) {
        return heuristics.getMobility(players[player - 1], board);
    }

    public int getCoinParity(int player) {
        return heuristics.getCoinParity(players[player - 1], board);
    }

    public int getMapValue(int player) {
        return heuristics.getMapValue(players[player - 1], board);
    }

    public int getHeuristic(int player) {
        return heuristics.getEvaluationForPlayerStatistic(players[player - 1], board);
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

    public int[][] getReachableField() {
        return mapAnalyzer.getReachableField();
    }

    public boolean isReachableFinished() {
        return mapAnalyzer.isReachableFinished();
    }

    public String[] getTransitions() {
        Collection<Transition> transitions = board.getAllTransitions().values();
        String[] returnTransitions = new String[transitions.size() / 2];
        ArrayList<Transition> list = new ArrayList<>();
        int counter = 0;

        for (Transition transition : transitions) {
            int x = transition.getX();
            int y = transition.getY();
            int r = transition.getR();

            Transition opposite = board.getTransition(x, y, r);

            if (!list.contains(transition) || !list.contains(opposite)) {
                returnTransitions[counter] = (transition + " <-> " + opposite);
                counter++;
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

        ArrayList<Transition> list = new ArrayList<>();

        for (Transition transition : board.getAllTransitions().values()) {
            int x = transition.getX();
            int y = transition.getY();
            int r = transition.getR();

            Transition opposite = board.getTransition(x, y, r);

            if (!list.contains(transition) || !list.contains(opposite)) {
                gameString.append(transition + " <-> " + opposite + "\n");
                list.add(transition);
                list.add(opposite);
            }
        }

        return gameString.toString();
    }
}
