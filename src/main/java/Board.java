import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * The board class contains all information about a current game board and stores transitions,
 * contains methods for processing a game move including special pieces and calculates allowed moves.
 *
 * @author Benedikt Halbritter
 * @author Iwan Eckert
 * @author Markus Koch
 */
public class Board {

    private final HashMap<Integer, Transition> transitions;
    private final int playerAmount;
    private final int bombRadius;

    private char[][] field;
    private final int width;
    private final int height;

    /**
     * Creates a Board class with all information about it.
     *
     * @param field two-dimensional char array which represents the board
     * @param transitions a HashMap of all transitions
     * @param playerAmount the number of players
     * @param bombRadius the bomb radius
     *
     * @see Transition
     */
    @NotNull
    public Board(char[][] field, HashMap<Integer, Transition> transitions, int playerAmount, int bombRadius) {
        this.playerAmount = playerAmount;
        this.transitions = transitions;
        this.bombRadius = bombRadius;
        this.field = field;

        this.height = field.length;
        this.width = field[0].length;
    }

    private void choice() {
        System.out.print("Please enter two Players you would like to change: ");
        Scanner scanner = new Scanner(System.in);

        char a = scanner.next().charAt(0);
        char b = scanner.next().charAt(0);

        choice(a, b);
    }

    private void choice(char a, char b) {
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

    private void bonus(Player player) {
        System.out.print("!!BONUS!! choose your item (b = bomb, o = override): ");
        Scanner scanner = new Scanner(System.in);

        char item = scanner.next().charAt(0);

        if (item == 'b') {
            System.out.println("BOMB selected");
            System.out.println("BEFORE: " + player.getBomb());
            player.increaseBomb();
            System.out.println("AFTER: " + player.getBomb());
        } else if (item == 'o') {
            System.out.println("OVERRIDE selected");
            System.out.println("BEFORE: " + player.getOverrideStone());
            player.increaseOverrideStone();
            System.out.println("AFTER: " + player.getOverrideStone());
        } else {
            System.out.println("INVALID SELECTION");
            System.exit(1);
        }
    }

    private void inversion() {
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

    // TODO: delete me later
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
                fieldCopy[y1 + 1][x1 + 1] = '@';
            }

            if (fieldCopy[y2 + 1][x2 + 1] == '-') {
                System.out.println("ERROR: x2=" + x2 + " || y2=" + y2);
            } else {
                fieldCopy[y2 + 1][x2 + 1] = '@';
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


    /**
     * Returns a list of all positions from a passed player.
     *
     * @param player char representation of a player
     *
     * @return a list of all positions from a passed player
     */
    public List<int[]> getPositions(char player) {
        List<int[]> positions = new LinkedList<>();

        // searches for all pieces of a player
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (field[y][x] == player) {
                    positions.add(new int[] {x, y});
                }
            }
        }

        return positions;
    }

    /**
     * Returns a list of all legal moves from a passed player.
     * It is possible to include override stones or not.
     *
     * @param player char representation of a player
     * @param overrideMoves true if override moves should be added
     *
     * @return a list of all legal {@link Move}s from a passed player
     *
     * @see Move
     */
    public List<Move> getLegalMovesPrint(Player player, boolean overrideMoves) {
        List<Move> legalMoves = new LinkedList<>();
        List<Move> legalOverrideMoves = new LinkedList<>();

        // inserts all legal moves of a player's pieces into a list
        for (int[] position : getPositions(player.getNumber())) {
            legalMoves.addAll(checkNormalMoves(position[0], position[1], player.getNumber()));

            if (player.hasOverrideStone() && overrideMoves) {
                legalOverrideMoves.addAll(checkOverrideMoves(position[0], position[1], player.getNumber()));
            }
        }

        if (player.hasOverrideStone() && overrideMoves) {
            for (int[] expansion : getPositions('x')) {
                Move expansionMove = new Move(expansion);
                legalOverrideMoves.add(expansionMove);
            }
        }

        System.out.println("LEGAL MOVES FROM PLAYER '" + player.getNumber() + "'");
        for (Move legalNormalMove : legalMoves) {
            System.out.println(legalNormalMove);
        }

        if (overrideMoves) {
            System.out.println("\nLEGAL OVERRIDE MOVES FROM PLAYER '" + player.getNumber() + "'");
            for (Move legalOverrideMove : legalOverrideMoves) {
                System.out.println(legalOverrideMove);
            }
        }

        legalMoves.addAll(legalOverrideMoves);

        return legalMoves;
    }

    private List<Move> getLegalMoves(Player player, boolean overrideMoves) {
        List<Move> legalMoves = new LinkedList<>();

        // inserts all legal moves of a player's pieces into a list
        for (int[] position : getPositions(player.getNumber())) {
            legalMoves.addAll(checkNormalMoves(position[0], position[1], player.getNumber()));

            if (player.hasOverrideStone() && overrideMoves) {
                legalMoves.addAll(checkOverrideMoves(position[0], position[1], player.getNumber()));
            }
        }

        if (player.hasOverrideStone() && overrideMoves) {
            for (int[] expansion : getPositions('x')) {
                Move expansionMove = new Move(expansion);
                legalMoves.add(expansionMove);
            }
        }

        return legalMoves;
    }

    /**
     * Executing a valid move entered by a human.
     *
     * @param player player who should execute the move
     * @param overrideMoves boolean whether override stones are to be executed as well
     *
     * @see Move
     */
    public void executeMove(Player player, boolean overrideMoves) {
        getLegalMovesPrint(player, overrideMoves);

        System.out.print("\nPlease enter a valid move: ");
        Scanner scanner = new Scanner(System.in);
        int x = scanner.nextInt();
        int y = scanner.nextInt();

        executeMove(x, y, player, overrideMoves);
        System.out.println("\n" + toString());
    }

    /**
     * Computerized method for executing a valid move.
     *
     * @param x position of the player on the x axis
     * @param y position of the player on the y axis
     * @param player player who should execute the move
     * @param overrideMoves boolean whether override stones are to be executed as well
     *
     * @see Move
     */
    public void executeMove(int x, int y, Player player, boolean overrideMoves) {
        List<Move> legalMoves = getLegalMoves(player, overrideMoves);

        int[] move = new int[] {x, y};
        colorizeMove(legalMoves, move, player);

    }

    private void colorizeMove(List<Move> legalMoves, int[] move, Player player) {
        boolean override = false;

        boolean inversion = false;
        boolean choice = false;
        boolean bonus = false;

        for (Move legalMove : legalMoves) {
            if (legalMove.isMove(move)) {
                for (int[] position : legalMove.getList()) {
                    int colorizeX = position[0];
                    int colorizeY = position[1];
                    field[colorizeY][colorizeX] = player.getNumber();

                    if (legalMove.getInversion()) {
                        inversion = true;
                    } else if (legalMove.getChoice()) {
                        choice = true;
                    } else if (legalMove.getBonus()) {
                        bonus = true;
                    } else if (legalMove.getOverride()) {
                        override = true;
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
        } else if (override) {
            player.decreaseOverrideStone();
        }
    }

    private List<Move> checkNormalMoves(int x, int y, char player) {
        List<Move> legalMoves = new LinkedList<>();
        int[] currentDirection;

        for (int[] direction : Direction.getList()) {
            Move checkMove = new Move();
            currentDirection = new int[] {direction[0], direction[1]};
            char nextPiece;

            int currentX = x;
            int currentY = y;

            while (true) {
                int nextX, nextY;
                int directionValue = Direction.indexOf(currentDirection);
                Transition transition = getTransition(directionValue, currentX, currentY);

                // checks if there is a transaction, otherwise it goes one step further
                if (transition != null) {
                    // if there is a transition, the position is updated
                    int[] newPosition = transition.getDestination(currentX, currentY, directionValue);
                    nextX = newPosition[0];
                    nextY = newPosition[1];

                    // if there is a transition, the direction is updated
                    int[] newDirection = Direction.valueOf(newPosition[2]);
                    currentDirection[0] = (-1) * newDirection[0];
                    currentDirection[1] = (-1) * newDirection[1];
                } else {
                    nextX = currentX + currentDirection[0];
                    nextY = currentY + currentDirection[1];
                }

                // checks if the next step is no longer on the game field
                if (nextX < 0 || nextX >= width || nextY < 0 || nextY >= height) {
                    break;
                } else {
                    nextPiece = field[nextY][nextX];
                }

                // it is not allowed to reach himself or a hole
                if (nextPiece == player || nextPiece == '-') {
                    break;
                }
                // checks the validity for special fields
                else if (nextPiece == 'b' || nextPiece == 'i' || nextPiece == 'c') {
                    Move move = checkSpecialField(checkMove, nextPiece, nextX, nextY);

                    // when a move is returned it is an allowed move
                    if (move != null) {
                        legalMoves.add(checkMove);
                    }
                    break;
                }
                // checks if it is a legal move
                else if (nextPiece == '0') {
                    // checks if there are fields between 'player' and '0'
                    if (!checkMove.isEmpty()) {
                        checkMove.add(new int[]{nextX, nextY});
                        legalMoves.add(checkMove);
                    }
                    break;
                }
                // when you come upon an opponent you can move on
                else {
                    checkMove.add(new int[] {nextX, nextY});
                    currentX = nextX;
                    currentY = nextY;
                }
            }
        }

        return legalMoves;
    }

    private List<Move> checkOverrideMoves(int x, int y, char player) {
        LinkedList<Move> overrideMoves = new LinkedList<>();
        int[] currentDirection;

        for (int[] direction : Direction.getList()) {
            Move checkMove = new Move();
            checkMove.setOverride();

            currentDirection = new int[] {direction[0], direction[1]};
            char nextPiece;

            int currentX = x;
            int currentY = y;

            while (true) {
                int nextX, nextY;
                int directionValue = Direction.indexOf(currentDirection);
                Transition transition = getTransition(directionValue, currentX, currentY);

                // checks if there is a transaction, otherwise it goes one step further
                if (transition != null) {
                    // if there is a transition, the position is updated
                    int[] newPosition = transition.getDestination(currentX, currentY, directionValue);
                    nextX = newPosition[0];
                    nextY = newPosition[1];

                    // if there is a transition, the direction is updated
                    int[] newDirection = Direction.valueOf(newPosition[2]);
                    currentDirection[0] = (-1) * newDirection[0];
                    currentDirection[1] = (-1) * newDirection[1];
                } else {
                    nextX = currentX + currentDirection[0];
                    nextY = currentY + currentDirection[1];
                }

                // checks if the next step is no longer on the game field
                if (nextX < 0 || nextX >= width || nextY < 0 || nextY >= height) {
                    break;
                } else {
                    nextPiece = field[nextY][nextX];
                }
                // it is not allowed to reach a hole, a empty or special piece
                if (nextPiece == '-' || nextPiece == 'b' || nextPiece == 'i' || nextPiece == 'c' || nextPiece == '0') {
                    break;
                }
                // when you get to the start position it is not a valid move
                // (this situation can only be achieved with the help of transitions)
                else if (nextX == x && nextY == y) {
                    break;
                }
                // it is allowed to achieve a figure of itself
                if (nextPiece == player) {
                    if (!checkMove.isEmpty()) {
                        checkMove.add(new int[]{nextX, nextY});
                        overrideMoves.add(checkMove);
                    }
                    break;

                }
                // when you come upon an opponent you can move on
                else {
                    // moves are not added if the override stone is
                    // set to a position that is already in the list
                    if (!checkMove.contains(nextX, nextY)) {
                        checkMove.add(new int[]{nextX, nextY});
                        currentX = nextX;
                        currentY = nextY;

                        // a new instance is created and added to the list
                        if (checkMove.size() >= 2) {
                            Move move = new Move(checkMove);
                            overrideMoves.add(move);
                        }
                    } else {
                        checkMove.add(new int[]{nextX, nextY});
                        currentX = nextX;
                        currentY = nextY;
                    }
                }
            }
        }

        return overrideMoves;
    }

    private Move checkSpecialField(Move checkMove, char nextPiece, int nextX, int nextY) {
        // if a current move does not contain any elements, it means that the next
        // square is right next to the actual player, but this is not a valid move
        if (!checkMove.isEmpty()) {
            // if there is at least one field in between, it is a valid move
            checkMove.add(new int[]{nextX, nextY});

            if (nextPiece == 'i') {
                checkMove.setInversion();
            } else if (nextPiece == 'c') {
                checkMove.setChoice();
            } else {
                checkMove.setBonus();
            }

            return checkMove;
        }

        return null;
    }

    private Transition getTransition(int direction, int x, int y) {
        int transitionKey = Transition.hash(x, y, direction);
        return transitions.get(transitionKey);
    }

    public List<int[]> getStonesOfPlayer(char player) {
        List<int[]> stones = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (field[y][x] == player) {
                    stones.add(new int[] {x, y});
                }
            }
        }
        return stones;
    }

    public int getBombRadius() {
        return bombRadius;
    }

    /**
     * Returns the height of the board
     *
     * @return the height of the board
     */
    public int getHeight() {
        return height;
    }

    /**
     * Returns the width of the board
     *
     * @return the width of the board
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the a HashMap of all transitions.
     *
     * @return HashMap of all transitions
     *
     * @see Transition
     */
    public HashMap<Integer, Transition> getTransitions() {
        return transitions;
    }

    /**
     * Returns the current board.
     *
     * @return the current board
     */
    public char[][] getField() {
        return field;
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
