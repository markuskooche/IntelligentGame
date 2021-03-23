import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Game {
    private List<Player> players;
    private Map map;

    public Game(List<String> initMap) {
        players = createPlayers(initMap);
        map = createMap(initMap);
        map.printMap();
    }

    private Map createMap(List<String> initMap) {
        int hight = Integer.valueOf(initMap.get(3).substring(0, 2));
        int width = Integer.valueOf(initMap.get(3).substring(3, 5));
        int bombRadius = Integer.valueOf(initMap.get(2).substring(2, 3));
        String [][] mapField = new String [hight][width];
        List<String> initMapField = initMap.subList(4, hight + 4);

        //Create Field
        int y = 0;
        for (String line : initMapField) {
            String [] fields = line.split(" ");
            for (int x = 0; x < width; x++) {
                mapField [y][x] = fields[x];
            }
            y++;
        }

        //Create Transitions
        List<String> initMapTransitions = initMap.subList(hight + 4, initMap.size());
        HashMap<String, Transition> transitions = createTransitions(initMapTransitions);

        return new Map(mapField, transitions, bombRadius);
    }

    private HashMap<String, Transition> createTransitions(List<String> initMapTransitions) {
        HashMap<String, Transition> trans = new HashMap<String, Transition>();
        for (String line : initMapTransitions) {
            String [] fields = line.split(" ");
            int x1 = Integer.valueOf(fields[0]);
            int y1 = Integer.valueOf(fields[1]);
            int r1 = Integer.valueOf(fields[2]);
            int x2 = Integer.valueOf(fields[4]);
            int y2 = Integer.valueOf(fields[5]);
            int r2 = Integer.valueOf(fields[6]);
            Transition transition = new Transition(x1, y1, r1, x2, y2, r2);

            String trans_pos_1 = fields[0] + " " + fields[1] + " " + fields[2];
            trans.put(trans_pos_1, transition);

            String trans_pos_2 = fields[4] + " " + fields[5] + " " + fields[6];
            trans.put(trans_pos_2, transition);
        }
        return trans;
    }

    private List<Player> createPlayers(List<String> initMap) {
        List<Player> players = new ArrayList<>();
        int numPlayer = Integer.valueOf(initMap.get(0));
        int changeStone = Integer.valueOf(initMap.get(1));
        int numBomb = Integer.valueOf(initMap.get(2).substring(0, 1));

        for (int i = 0; i < numPlayer; i++) {
            Player player = new Player(numBomb, changeStone, true);
            players.add(player);
        }
        return players;
    }
}
