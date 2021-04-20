package controller;

import heuristic.Heuristics;
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

    private Player[] players;
    private Board board;
    private Heuristics heuristics;
    private MapAnalyzer mapAnalyzer;
    private int ourPlayerNumber;

    public Game(List<String> initMap, int ourPlayerNumber) {
        createPlayers(initMap);
        createBoard(initMap);
        this.ourPlayerNumber = ourPlayerNumber;
        mapAnalyzer = new MapAnalyzer(board);
        heuristics = new Heuristics(board, players, mapAnalyzer);
        System.out.println("----Ergebnis: " + heuristics.getEvaluationForPlayer(players[0]));
        System.out.println("----Ergebnis: " + heuristics.getEvaluationForPlayer(players[1]));
        System.out.println(mapAnalyzer.toString());
    }

    public Game(List<String> initMap) {
        createPlayers(initMap);
        createBoard(initMap);
        mapAnalyzer = new MapAnalyzer(board);
        heuristics = new Heuristics(board, players, mapAnalyzer);
        System.out.println("----Ergebnis: " + heuristics.getEvaluationForPlayer(players[0]));
        System.out.println("----Ergebnis: " + heuristics.getEvaluationForPlayer(players[1]));
        System.out.println(mapAnalyzer.toString());
    }

    public void setOurPlayerNumber(int ourPlayerNumber) {
        this.ourPlayerNumber = ourPlayerNumber;
    }

    public int[] executeOurMove() {
        Player p = players[ourPlayerNumber - 1];
        int [] ourMove = new int[3];
        List<Move> allMoves = board.getLegalMoves(p, true);

        Random r = new Random();
        int randomPick = r.nextInt(allMoves.size());

        Move pickedMove = allMoves.get(randomPick);
        ourMove[0] = pickedMove.getX();
        ourMove[1] = pickedMove.getY();

        if (pickedMove.isChoice()) {
            //Choose the exchange partner
            //Current: choose a random player
            ourMove[2] = r.nextInt(players.length - 1) + 1;

        } else if (pickedMove.isBonus()) {
            //Decide witch bonus we take
            ourMove[2] = 21; //Extra Overridestone
        } else {
            // Just a normal move
            ourMove[2] = 0;
        }

        return ourMove;
    }

    private void createPlayers(List<String> initMap) {
        int playerAmount = Integer.parseInt(initMap.get(0));
        int overrideStone = Integer.parseInt(initMap.get(1));

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