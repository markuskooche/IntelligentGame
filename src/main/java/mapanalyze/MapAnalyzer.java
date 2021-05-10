
package mapanalyze;
import loganalyze.additional.AnalyzeParser;
import map.Board;
import map.Direction;
import map.Transition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapAnalyzer {

    private static AnalyzeParser analyzeParser;
    private boolean reachableFinished;

    private int[][] field;
    private int[][] initialField;
    private Board board;
    private int[][] reachableField;
    private List<int[]> specialFieldListSidePath;
    private List<int[]> specialFieldListMainPath;
    private List<int[]> followFieldsPath;
    private int playerNumber;

    public MapAnalyzer(Board b, int pNumber, AnalyzeParser analyzeParser) {
        MapAnalyzer.analyzeParser = analyzeParser;

        board = b;
        playerNumber = pNumber;

        try {
            long time = System.currentTimeMillis();
            createReachableField();
            // TODO: [Benedikt] System.out.println("Map Analyze Zeit: " + (System.currentTimeMillis() - time));
            reachableFinished = true;
        } catch (StackOverflowError soe) {
            analyzeParser.mapAnalyzerError();
            reachableFinished = false;

            int height = board.getHeight();
            int width = board.getWidth();

            field = new int[height][width];
        }

        createField();
    }

    /**
     * creates a Field that contains only reachable Fields and values every Field by location and location to other Fields
     */
    public void createField() {

        int height = board.getHeight();
        int width = board.getWidth();

        //Field muss zurückgesetzt werden, da createField sehr oft aufgerufen wird
        for (int y = 0; y < height; y++) {
            if (width >= 0) System.arraycopy(initialField[y], 0, field[y], 0, width);
        }

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {

                char currField = board.getField()[i][j];

                if (currField != '-' && field[i][j] != Integer.MIN_VALUE) {
                    int newValue = getLocationValue(j, i);
                    int multiplier;
                    if (newValue == 7) {
                        multiplier = 25;
                        field[i][j] += newValue * multiplier;
                        createWaves(j, i, playerNumber, (newValue * multiplier)/4);
                    } else if (newValue == 6) {
                        multiplier = 15;
                        field[i][j] += newValue * multiplier;
                        createWaves(j, i, playerNumber, (newValue * multiplier)/4);
                    } else if (newValue == 5) {
                        multiplier = 10;
                        field[i][j] += newValue * multiplier;
                        createWaves(j, i, playerNumber, (newValue * multiplier)/4);
                    }else {
                        multiplier = 3;
                        field[i][j] += newValue * multiplier;
                        createWaves(j, i, playerNumber, newValue);
                    }
                }
                if (currField == 'c') {
                    field[i][j] += 5000;
                    createWaves(j, i, playerNumber, 25);
                } else if (currField == 'b') {
                    field[i][j] += 4000;
                    createWaves(j, i, playerNumber, 20);
                } else if (currField == 'i') {
                    field[i][j] += 4500;
                    createWaves(j, i, playerNumber, 22);
                }

            }
        }

    }

    public void createReachableField() {
        int height = board.getHeight();
        int width = board.getWidth();

        specialFieldListSidePath = new ArrayList<>();
        specialFieldListMainPath = new ArrayList<>();
        followFieldsPath = new ArrayList<>();
        reachableField = new int[height][width];

        field = new int[height][width];

        traverseMapForEachPlayerStone();
        alterCurrentMap();
        initialField = new int[height][width];

        for (int y = 0; y < height; y++) {
            if (width >= 0) System.arraycopy(field[y], 0, initialField[y], 0, width);
        }
    }

    private void changeFields(int x, int y, int[] currentDirection){
        if (!(x < 0 || x >= board.getWidth() || y < 0 || y >= board.getHeight())) {
            if (board.getField()[y][x] == '-') {
                return;
            }
        }

        if (!(x < 0 || x >= board.getWidth() || y < 0 || y >= board.getHeight())) {
            if (board.getField()[y][x] == '-') {
                return;
            }
        }

        //Skip the field itself
        x = x + currentDirection[0];
        y = y + currentDirection[1];

        //Follow this direction until the End of the board
        while (true) {

            // end the loop if the end of the board is reached
            if (x < 0 || x >= board.getWidth() || y < 0 || y >= board.getHeight() || board.getField()[y][x] == '-') {
                int directionValue = Direction.indexOf(currentDirection);
                int[] tempStone = new int[3];
                tempStone[0] = x - currentDirection[0];
                tempStone[1] = y - currentDirection[1];
                tempStone[2] = directionValue;

                Transition transition = board.getTransition(x - currentDirection[0], y - currentDirection[1], directionValue);
                boolean transitionAlreadyTaken = false;
                for (int[] specialFields : followFieldsPath) {
                    if (Arrays.equals(specialFields, tempStone)) {
                        transitionAlreadyTaken = true;
                        break;
                    }
                }
                if (transitionAlreadyTaken) {
                    break;
                } else {
                    followFieldsPath.add(tempStone);
                }

                // Follow Transactions
                if (transition != null) {
                    int[] destination = transition.getDestination();

                    x = destination[0];
                    y = destination[1];
                    currentDirection = Direction.valueOf((destination[2] + 4) % 8);
                    continue;
                }
                break;
            }

            //Mark current Field as finished
            if(reachableField[y][x] != 4 && reachableField[y][x] != 3) {
                reachableField[y][x] = 1;
            }

            //Go to the next Field
            x = x + currentDirection[0];
            y = y + currentDirection[1];

        }
    }

    private void followFields(int x, int y, int[] currentDirection){

        int[][] directions = {{0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}};

        changeFields(x,y,currentDirection);
        followFieldsPath.clear();

        // TODO: [Benedikt] System.out.println("X: " + x + " Y: " + y);

        //Follow this direction until the End of the board
        while (true){


            //currentStone is the current Stone and Direction, this is needed to prevent infinite loops where transactions are towards another
            int [] currStone = new int[3];
            currStone[0] = x;
            currStone[1] = y;

            // end the loop if the end of the board is reached
            if (x < 0 || x >= board.getWidth() || y < 0 || y >= board.getHeight() || board.getField()[y][x] == '-') {
                int directionValue = Direction.indexOf(currentDirection);
                int [] tempStone = new int[3];
                tempStone[0] = x - currentDirection[0];
                tempStone[1] = y - currentDirection[1];
                tempStone[2] = directionValue;

                Transition transition = board.getTransition(x - currentDirection[0], y - currentDirection[1], directionValue);
                boolean transitionAlreadyTaken = false;
                for(int[] specialFields : specialFieldListMainPath){
                    if(Arrays.equals(specialFields, tempStone)){
                        transitionAlreadyTaken = true;
                        break;
                    }
                }


                // Follow Transactions
                if (transition != null) {
                    if(transitionAlreadyTaken){
                        break;
                    }else{
                        specialFieldListMainPath.add(tempStone);
                    }

                    int[] destination = transition.getDestination();

                    x = destination[0];
                    y = destination[1];
                    currentDirection = Direction.valueOf((destination[2]+4)%8);
                    threeToFour();
                    continue;
                }
                break;
            }

            //look for other stones in all directions
            for (int[] direction : directions) {

                int nextX = x + direction[0];
                int nextY = y + direction[1];
                boolean transitionAlreadyTaken = false;
                currStone[2] = Direction.indexOf(direction);

                //if the function already went along this transaction, prevent it from doing it again
                for(int[] specialFields : specialFieldListSidePath){
                    if(Arrays.equals(specialFields, currStone)){
                        transitionAlreadyTaken = true;
                        break;
                    }
                }
                if(transitionAlreadyTaken){
                    continue;
                }

                // avoids IndexOutOfBounds exception                                                     required for intermap transactions
                if (nextX < 0 || nextX >= board.getWidth() || nextY < 0 || nextY >= board.getHeight() || board.getField()[nextY][nextX] == '-') {
                    int directionValue = Direction.indexOf(direction);

                    Transition transition = board.getTransition(x, y, directionValue);

                    // Follow Transactions
                    if (transition != null) {
                        int oppositeX;
                        int oppositeY;

                        int[] destination = transition.getDestination();
                        int oppositeDestValue = (directionValue + 4) % 8;

                        // Ignore the transition directly after the Function went through it
                        if(oppositeDestValue == Direction.indexOf(currentDirection)){
                            continue;
                        }
                        //Ignore the transition, if the destination is 1
                        if(reachableField[destination[1]][destination[0]] == 1){
                            continue;
                        }

                        if(board.getField()[destination[1]][destination[0]] == '0'){
                            continue;
                        }

                        // Follow fields if they are reachable
                        boolean searchForFours = false;

                        if(reachableField[y][x] != 4){
                            searchForFours = true;
                        }

                        if(reachableField[destination[1]][destination[0]] == 4 && !searchForFours){
                            continue;
                        }

                        int oppositeDest = (destination[2] + 4) % 8;

                        //set the current Stone as the start of the transitions
                        specialFieldListSidePath.add(currStone);

                        //Follow the transition
                        threeToFour();
                        followFields(destination[0], destination[1], Direction.valueOf(oppositeDest));

                        /*
                        int i = 0;
                        //Remove the finished field from the blocked List
                        for(int[] specialFields : specialFieldListSidePath){
                            if(Arrays.equals(specialFields, currStone)){
                                specialFieldListSidePath.remove(i);
                                break;
                            }
                            i++;
                        }
                        */

                        int[] oppositeDirection = new int[2];
                        oppositeDirection[1] = direction[1]*(-1);
                        oppositeDirection[0] = direction[0]*(-1);

                        int[] oppositeCurrDirection = new int[2];
                        oppositeCurrDirection[0] = currentDirection[0]*(-1);
                        oppositeCurrDirection[1] = currentDirection[1]*(-1);

                        //Go in the other direction
                        oppositeX = x + direction[0]*(-1);
                        oppositeY = y + direction[1]*(-1);

                        if (oppositeX < 0 || oppositeX >= board.getWidth() || oppositeY < 0 || oppositeY >= board.getHeight() || board.getField()[oppositeY][oppositeX] == '-') {

                            directionValue = Direction.indexOf(oppositeDirection);
                            transition = board.getTransition(oppositeX, oppositeY, directionValue);

                            if (transition != null) {

                                destination = transition.getDestination();

                                if(oppositeDest == Direction.indexOf(currentDirection) || Direction.indexOf(direction) == Direction.indexOf(oppositeCurrDirection)) {
                                    specialFieldListSidePath.add(currStone);
                                    threeToFour();
                                    followFields(destination[0], destination[1], Direction.valueOf(destination[2]));

                           /*         i = 0;
                                    //Remove the finished field from the blocked List
                                    for(int[] specialFields : specialFieldListSidePath){
                                        if(Arrays.equals(specialFields, currStone)){
                                            specialFieldListSidePath.remove(i);
                                            break;
                                        }
                                        i++;
                                     }
                            */

                                }
                            }
                        }else{
                            followFields(oppositeX, oppositeY,oppositeDirection);
                        }
                    }

                } else {

                    // Follow fields if they are reachable
                    boolean searchForFours = false;

                    if(reachableField[y][x] != 4){
                        searchForFours = true;
                    }

                    char currChar = board.getField()[nextY][nextX];
                    if ((currChar != '-' &&  currChar != '0' && currChar != 'b' && currChar != 'c'&& currChar != 'i') || reachableField[nextY][nextX] == 3 || reachableField[nextY][nextX] == 4 || reachableField[nextY][nextX] == 1) {

                        //changeFields(x,y,direction);

                        //remains not active until further research
                        //if((currChar != '-' &&  currChar != '0' && currChar != 'b' && currChar != 'c'&& currChar != 'i') && reachableField[nextY][nextX] == 4 && !searchForFours){
                        //    continue;
                        //}

                        //cancel the current iteration if the Neighboring Field is already finished
                        if(reachableField[nextY][nextX] == 4 && !searchForFours){
                            continue;
                        }

                        if(reachableField[nextY][nextX] == 1){
                            continue;
                        }

                        int[] oppositeDirection = new int[2];
                        oppositeDirection[1] = direction[1]*(-1);
                        oppositeDirection[0] = direction[0]*(-1);
                        int newY = y + oppositeDirection[1];
                        int newX = x + oppositeDirection[0];

                        int[] oppositeCurrDirection = new int[2];
                        oppositeCurrDirection[0] = currentDirection[0]*(-1);
                        oppositeCurrDirection[1] = currentDirection[1]*(-1);

                        //dont follow fields in the direction the function is heading and backwards / Directions are required for comparison
                        if(!(Direction.indexOf(currentDirection) == Direction.indexOf(direction) || Direction.indexOf(direction) == Direction.indexOf(oppositeCurrDirection))){
                            //follow the Field in both directions
                            threeToFour();
                            reachableField[y][x] = 3;
                            followFields(nextX,nextY, direction);
                            threeToFour();
                            reachableField[y][x] = 3;
                            followFields(newX,newY,oppositeDirection);
                            threeToFour();
                            reachableField[y][x] = 3;
                        }

                    }
                }

            }

            //Mark current Field as finished
            reachableField[y][x] = 4;

            //Go to the next Field
            x = x + currentDirection[0];
            y = y + currentDirection[1];

        }
    }

    /**
     * alters all 3's in the reachableField to 4's
     */
    private void threeToFour(){
        int height = board.getHeight();
        int width = board.getWidth();

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {

                int currField = reachableField[i][j];

                if (currField == 3) {
                    reachableField[i][j] = 4;
                }
            }
        }
    }


    private void traverseMapForEachPlayerStone() {

        int height = board.getHeight();
        int width = board.getWidth();

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                char currField = board.getField()[i][j];

                if(currField == '1' || currField == '2' ||currField == '3' ||currField == '4' ||
                        currField == '5' ||currField == '6' ||currField == '7' || currField == '8' ||currField == 'x'){
                    //Skip stones that have already been reached
                    if(reachableField[i][j]!=4){
                        traverseMap(j,i);
                    }
                }

            }
        }
        threeToFour();
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
                    field[i][j] = Integer.MIN_VALUE;
                    //board.getField()[i][j] = '-';
                }
            }
        }
    }

    /**
     * Looks in all directions of the field and over Transitions to find possible Move-directions
     */
    private void traverseMap(int x, int y) {
        int[][] directions = {{0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}};
        int[] currentDirection;

        //mark field as being worked on
        reachableField[y][x] = 3;

        for (int[] direction : directions) {
            currentDirection = direction;
            int[] oppositeDirection = Direction.valueOf((Direction.indexOf(currentDirection) + 4) % 8);

            int nextX = x + currentDirection[0];
            int nextY = y + currentDirection[1];

            // avoids IndexOutOfBounds exception                                                     required for intermap transactions
            if (nextX < 0 || nextX >= board.getWidth() || nextY < 0 || nextY >= board.getHeight() || board.getField()[nextY][nextX] == '-') {
                int directionValue = Direction.indexOf(direction);

                Transition transition = board.getTransition(x, y, directionValue);

                // Follow Transactions
                if (transition != null) {
                    int[] destination = transition.getDestination();

                    //look what the char of the destination field is, to determine whether  a move is possible
                    char currChar = board.getField()[destination[1]][destination[0]];
                    if (currChar != '-' && currChar != '0' && currChar != 'b' && currChar != 'c'&& currChar != 'i') {
                        followFields(x, y, currentDirection);
                        //Also look in the opposite direction, because now all fields in this line are reachable
                        followFields(x, y, oppositeDirection);
                    }
                }

            } else {
                // Follow fields if they are reachable
                char currChar = board.getField()[nextY][nextX];
                if (currChar != '-' && currChar != '0' && currChar != 'b' && currChar != 'c'&& currChar != 'i' && reachableField[nextY][nextX] != 4){
                    followFields(x, y, currentDirection);
                    //Also look in the opposite direction, because now all fields in this line are reachable
                    followFields(x, y, oppositeDirection);
                }
            }

        }
    }
    /**
     * Calculates the Map-Score for the given Player
     *
     * @return int with the value of the Player-Score for the given player
     */
    public int calculateScoreForPlayer(char playerNumber) {
        int height = board.getHeight();
        int width = board.getWidth();
        int playerScore = 0;

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {

                char currField = board.getField()[i][j];

                if (currField == playerNumber) {
                    playerScore += field[i][j];
                }
            }
        }
        return playerScore;
    }
    //TODO klasse hat als attribut ein int array das die Einzelnen Scores der player verwaltet
  //  pubilc void calculateScoreForPlayer3(List<int[]> changedFields){
//
  //  }

    public int calculateScoreForPlayer2(char playerNumber, Board tmpBoard) {
        int height = tmpBoard.getHeight();
        int width = tmpBoard.getWidth();
        int playerScore = 0;
        int minFieldValue = Integer.MAX_VALUE;
        //TODO fieldScore soll mithilfe
        //find smalles field value
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if(minFieldValue > field[i][j] && field[i][j] != Integer.MIN_VALUE){
                    minFieldValue = field[i][j];
                }
            }
        }

        if(minFieldValue < 0){
            minFieldValue *= (-1);
        }

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {

                char currField = tmpBoard.getField()[i][j];

                if (currField == playerNumber && field[i][j] != Integer.MIN_VALUE) {
                    playerScore += (field[i][j] + minFieldValue);
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

        int[][] directions = {{0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}};
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

                currX = x + (direction[0] * currRange);
                currY = y + (direction[1] * currRange);

                if (currX < 0 || currX >= board.getWidth() || currY < 0 || currY >= board.getHeight() || board.getField()[currY][currX] == '-') {

                    int directionValue = Direction.indexOf(direction);
                    Transition transition;
                    if(currRange == 1){
                        transition = board.getTransition(x, y, directionValue);
                    }else{
                        transition = board.getTransition(oldX, oldY, directionValue);
                    }

                    if (transition != null) {
                        followTransaction(transition,(range - currRange), startValue, exhaustion);
                    }
                    break;

                } else {
                    // remember last reachable Field in case there is a transaction
                    if (board.getField()[currY][currX] != '-' && field[currY][currX] != Integer.MIN_VALUE) {

                        oldX = currX;
                        oldY = currY;

                        if (((startValue) - exhaustion) > 0) {

                            if (currRange % playerNumber == 0) {
                                field[currY][currX] += ((startValue) - exhaustion) ;
                            } else {
                                field[currY][currX] += ((startValue) - exhaustion) *(playerNumber - (currRange % playerNumber)) * omen ;
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

    private void followTransaction(Transition transition, int range, int startValue, int exhaustion) {

        int[][] directions = {{0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}};
        int omen = -1;
        int directionValue;

        //get the opposite side of the given transaction
        int[] newPosition = transition.getDestination();
        int startX = newPosition[0];
        int startY = newPosition[1];
        int[] newDirection = directions[newPosition[2]];
        // new direction must be turned by 180 degrees
        newDirection[0] *= (-1);
        newDirection[1] *= (-1);
        boolean first = true;
        int oldX = startX;
        int oldY = startY;

        while (range >= 0) {

            //Skip the first iteration, because the field the transaction ends in must also be counted
            if (!first) {
                startX += (newDirection[0]);
                startY += (newDirection[1]);
            } else {
                first = false;
            }

            if (startX < 0 || startX >= board.getWidth() || startY < 0 || startY >= board.getHeight() || board.getField()[startY][startX] == '-') {

                directionValue = Direction.indexOf(newDirection);

                transition = board.getTransition(oldX, oldY, directionValue);

                if (transition == null) {
                    return;
                } else {
                    followTransaction(transition, range, startValue, exhaustion);
                    break;
                }
            }else{
                if (board.getField()[startY][startX] != '-' && field[startY][startX] != Integer.MIN_VALUE) {

                    oldX = startX;
                    oldY = startY;

                    if (((startValue) - exhaustion) > 0) {

                        if (range % playerNumber == 0) {
                            field[startY][startX] += ((startValue) - exhaustion) ;
                        } else {
                            field[startY][startX] += ((startValue) - exhaustion) *(playerNumber - (range % playerNumber)) * omen ;
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

        int[][] directions = {{0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}};
        int[] currentDirection;
        int currNumbers = 0;

        for (int[] direction : directions) {
            currentDirection = direction;

            int nextX = x + currentDirection[0];
            int nextY = y + currentDirection[1];

            // avoids IndexOutOfBounds exception
            if (nextX < 0 || nextX >= board.getWidth() || nextY < 0 || nextY >= board.getHeight()) {
                int directionValue = Direction.indexOf(direction);

                Transition transition = board.getTransition(x, y, directionValue);

                if (transition != null) {
                    continue;
                }
                currNumbers++;
            } else {
                if(board.getField()[nextY][nextX] == '-' || field[nextY][nextX] == Integer.MIN_VALUE){
                    currNumbers++;
                }
            }
        }
        return currNumbers;
    }

    @Override
    public String toString() {
        StringBuilder boardString = new StringBuilder();

        for (int y = 0; y < board.getHeight(); y++) {
            for (int x = 0; x < board.getWidth(); x++) {
                if(reachableField[y][x] == 0){
                    boardString.append(" -");
                    continue;
                }
                boardString.append(String.format("%2s", reachableField[y][x]));
            }
            boardString.append("\n");
        }


        return boardString.toString();
    }

    public String getBoardValues() {
        StringBuilder boardString = new StringBuilder();

        for (int y = 0; y < board.getHeight(); y++) {
            for (int x = 0; x < board.getWidth(); x++) {
                if(field[y][x] == Integer.MIN_VALUE){
                    boardString.append(String.format("%4s", "inf"));
                }else{
                    boardString.append(String.format("%4s", field[y][x]));
                }
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
        createField();
    }

    public int getPlayerNumber(){
        return playerNumber;
    }

    public void setPlayerNumber(int playerNumber){
        this.playerNumber = playerNumber;
        createField();
    }

    public int[][] getReachableField() {
        return reachableField;
    }

    public boolean isReachableFinished() {
        return reachableFinished;
    }

    public void setReachableField(int[][] reachableField) {
        this.reachableField = reachableField;
    }
}
