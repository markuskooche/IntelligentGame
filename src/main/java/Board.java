import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Board {

    private final HashMap<Integer, Transition> transitions;
    private final int playerAmount;
    private final int bombRadius;

    private char[][] field;
    private final int width;
    private final int height;

    @NotNull
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

    public void bonus(Player player) {
        System.out.print("!!BONUS!! choose your item (b = bomb, o = override): ");
        Scanner scanner = new Scanner(System.in);

        char item = scanner.next().charAt(0);

        if (item == 'b') {
            System.out.println("BOMB selected");
            System.out.println("BEFORE: " + player.getBomb());
            player.setBomb(player.getBomb() + 1);
            System.out.println("AFTER: " + player.getBomb());
        } else if (item == 'o') {
            System.out.println("OVERRIDE selected");
            System.out.println("BEFORE: " + player.getOverrideStone());
            player.setOverrideStone(player.getOverrideStone() + 1);
            System.out.println("AFTER: " + player.getOverrideStone());
        } else {
            System.out.println("INVALID SELECTION");
            System.exit(1);
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

    public void executeMove(int x, int y, Player player) {
        List<Moves> legalMoves = getLegalMoves(player.getNumber());
        boolean inversion = false;
        boolean choice = false;
        boolean bonus = false;

        int[] move = new int[] {x, y};

        boolean moveWasValid = false;
        for (Moves legalMove : legalMoves) {
            if (legalMove.isMove(move)) {
                for (int[] position : legalMove.getList()) {
                    int colorizeX = position[0];
                    int colorizeY = position[1];
                    field[colorizeY][colorizeX] = player.getNumber();
                    moveWasValid = true;

                    if (legalMove.getInversion()) {
                        inversion = true;
                    } else if (legalMove.getChoice()) {
                        choice = true;
                    } else if (legalMove.getBonus()) {
                        bonus = true;
                    }
                }
            }
        }

        if (inversion) {
            inversion();
        } else if (choice) {
            // try only for TestExecuteMove
            try {
                choice();
            } catch (NoSuchElementException e) {
                choice('1', '2');
            }
        } else if (bonus) {
            bonus(player);
        }

        if (moveWasValid) {
            System.out.println("\n" + toString());
        } else {
            if (player.hasOverrideStone()) {
                if (tryOverrideMove(move[0], move[1], player.getNumber())) {
                    System.out.println("\n" + toString());
                } else {
                    System.out.println("DISQUALIFIED: not a legal move (not a possible override stone)");
                }
            } else {
                System.out.println("DISQUALIFIED: not a legal move (no valid position selected & no override stone)");
            }
        }
    }

    private boolean tryOverrideMove(int x, int y, char player) {
        List<Moves> legalOverrideMoves = new LinkedList<>();
        boolean validOverrideMove = false;

        int[][] directions = {{-1, 0 }, {-1, 1}, {0, 1}, {1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}};
        int[] currentDirection;

        for (int[] direction : directions) {
            Moves checkMove = new Moves();
            currentDirection = direction;

            int currentX = x;
            int currentY = y;

            while (true) {
                int nextX = currentX + currentDirection[1];
                int nextY = currentY + currentDirection[0];
                boolean isTransition = false;

                // avoids IndexOutOfBounds exception
                if (nextX < 0 || nextX >= width || nextY < 0 || nextY >= height) {
                    int directionValue = Direction.indexOf(direction);

                    int transitionKey = Transition.hash(currentX, currentY, directionValue);
                    Transition transition = transitions.get(transitionKey);

                    if (transition != null) {
                        isTransition = true;
                    } else {
                        break;
                    }
                }

                char nextPiece = 'T';
                if (!isTransition) {
                    nextPiece = field[nextY][nextX];
                }

                if (nextPiece == player) {
                    if (!checkMove.isEmpty()) {
                        checkMove.add(new int[]{nextX, nextY});
                        checkMove.add(new int[]{x, y});
                        legalOverrideMoves.add(checkMove);
                        validOverrideMove = true;
                        System.out.println("VALID OVERRIDE MOVE" + Arrays.toString(direction));
                    }
                    break;
                }
                else if (nextX == x && nextY == y) {
                    break;
                }
                else if (nextPiece == 'b' || nextPiece == 'i' || nextPiece == 'c' || nextPiece == 'x') {
                    break;
                }
                // check if a transition is possible
                else if (nextPiece == '-' || nextPiece == 'T') {
                    int directionValue = Direction.indexOf(direction);

                    int transitionKey = Transition.hash(currentX, currentY, directionValue);
                    Transition transition = transitions.get(transitionKey);

                    if (transition == null) {
                        break;
                    } else {
                        int[] newPosition = transition.getDestination(currentX, currentY, directionValue);
                        char transitionPiece = field[newPosition[1]][newPosition[0]];

                        if (transitionPiece == player) {
                            if (!checkMove.isEmpty()) {
                                checkMove.add(new int[]{newPosition[0], newPosition[1]});
                                checkMove.add(new int[]{x, y});
                                legalOverrideMoves.add(checkMove);
                                validOverrideMove = true;
                                System.out.println("VALID OVERRIDE MOVE (with transition)");
                            }
                            break;
                        }
                        else if (transitionPiece == '0') {
                            break;
                        }
                        else {
                            int[] newDirection = Direction.valueOf(newPosition[2]);
                            if (newDirection != null) {
                                currentDirection[0] = (-1) * newDirection[0];
                                currentDirection[1] = (-1) * newDirection[1];
                                checkMove.add(new int[]{newPosition[0], newPosition[1]});
                                currentX = newPosition[0];
                                currentY = newPosition[1];
                            } else {
                                System.err.println("FIXME: Transition found no value");
                                System.err.println("(" + x + ", " + y + ")");
                                System.err.println(toString());
                                break;
                            }
                        }
                    }
                }
                // finishes a valid move
                else if (nextPiece == '0') {
                    break;
                }
                // when it is an opponent
                else {
                    checkMove.add(new int[] {nextX, nextY});
                    currentX = nextX;
                    currentY = nextY;

                    nextX += currentDirection[1];
                    nextY += currentDirection[0];
                }

            }
        }

        for (Moves legalMove : legalOverrideMoves) {
            for (int[] position : legalMove.getList()) {
                int colorizeX = position[0];
                int colorizeY = position[1];
                field[colorizeY][colorizeX] = player;
            }
        }

        return validOverrideMove;
    }

    /**
     * Creates a list with legal moves for a selected player
     *
     * @returns List of allowed positions for the player [0] = x and [1] = y
     */
    public List<Moves> getLegalMoves(char player) {
        List<Moves> legalMoves = new LinkedList<>();

        // searches for all pieces and adds the legal moves to the list
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (field[y][x] == player) {
                    List<Moves> tmpMoves = checkDirection(x, y, player);
                    legalMoves.addAll(tmpMoves);
                }
            }
        }

        System.out.println("\nLEGAL MOVES FROM PLAYER '" + player + "'");
        if (!legalMoves.isEmpty()) {
            for (Moves move : legalMoves) {
                System.out.println(move);
            }
        } else {
            System.out.println("THERE ARE NOT LEGAL MOVES");
        }

        return legalMoves;
    }

    public List<Moves> checkDirection(int x, int y, char player) {
        List<Moves> legalMoves = new LinkedList<>();

        int[][] directions = {{-1, 0}, {-1, 1}, {0, 1}, {1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}};

        int[] currentDirection;

        for (int[] direction : directions) {
            Moves checkMove = new Moves();
            currentDirection = direction;

            int currentX = x;
            int currentY = y;

            while (true) {
                int nextX = currentX + currentDirection[1];
                int nextY = currentY + currentDirection[0];
                boolean isTransition = false;

                // avoids IndexOutOfBounds exception
                if (nextX < 0 || nextX >= width || nextY < 0 || nextY >= height) {
                    int directionValue = Direction.indexOf(direction);

                    int transitionKey = Transition.hash(currentX, currentY, directionValue);
                    Transition transition = transitions.get(transitionKey);

                    if (transition != null) {
                        isTransition = true;
                    } else {
                        break;
                    }
                }

                char nextPiece = 'T';
                if (!isTransition) {
                    nextPiece = field[nextY][nextX];
                }

                // check if it is a non-allowed field
                if (nextPiece == player) {
                    break;
                }
                else if (nextPiece == 'b' || nextPiece == 'i' || nextPiece == 'c' || nextPiece == 'x') {
                    if (!checkMove.isEmpty()) {
                        checkMove.add(new int[]{nextX, nextY});

                        if (nextPiece == 'i') {
                            checkMove.setInversion();
                        } else if (nextPiece == 'c') {
                            checkMove.setChoice();
                        } else if (nextPiece == 'b') {
                            checkMove.setBonus();
                        }

                        legalMoves.add(checkMove);
                    }
                    break;
                }
                // check if a transition is possible
                else if (nextPiece == '-' || nextPiece == 'T') {
                    int directionValue = Direction.indexOf(direction);

                    int transitionKey = Transition.hash(currentX, currentY, directionValue);
                    Transition transition = transitions.get(transitionKey);

                    if (transition == null) {
                        break;
                    } else {
                        int[] newPosition = transition.getDestination(currentX, currentY, directionValue);
                        char transitionPiece = field[newPosition[1]][newPosition[0]];

                        if (transitionPiece == player) {
                            break;
                        }
                        else if (transitionPiece == '0') {
                            if (!checkMove.isEmpty()) {
                                checkMove.add(new int[]{newPosition[0], newPosition[1]});
                                legalMoves.add(checkMove);
                            }
                            break;
                        }
                        else {
                            int[] newDirection = Direction.valueOf(newPosition[2]);
                            if (newDirection != null) {
                                currentDirection[0] = (-1) * newDirection[0];
                                currentDirection[1] = (-1) * newDirection[1];
                                checkMove.add(new int[]{newPosition[0], newPosition[1]});
                                currentX = newPosition[0];
                                currentY = newPosition[1];
                            } else {
                                System.err.println("FIXME: Transition found no value");
                                System.err.println("(" + x + ", " + y + ")");
                                System.err.println(toString());
                                return null;
                            }
                        }
                    }
                }
                // finishes a valid move
                else if (nextPiece == '0') {
                    if (!checkMove.isEmpty()) {
                        checkMove.add(new int[]{nextX, nextY});
                        legalMoves.add(checkMove);
                    }
                    break;
                }
                // when it is an opponent
                else {
                    checkMove.add(new int[] {nextX, nextY});
                    currentX = nextX;
                    currentY = nextY;

                    nextX += currentDirection[1];
                    nextY += currentDirection[0];
                }
            }
        }

        return legalMoves;
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
