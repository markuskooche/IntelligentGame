import java.util.HashMap;
import java.util.List;

public class Map {

    private String [][] field;
    private final int bombRadius;
    private HashMap<String, Transition> transitions;

    public Map(String [][] field, HashMap<String, Transition> transitions, int bombRadius) {
        this.field = field;
        this.transitions = transitions;
        this.bombRadius = bombRadius;
    }

    public void printMap() {
        for (int i = 0; i < field.length; i++) {
            for (int j = 0; j < field[0].length; j++) {
                System.out.print(" " + field[i][j]);
            }
            System.out.println();
        }
    }

    public HashMap<String, Transition> getMap() {
        return transitions;
    }
}
