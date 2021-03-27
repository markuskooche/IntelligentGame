import java.util.HashMap;
import java.util.Scanner;

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

    /*
    private boolean inversion = false;

    // private weil man sonst inversion außerhalb ändern könnte
    private List checkMove() {
        sdkfjasdjf;

        inversion = true;
    }

    public void executeMove() {
        ajdlf;

        if (inversion) {
            kajdflkj;
            inversion = false;
        }
    }
    */

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
                fieldCopy[h][w] = field[h-1][w-1];
            }
        }

        for (Transition transition : transitions.values()) {
            int x1 = transition.getX1();
            int y1 = transition.getY1();
            int r1 = transition.getR1();
            int x2 = transition.getX2();
            int y2 = transition.getY2();
            int r2 = transition.getR2();

            if (fieldCopy[y1+1][x1+1] == '-') {
                System.out.println("ERROR: x1=" + x1 + " || y1=" + y1);
            } else {
                fieldCopy[y1+1][x1+1] = '█';
            }

            if (fieldCopy[y2 + 1][x2 + 1] == '-') {
                System.out.println("ERROR: x2=" + x2 + " || y2=" + y2);
            } else {
                fieldCopy[y2 + 1][x2 + 1] = '█';
            }

            switch (r1) {
                case 0:
                    fieldCopy[y1][x1+1] = '↕';
                    break;
                case 1:
                    fieldCopy[y1][x1+2] = '⤢';
                    break;
                case 2:
                    fieldCopy[y1+1][x1+2] = '↔';
                    break;
                case 3:
                    fieldCopy[y1+2][x1+2] = '⤡';
                    break;
                case 4:
                    fieldCopy[y1+2][x1+1] = '↕';
                    break;
                case 5:
                    fieldCopy[y1+2][x1] = '⤢';
                    break;
                case 6:
                    fieldCopy[y1+1][x1] = '↔';
                    break;
                case 7:
                    fieldCopy[y1][x1] = '⤡';
                    break;
                default:
                    break;
            }

            switch (r2) {
                case 0:
                    fieldCopy[y2][x2+1] = '↕';
                    break;
                case 1:
                    fieldCopy[y2][x2+2] = '⤢';
                    break;
                case 2:
                    fieldCopy[y2+1][x2+2] = '↔';
                    break;
                case 3:
                    fieldCopy[y2+2][x2+2] = '⤡';
                    break;
                case 4:
                    fieldCopy[y2+2][x2+1] = '↕';
                    break;
                case 5:
                    fieldCopy[y2+2][x2] = '⤢';
                    break;
                case 6:
                    fieldCopy[y2+1][x2] = '↔';
                    break;
                case 7:
                    fieldCopy[y2][x2] = '⤡';
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
