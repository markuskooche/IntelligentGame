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

    private final char[][] field;
    private final int width;
    private final int height;
    private int[] playerScores;
    private int[][] tmpField;

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
        this.playerScores = new int[playerAmount];

        this.height = field.length;
        this.width = field[0].length;

    }

    /**
     * Constructor to create a copy of a existing board
     * @param toCopyBoard
     */
    public Board(Board toCopyBoard) {
        this.playerAmount = toCopyBoard.getPlayerAmount();
        this.transitions = toCopyBoard.getAllTransitions();
        this.bombRadius = toCopyBoard.getBombRadius();
        this.field = copyField(toCopyBoard.getField());
        this.playerScores =  copyScores(toCopyBoard.getPlayerScores());

        this.height = field.length;
        this.width = field[0].length;
    }

    private int[] copyScores(int[] playerScores){
        int[] newScores = new int[playerAmount];
        for(int i = 0; i < playerAmount; i++){
            newScores[i] = playerScores[i];
        }
        return newScores;
    }

    private char[][] copyField(char[][] field) {
        int h  = field.length;
        int l = field[0].length;
        char[][] newField = new char[h][l];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < l; x++) {
                newField [y][x] = field[y][x];
            }
        }
        return newField;
    }

    protected void choice(char a, char b) {
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

    private int[][] createHelperField(){
        int[][] tmp = new int[height][width];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if(field[i][j] != '-'){
                    tmp[i][j] = 0;
                }else{
                    tmp[i][j] = -1;
                }
            }
        }
        return tmp;
    }

    public void executeBomb(int x, int y) {

        tmpField = createHelperField();
        executeBombRecursive(x, y, bombRadius+1);
        removeFields();

    }

    private void executeBombRecursive(int x, int y, int radius) {

        if (radius == 0) {
            return;
        }

        int[][] directions = {{0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}};
        int newX, newY;

        tmpField[y][x] = radius;

        for (int[] direction : directions) {
            newX = x + (direction[0]);
            newY = y + (direction[1]);

            if (newX < 0 || newX >= width || newY < 0 || newY >= height || field[newY][newX] == '-') {

                int directionValue = Direction.indexOf(direction);
                Transition transition;

                transition = getTransition(x, y, directionValue);

                if (transition != null) {
                    if(tmpField[transition.getY()][transition.getX()] < radius) {
                        executeBombRecursive(transition.getX(), transition.getY(), radius - 1);
                    }
                }

            } else {
                if ((tmpField[y][x] - 1) > tmpField[newY][newX]) {
                    executeBombRecursive(newX, newY, radius - 1);
                }
            }

        }
    }

    private void removeFields(){
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if(tmpField[i][j] != 0 && tmpField[i][j] != -1){
                    field[i][j] = '-';
                }
            }
        }
    }

    public void colorizeMove(Move legalMove, Player player, int additionalOperation) {
        boolean override = false;

        boolean inversion = false;
        boolean choice = false;
        boolean bonus = false;

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

    protected Move checkMove(int x, int y, char player, boolean isOverrideMove) {
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

    public int getPlayerAmount(){
        return playerAmount;
    }

    /**
     * Returns the current player scores.
     *
     * @return the current player scores
     */
    public int[] getPlayerScores() {
        return playerScores;
    }

    public void setPlayerScores(int[] playerScores) {
        this.playerScores = playerScores;
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
