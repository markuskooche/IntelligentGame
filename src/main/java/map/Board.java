package map;

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
     * Creates a map.Board class with all information about it.
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

    private void choiceManually() {
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

    private void bonus(Player player, int bonus) {
        if (bonus == 21) {
            player.increaseOverrideStone();
        } else if(bonus == 20) {
            player.increaseBomb();
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

    /**
     * Returns a list of all positions from a passed player.
     *
     * @param player char representation of a player
     *
     * @return a list of all positions from a passed player
     */
    public List<int[]> getPlayerPositions(char player) {
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
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                char piece = field[y][x];
                if ("0bic".indexOf(piece) != -1) {
                    Move legalMove = checkMove(x, y, player.getNumber(), false);

                    if (!legalMove.isEmpty()) {
                            legalMoves.add(legalMove);
                    }
                }

                if (player.hasOverrideStone() && overrideMoves && "x12345678".indexOf(field[y][x]) != -1) {
                    Move legalOverrideMove = checkMove(x, y, player.getNumber(), true);
                    if (!legalOverrideMove.isEmpty()) {
                        legalOverrideMoves.add(legalOverrideMove);
                    }
                }
            }
        }

        if (player.hasOverrideStone() && overrideMoves) {
            for (int[] expansion : getPlayerPositions('x')) {
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

    public List<Move> getLegalMoves(Player player, boolean overrideMoves) {
        List<Move> legalMoves = new LinkedList<>();

        // inserts all legal moves of a player's pieces into a list
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                char piece = field[y][x];
                if ("0bic".indexOf(piece) != -1) {
                    Move legalMove = checkMove(x, y, player.getNumber(), false);

                    if (!legalMove.isEmpty()) {
                        legalMoves.add(legalMove);
                    }
                }

                if (player.hasOverrideStone() && overrideMoves && "x12345678".indexOf(field[y][x]) != -1) {
                    Move legalOverrideMove = checkMove(x, y, player.getNumber(), true);
                    if (!legalOverrideMove.isEmpty()) {
                        legalMoves.add(legalOverrideMove);
                    }
                }
            }
        }

        if (player.hasOverrideStone() && overrideMoves) {
            for (int[] expansion : getPlayerPositions('x')) {
                Move expansionMove = new Move(expansion);
                legalMoves.add(expansionMove);
            }
        }

        return legalMoves;
    }

    public void executeBomb(int x, int y) {
        field[y][x] = '-';
    }

    /**
     * Executing a valid move entered by a human (for testing).
     *
     * @param player player who should execute the move
     * @param overrideMoves boolean whether override stones are to be executed as well
     *
     * @see Move
     */
    public void executeMoveManually(Player player, boolean overrideMoves) {
        getLegalMovesPrint(player, overrideMoves);

        System.out.print("\nPlease enter a valid move: ");
        Scanner scanner = new Scanner(System.in);
        int x = scanner.nextInt();
        int y = scanner.nextInt();
        int additionalOperation = scanner.nextInt();

        executeMove(x, y, player, additionalOperation, overrideMoves);
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
    public void executeMove(int x, int y, Player player, int additionalOperation, boolean overrideMoves) {
        List<Move> legalMoves = getLegalMoves(player, overrideMoves);

        int[] move = new int[] {x, y};
        colorizeMove(legalMoves, move, player, additionalOperation);
    }

    private void colorizeMove(List<Move> legalMoves, int[] move, Player player, int additionalOperation) {
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

                    if (legalMove.isInversion()) {
                        inversion = true;
                    } else if (legalMove.isChoice()) {
                        choice = true;
                    } else if (legalMove.isBonus()) {
                        bonus = true;
                    } else if (legalMove.isOverride()) {
                        override = true;
                    }
                }
            }
        }

        if (inversion) {
            inversion();
        } else if (choice) {
            choice(player.getNumber(), (char) (additionalOperation + '0'));
        } else if (bonus) {
            bonus(player, additionalOperation);
        } else if (override) {
            player.decreaseOverrideStone();
        }
    }

    private Move checkMove(int x, int y, char player, boolean isOverrideMove) {
        Move legalMove;

        if (isOverrideMove) {
            legalMove = new Move();
            legalMove.setOverride();
        } else {
            legalMove = checkSpecialField(field[y][x]);
        }
        int[] currentDirection;

        for (int[] direction : Direction.getList()) {
            currentDirection = new int[] {direction[0], direction[1]};
            Move checkMove = new Move();
            char nextPiece;

            int currentX = x;
            int currentY = y;

            while (true) {
                int nextX, nextY;
                int directionValue = Direction.indexOf(currentDirection);
                Transition transition = getTransition(currentX, currentY, directionValue);

                // checks if there is a transition, otherwise it goes one step further
                if (transition != null) {
                    // if there is a transition, the position is updated
                    int[] newPosition = transition.getDestination();
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
                if ("0-bic".indexOf(nextPiece) != -1) {
                    break;
                }
                // when you get to the start position it is not a valid move
                // (this situation can only be achieved with the help of transitions)
                else if (nextX == x && nextY == y) {
                    break;
                }
                // checks if it is a legal move
                else if (nextPiece == player) {
                    // checks if there are fields between 'player' and '0'
                    if (!checkMove.isEmpty()) {
                        if (legalMove.isEmpty()) {
                            legalMove.add(new int[] {x, y});
                        }
                        legalMove.merge(checkMove);
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

        return legalMove;
    }

    private Move checkSpecialField(char piece) {
        Move move = new Move();

        switch (piece) {
            case 'b':
                move.setBonus();
                break;
            case 'c':
                move.setChoice();
                break;
            case 'i':
                move.setInversion();
                break;
            default:
                break;
        }

        return move;
    }

    public Transition getTransition(int x, int y, int direction) {
        int transitionKey = Transition.hash(x, y, direction);
        return transitions.get(transitionKey);
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
    public HashMap<Integer, Transition> getAllTransitions() {
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

        boardString.append("    ");
        for (int i = 0; i < width; i++) {
            boardString.append((i % 10) + " ");
        }

        boardString.append("\n  /");
        for (int i = 0; i < width; i++) {
            boardString.append("--");
        }

        boardString.append("\n");
        for (int y = 0; y < height; y++) {
            boardString.append((y % 10) + " | ");
            for (int x = 0; x < width; x++) {
                boardString.append(field[y][x]);
                boardString.append(" ");
            }
            boardString.append("\n");
        }

        return boardString.toString();
    }
}
