import java.util.HashMap;
import java.util.List;

public class Game {

    private Player[] players;
    private Board board;

    public Game(List<String> initMap) {
        createPlayers(initMap);
        createBoard(initMap);
        System.out.println(toString());
        board.executeMove(4, 2, '1');
    }

    private void createPlayers(List<String> initMap) {
        int playerAmount = Integer.parseInt(initMap.get(0));
        int overrideStone = Integer.parseInt(initMap.get(1));

        String[] bombInfo = initMap.get(2).split(" ");
        int bombAmount = Integer.parseInt(bombInfo[0]);

        players = new Player[playerAmount];
        for (int i = 0; i < playerAmount; i++) {
            players[i] = new Player(overrideStone, bombAmount);
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
        HashMap<String, Transition> transitions = createTransitions(initMapTransitions);

        board = new Board(mapField, transitions, bombRadius);
    }

    private HashMap<String, Transition> createTransitions(List<String> initMapTransitions) {
        HashMap<String, Transition> transitions = new HashMap<>();
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

            String transPos1 = x1 + " " + y1 + " " + r1;
            transitions.put(transPos1, transition);

            String transPos2 = x2 + " " + y2 + " " + r2;
            transitions.put(transPos2, transition);
        }

        return transitions;
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

        HashMap<String, Transition> transitions = board.getTransition();
        gameString.append(transitions);

        return gameString.toString();
    }
}
