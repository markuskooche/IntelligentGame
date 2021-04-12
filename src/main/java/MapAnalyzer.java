public class MapAnalyzer {

    private int[][] field;
    private Board board;
    private int[][] reachableField; // bool array

    public MapAnalyzer(Board b) {
        board = b;
    }

    /**
     * creates a Field that contains only reachable Fields and values every Field by location and location to other Fields
     *
     */
    public void createField() {

        int height = board.getHeight();
        int width = board.getWidth();
        field = new int[height][width];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {

                char currField = board.getField()[i][j];

                System.out.println("Ausgabe: " + j + " + " + i);

                if (currField != '-') {
                    //create RechableField for the First field that is Writable (The most performant way would by around the middle of the Map
                    createReachableField(j, i);
                    int newValue = getLocationValue(j, i);
                    field[i][j] += newValue * 3;

                    if (newValue >= 5) {
                        //Field is Corner
                        createWaves(j, i, newValue, newValue / 2);
                    } else {
                        //Field is no Corner
                        createWaves(j, i, newValue, newValue);
                    }
                }
                if (currField == 'c') {
                    field[i][j] += 20;
                    createWaves(j, i, 50, 20);
                } else if (currField == 'b') {
                    field[i][j] += 15;
                    createWaves(j, i, 50, 15);
                } else if (currField == 'i') {
                    field[i][j] += 10;
                    createWaves(j, i, 50, 10);
                }

            }
        }

    }

    private void createReachableField(int x, int y) {
        int height = board.getHeight();
        int width = board.getWidth();
        reachableField = new int[height][width];
        traverseMap(x, y);
        alterCurrentMap();
    }
    /**
     * Updates the current board and removes all Fields that are not reachable
     */
    private void alterCurrentMap() {
        int height = board.getHeight();
        int width = board.getWidth();

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {

                int currField = reachableField[i][j];

                if (currField == 0) {
                    board.getField()[i][j] = '-';
                }
            }
        }
    }

    /**
     * Traverses the Map with depth-first search to find all reachable fields
     */
    private void traverseMap(int x, int y) {
        int[][] directions = {{-1, 0}, {-1, 1}, {0, 1}, {1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}};
        int[] currentDirection;

        reachableField[y][x] = 1;


        if( x == 29 && y == 18){
            System.out.println("test");
        }


        //System.out.println("(" + x + ")(" + y + ") Wurde gelockt");
        for (int[] direction : directions) {
            currentDirection = direction;

            int nextX = x + currentDirection[1];
            int nextY = y + currentDirection[0];

            // avoids IndexOutOfBounds exception                                                     required for intermap transactions
            if (nextX < 0 || nextX >= board.getWidth() || nextY < 0 || nextY >= board.getHeight() || board.getField()[nextY][nextX] == '-') {
                int directionValue = Direction.indexOf(direction);

                int transitionKey = Transition.hash(x, y, directionValue);
                Transition transition = board.getTransition().get(transitionKey);

                // Follow Transactions
                if (transition != null) {
                    if (reachableField[transition.getY1()][transition.getX1()] != 1) {
                        traverseMap(transition.getX1(), transition.getY1());
                    } else if (reachableField[transition.getY2()][transition.getX2()] != 1) {
                        traverseMap(transition.getX2(), transition.getY2());
                    }
                }

            } else {
                // Follow fields if they are reachable
                char currChar = board.getField()[nextY][nextX];
                if (currChar != '-' && reachableField[nextY][nextX] != 1) {
                    traverseMap(nextX, nextY);
                }
            }

        }
    }
    /**
     * Calculates the Map-Score for the given Player
     *
     * @return int with the value of the Player-Score for the given player
     */
    public int calculateScoreForPlayer(char playernumber) {
        int height = board.getHeight();
        int width = board.getWidth();
        int playerScore = 0;

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {

                char currField = board.getField()[i][j];

                if (currField == playernumber) {
                    playerScore += field[i][j];
                }
            }
        }
        return playerScore;
    }

    /**
     * Creates a wave in all directions that changes sign every Field and gets smaller.
     *
     */
    private void createWaves(int x, int y, int range, int startValue) {

        int[][] directions = {{-1, 0}, {-1, 1}, {0, 1}, {1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}};
        int exhaustion = 1;
        int omen = -1;
        int currX ;
        int currY ;
        int oldX = x;
        int oldY = y;
        // go in each direction
        for (int[] direction : directions) {
            // go until the range runs out or there are no more reachable fields
            for (int currRange = 1; currRange <= range; currRange++) {

                currX = x + (direction[1] * currRange);
                currY = y + (direction[0] * currRange);

                if (currX < 0 || currX >= board.getWidth() || currY < 0 || currY >= board.getHeight()) {

                    int directionValue = Direction.indexOf(direction);

                    int transitionKey = Transition.hash(oldX, oldY, directionValue);
                    Transition transition = board.getTransition().get(transitionKey);

                    if (transition != null) {
                        followTransaction(transition, oldX, oldY, direction, range - currRange, startValue, exhaustion);
                    }
                    break;

                } else {
                    // remember last reachable Field in case there is a transaction
                    if (board.getField()[currY][currX] != '-') {
                    oldX = currX;
                    oldY = currY;

                        if (((startValue) - exhaustion) > 0) {

                            if (currRange % 2 == 1) {
                                field[currY][currX] += ((startValue) - exhaustion) * omen;
                            } else {
                                field[currY][currX] += ((startValue) - exhaustion);
                            }
                        }
                    }else{
                        break;
                    }
                }
                exhaustion++;
            }
            exhaustion = 1;
        }

    }

    private void followTransaction(Transition transition, int x, int y, int[] direction, int range, int startValue, int exhaustion) {

        int[][] directions = {{-1, 0}, {-1, 1}, {0, 1}, {1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}};
        int omen = -1;
        int directionValue = Direction.indexOf(direction);

        //get the opposite side of the given transaction
        int[] newPosition = transition.getDestination(x, y, directionValue);
        int startX = newPosition[0];
        int startY = newPosition[1];
        int[] newDirection = directions[newPosition[2]];
        // new direction must be turned by 180 degrees
        newDirection[0] *= (-1);
        newDirection[1] *= (-1);
        boolean first = true;
        int oldX = startX;
        int oldY = startY;

        while (range > 0) {

            System.out.println("(" + oldX + ")(" + oldY + ") Wurde gelockt");

            //Skip the first iteration, because the field the transaction ends in must also be counted
            if (!first) {
                startX += (newDirection[1]);
                startY += (newDirection[0]);
            } else {
                first = false;
            }

            if (startX < 0 || startX >= board.getWidth() || startY < 0 || startY >= board.getHeight()) {

                directionValue = Direction.indexOf(newDirection);

                int transitionKey = Transition.hash(oldX, oldY, directionValue);
                transition = board.getTransition().get(transitionKey);

                if (transition == null) {
                    return;
                } else {
                    followTransaction(transition, oldX, oldY, newDirection, range, startValue, exhaustion);
                    break;
                }
            }else{
                if (board.getField()[startY][startX] != '-') {

                    oldX = startX;
                    oldY = startY;

                    if (((startValue) - exhaustion) > 0) {

                        if (range % 2 == 1) {
                            field[startY][startX] += ((startValue) - exhaustion) * omen;
                        } else {
                            field[startY][startX] += ((startValue) - exhaustion);
                        }
                    }
                }
            }

            range--;
            exhaustion++;
        }
    }

    /**
     * Get the value of a field depending on the aligning fields that are reachable
     */
    private int getLocationValue(int x, int y) {

        int[][] directions = {{-1, 0}, {-1, 1}, {0, 1}, {1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}};
        int[] currentDirection;
        int currnumbers = 0;
        char[][] tmpfield = board.getField();

        for (int[] direction : directions) {
            currentDirection = direction;

            int nextX = x + currentDirection[1];
            int nextY = y + currentDirection[0];

            // avoids IndexOutOfBounds exception
            if (nextX < 0 || nextX >= board.getWidth() || nextY < 0 || nextY >= board.getHeight()) {
                int directionValue = Direction.indexOf(direction);

                int transitionKey = Transition.hash(x, y, directionValue);
                Transition transition = board.getTransition().get(transitionKey);

                if (transition != null) {
                    continue;
                }
                currnumbers++;
            } else {

                char currChar = tmpfield[nextY][nextX];
                if (currChar == '-') {
                    currnumbers++;
                }
            }
        }
        return currnumbers;
    }

    @Override
    public String toString() {
        StringBuilder boardString = new StringBuilder();

        for (int y = 0; y < board.getHeight(); y++) {
            for (int x = 0; x < board.getWidth(); x++) {
                boardString.append(field[y][x]);
                boardString.append(" ");
            }
            boardString.append("\n");
        }

        return boardString.toString();
    }

    public int[][] getField() {
        return field;
    }

    public void setField(int[][] field) {
        this.field = field;
    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public int[][] getReachableField() {
        return reachableField;
    }

    public void setReachableField(int[][] reachableField) {
        this.reachableField = reachableField;
    }
}
