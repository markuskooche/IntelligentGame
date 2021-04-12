import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class Game {

    private Player[] players;
    private Board board;
    private Heuristics heuristics;
    private MapAnalyzer mapAnalyzer;

    public Game(List<String> initMap) {
        createPlayers(initMap);
        createBoard(initMap);
        mapAnalyzer = new MapAnalyzer(board);
        mapAnalyzer.createField();
        heuristics = new Heuristics(board, mapAnalyzer);
        System.out.println("----Ergebnis: " + heuristics.getEvaluationForPlayer(players[0]));
        System.out.println("----Ergebnis: " + heuristics.getEvaluationForPlayer(players[1]));
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

    public void executeMove(char player) {
        System.out.print(getBoard());

        board.getLegalMoves(player);
        System.out.println();

        System.out.println("PLAYER '" + player + "' SELECTED");
        System.out.print("Please enter a valid move: ");
        Scanner scanner = new Scanner(System.in);
        int x = scanner.nextInt();
        int y = scanner.nextInt();

        executeMove(x, y, player);
    }

    public void executeMove(int x, int y, char player) {
        // ASCII '1' - 49 = 0
        int index = player - 49;
        board.executeMove(x, y, players[index]);
    }

    public Player[] getPlayers() {
        return players;
    }

    public void setPlayers(Player[] players) {
        this.players = players;
    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
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
        for (Transition transition : board.getTransition().values()) {
            if (!transitionList.contains(transition)) {
                transitionList.add(transition);
                gameString.append(transition);
                gameString.append("\n");
            }
        }

        return gameString.toString();
    }
}
