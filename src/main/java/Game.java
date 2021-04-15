import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    private Player[] players;
    private Board board;
    private Heuristics heuristics;
    private MapAnalyzer mapAnalyzer;

    public Game(List<String> initMap) {
        createPlayers(initMap);
        createBoard(initMap);
        mapAnalyzer = new MapAnalyzer(board);
        heuristics = new Heuristics(board, players, mapAnalyzer);
        System.out.println("----Ergebnis: " + heuristics.getEvaluationForPlayer(players[0]));
        System.out.println("----Ergebnis: " + heuristics.getEvaluationForPlayer(players[1]));
        System.out.println(mapAnalyzer.toString());
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

            Transition transition = new Transition(x1, y1, r1, x2, y2, r2);

            int transPos1 = Transition.hash(x1, y1, r1);
            transitions.put(transPos1, transition);

            int transPos2 = Transition.hash(x2, y2, r2);
            transitions.put(transPos2, transition);
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
    public void executeMove(int x, int y, int player) {
        // ASCII '1' - 49 = 0
        board.executeMove(x, y, players[player - 1], true);
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
        if (number < players.length) {
            return players[number - 1];
        }

        return null;
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

        ArrayList<Transition> transitionList = new ArrayList<>();
        for (Transition transition : board.getAllTransitions().values()) {
            if (!transitionList.contains(transition)) {
                transitionList.add(transition);
                gameString.append(transition);
                gameString.append("\n");
            }
        }

        return gameString.toString();
    }
}
