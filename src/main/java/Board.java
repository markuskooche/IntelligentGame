import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Board {

    private char[][] field;
    private final int bombRadius;
    private final HashMap<String, Transition> transitions;
    private final int width;
    private final int height;

    public Board(char[][] field, HashMap<String, Transition> transitions, int bombRadius) {
        this.field = field;
        this.transitions = transitions;
        this.bombRadius = bombRadius;

        this.height = field.length;
        this.width = field[0].length;
    }

    public void executeMove(int xPos, int yPos, char player) {
        List<int[]> moves = checkMove(xPos, yPos, player);

        if (!moves.isEmpty()) {
            moves.add(new int[] {xPos, yPos});
        }

        for (int[] position : moves) {
            int x = position[0];
            int y = position[1];
            field[y][x] = player;
        }
    }

    public List<int[]> checkMove(int xPos, int yPos, char player) {
        List<int[]> fieldsToMark = new ArrayList<>();
        List<int[]> tmp = new ArrayList<>();

        if (!isPickedPositionValid(xPos, yPos, player)) {
            System.out.println("Invalid Picked Move Position: " + xPos + " " + yPos + " " + "Player " + player);
            return fieldsToMark;
        }

        List<int[]> directions = new Directions().getDirectionList();
        for (int [] dir : directions) {
            fieldsToMark.addAll(checkDirection(dir, xPos, yPos, player, tmp));
            tmp.clear();
        }
        return fieldsToMark;
    }

    private List<int[]> checkDirection(int [] direction, int xPos, int yPos, char player, List<int[]> tmp) {
        Character[] notEnemyOrMe = {'-', '0', 'b', 'c', 'i'};

        int x = xPos;
        int y = yPos;
        while (true) {
            //Check for Transition
            List<int[]> destTransition = getTransitionFromField(direction, x, y);
            if (!destTransition.isEmpty()) {
                int [] newDir = destTransition.get(1);
                int xNew = destTransition.get(0)[0] - newDir[0]; //will be added above again
                int yNew = destTransition.get(0)[1] - newDir[1];
                checkDirection(newDir, xNew, yNew,player, tmp);
                break;
            }

            x += direction[0];
            y += direction[1];

            if (x < 0 || x >= width || y < 0 || y >= height) {
                tmp.clear();
                break;
            }
            if (field[y][x] == player) {
                break;
            }
            if (isThisPositionValid(x, y, notEnemyOrMe)) {
                int[] posToMark = {x, y};
                tmp.add(posToMark);
            } else {
                tmp.clear();
                break;
            }
        }
        return tmp;
    }

    /**
     * Returns the destination position form a field with a transition as list
     * Index: 0 = destination position
     * Index: 1 = out coming direction when using the transition
     */
    private List<int[]> getTransitionFromField(int [] direction, int xPos, int yPos) {
        Directions directions = new Directions();
        String dir = "" + directions.getNumberFromDir(direction);
        String key = xPos + " " + yPos + " " + dir;
        Transition transition = transitions.get(key);
        List<int[]> destination = new ArrayList<>();

        if (transition != null) {
            int [] dest = null;
            int [] newDir = null;
            if (transition.getPos1()[0] == xPos && transition.getPos1()[1] == yPos) {
                dest = transition.getPos2();
                newDir = directions.getDirectionFromNum(dest[2]);
                //Invert the direction
                newDir[0] = newDir[0] * -1;
                newDir[1] = newDir[1] * -1;
            }
            if (transition.getPos2()[0] == xPos && transition.getPos2()[1] == yPos) {
                dest = transition.getPos1();
                newDir = directions.getDirectionFromNum(dest[2]);
                //Invert the direction
                newDir[0] = newDir[0] * -1;
                newDir[1] = newDir[1] * -1;
            }
            destination.add(dest);
            destination.add(newDir);
        }
        return destination;
    }

    /**
     * Compare the given field(xPos, yPos) with invalidChars. <br>
     * Returns true, if invalidChars does not contain the field
     */
    private boolean isThisPositionValid(int xPos, int yPos, Character[] invalidChars) {
        if (xPos < 0 || xPos >= width || yPos < 0 || yPos >= height) {
            return false;
        }
        char fieldToCheck = field[yPos][xPos];
        List<Character> invalidFields = new ArrayList<>(Arrays.asList(invalidChars));

        if (!invalidFields.contains(fieldToCheck)) {
            return true;
        }
        return false;
    }

    private boolean isPickedPositionValid(int xPos, int yPos, char player) {
        char fieldPos = field[yPos][xPos];
        if (fieldPos != '-' && fieldPos != player) {
            if (fieldPos == '0' || fieldPos == 'b' || fieldPos == 'c' || fieldPos == 'i') {
                return true;
            }
        }

        return false;
    }

    public int getBombRadius() {
        return bombRadius;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public void setPiece(int height, int width, char piece) {
        this.field[height][width] = piece;
    }

    public char getPiece(int height, int width) {
        return field[height][width];
    }

    public HashMap<String, Transition> getTransition() {
        return transitions;
    }

    @Override
    public String toString() {
        StringBuilder boardString = new StringBuilder();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                boardString.append(field[y][x]);
                boardString.append(" ");
            }
            boardString.append("\n");
        }

        return boardString.toString();
    }
}
