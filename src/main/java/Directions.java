import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Directions {
    int [] LEFT = {-1, 0};
    int [] RIGHT = {1, 0};
    int [] UP = {0, -1};
    int [] DOWN = {0, 1};
    int [] UP_LEFT = {-1, -1};
    int [] UP_RIGHT = {1, -1};
    int [] DOWN_LEFT = {-1, 1};
    int [] DOWN_RIGHT = {1, 1};

    private List<int[]> dirList;

    public Directions () {
        dirList = new ArrayList<>(Arrays.asList(LEFT, RIGHT, UP, DOWN, UP_LEFT,
                UP_RIGHT, DOWN_LEFT, DOWN_RIGHT));
    }

    public int[] getDirectionFromNum(int num) {
        switch (num)
        {
            case 0: return UP;
            case 1: return UP_RIGHT;
            case 2: return RIGHT;
            case 3: return DOWN_RIGHT;
            case 4: return DOWN;
            case 5: return DOWN_LEFT;
            case 6: return LEFT;
            case 7: return UP_LEFT;
            default:
                System.out.println("Invalid direction (" + num + ")");
                return null;
        }
    }

    public int getNumberFromDir(int[] direction) {
        if (compare(UP, direction)) return 0;
        if (compare(UP_RIGHT, direction)) return 1;
        if (compare(RIGHT, direction)) return 2;
        if (compare(DOWN_RIGHT, direction)) return 3;
        if (compare(DOWN, direction)) return 4;
        if (compare(DOWN_LEFT, direction)) return 5;
        if (compare(LEFT, direction)) return 6;
        if (compare(UP_LEFT, direction)) return 7;
        return -1;
    }

    private boolean compare(int[] a, int [] b) {
        if (a[0] == b[0] && a[1] == b[1]) return true;
        return false;
    }

    public List<int[]> getDirectionList() {
        return dirList;
    }
}
