import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.List;

public class Board {

    private final HashMap<Integer, Transition> transitions;
    private final int playerAmount;
    private final int bombRadius;

    private char[][] field;
    private final int width;
    private final int height;

    public Board(char[][] field, HashMap<Integer, Transition> transitions, int playerAmount, int bombRadius) {
        this.playerAmount = playerAmount;
        this.transitions = transitions;
        this.bombRadius = bombRadius;
        this.field = field;

        this.height = field.length;
        this.width = field[0].length;
    }

    public void choice() {
        System.out.print("Please enter two Players you would like to change: ");
        Scanner scanner = new Scanner(System.in);

        char a = scanner.next().charAt(0);
        char b = scanner.next().charAt(0);

        choice(a, b);
    }

    public void choice(char a, char b) {
        if (a != b) {
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    if (field[i][j] == a) {
                        field[i][j] = b;
                    } else if (field[i][j] == b) {
                        field[i][j] = a;
                    }
                }
            }
        }
    }

    public void inversion() {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {

                // '1' is 49 in ASCII   '9' is 59 in ASCII
                if (49 <= field[i][j] && 59 >= field[i][j]) {
                    int newPlayer = field[i][j] % playerAmount + 1;

                    // the addition changes the int into an ASCII value
                    field[i][j] = (char) (newPlayer + '0');
                }
            }
        }
    }

    public void printTransition() {
        int heightCopy = height + 2;
        int widthCopy = width + 2;
        char[][] fieldCopy = new char[heightCopy][widthCopy];

        for (int h = 0; h < heightCopy; h++) {
            for (int w = 0; w < widthCopy; w++) {
                fieldCopy[h][w] = '-';
            }
        }

        for (int h = 1; h < (heightCopy - 1); h++) {
            for (int w = 1; w < (widthCopy - 1); w++) {
                fieldCopy[h][w] = field[h - 1][w - 1];
            }
        }

        for (Transition transition : transitions.values()) {
            int x1 = transition.getX1();
            int y1 = transition.getY1();
            int r1 = transition.getR1();
            int x2 = transition.getX2();
            int y2 = transition.getY2();
            int r2 = transition.getR2();

            if (fieldCopy[y1 + 1][x1 + 1] == '-') {
                System.out.println("ERROR: x1=" + x1 + " || y1=" + y1);
            } else {
                fieldCopy[y1 + 1][x1 + 1] = '©';
            }

            if (fieldCopy[y2 + 1][x2 + 1] == '-') {
                System.out.println("ERROR: x2=" + x2 + " || y2=" + y2);
            } else {
                fieldCopy[y2 + 1][x2 + 1] = '©';
            }

            switch (r1) {
                case 0:
                    fieldCopy[y1][x1 + 1] = '|';
                    break;
                case 1:
                    fieldCopy[y1][x1 + 2] = '/';
                    break;
                case 2:
                    fieldCopy[y1 + 1][x1 + 2] = '=';
                    break;
                case 3:
                    fieldCopy[y1 + 2][x1 + 2] = '\\';
                    break;
                case 4:
                    fieldCopy[y1 + 2][x1 + 1] = '|';
                    break;
                case 5:
                    fieldCopy[y1 + 2][x1] = '/';
                    break;
                case 6:
                    fieldCopy[y1 + 1][x1] = '=';
                    break;
                case 7:
                    fieldCopy[y1][x1] = '\\';
                    break;
                default:
                    break;
            }

            switch (r2) {
                case 0:
                    fieldCopy[y2][x2 + 1] = '|';
                    break;
                case 1:
                    fieldCopy[y2][x2 + 2] = '/';
                    break;
                case 2:
                    fieldCopy[y2 + 1][x2 + 2] = '=';
                    break;
                case 3:
                    fieldCopy[y2 + 2][x2 + 2] = '\\';
                    break;
                case 4:
                    fieldCopy[y2 + 2][x2 + 1] = '|';
                    break;
                case 5:
                    fieldCopy[y2 + 2][x2] = '/';
                    break;
                case 6:
                    fieldCopy[y2 + 1][x2] = '=';
                    break;
                case 7:
                    fieldCopy[y2][x2] = '\\';
                    break;
                default:
                    break;
            }
        }

        for (int h = 0; h < heightCopy; h++) {
            for (int w = 0; w < widthCopy; w++) {
                System.out.print(fieldCopy[h][w] + " ");
            }
            System.out.println();
        }
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
        int dir = directions.getNumberFromDir(direction);
       // String key = xPos + " " + yPos + " " + dir;
        int key = Transition.hash(xPos, yPos, dir);
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

    public char[][] getBoard() {
        return field;
    }

    public void setPiece(int height, int width, char piece) {
        this.field[height][width] = piece;
    }

    public char getPiece(int height, int width) {
        return field[height][width];
    }

    public HashMap<Integer, Transition> getTransition() {
        return transitions;
    }

    public char[][] getField() {
        return field;
    }

    public void setField(char[][] field) {
        this.field = field;
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
