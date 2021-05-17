
package mapanalyze;
import loganalyze.additional.AnalyzeParser;
import map.Board;
import map.Direction;
import map.Player;
import map.Transition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapAnalyzer {

    public static final int REACHABLE = 4;
    private static final int IN_PROGRESS = 3;
    private static final int MARKED = 1;
    public static final int UNREACHABLE = 0;

    private static AnalyzeParser analyzeParser;
    private boolean reachableFinished;

    private int[][] field;
    private int[][] initialField;
    private Board board;
    private int[][] reachableField;
    private List<int[]> specialFieldListSidePath;
    private List<int[]> specialFieldListMainPath;
    private List<int[]> followFieldsPath;
    private int[][] visibleField;
    private int[][] tmpField;
    private int playerNumber;
    private int width;
    private int height;


    public MapAnalyzer(Board b, int pNumber, AnalyzeParser analyzeParser) {
        MapAnalyzer.analyzeParser = analyzeParser;

        board = b;
        playerNumber = pNumber;

        initAllFields();
        try {
            long time = System.currentTimeMillis();
            createReachableField();
            // TODO: [Benedikt] System.out.println("Map Analyze Zeit: " + (System.currentTimeMillis() - time));
            reachableFinished = true;
        } catch (StackOverflowError soe) {
            analyzeParser.mapAnalyzerError();
            reachableFinished = false;
        }

        createField();
    }

    private void initAllFields(){
        height = board.getHeight();
        width = board.getWidth();

        reachableField = new int[height][width];

        //init Array
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                reachableField[i][j] = 0;
            }
        }

        field = new int[height][width];
        initialField = new int[height][width];

        visibleField = new int[height][width];

    }

    /**
     * creates a Field that contains only reachable Fields and values every Field by location and location to other Fields
     */
    public void createField() {

        //Field muss zurückgesetzt werden, da createField sehr oft aufgerufen wird
        for (int y = 0; y < height; y++) {
            if (width >= 0) System.arraycopy(initialField[y], 0, field[y], 0, width);
        }

        int wavelenght = playerNumber + 1;

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {

                char currField = board.getField()[i][j];

                if (currField != '-' && field[i][j] != Integer.MIN_VALUE) {
                    int newValue = getLocationValue(j, i);
                    int multiplier;
                    if (newValue == 7) {
                        multiplier = 25;
                        field[i][j] += newValue * multiplier * 2;
                        createWaves(j, i, wavelenght, (newValue * multiplier)/4);
                    } else if (newValue == 6) {
                        multiplier = 15;
                        field[i][j] += newValue * multiplier * 2;
                        createWaves(j, i, wavelenght, (newValue * multiplier)/4);
                    } else if (newValue == 5) {
                        multiplier = 10;
                        field[i][j] += newValue * multiplier * 2;
                        createWaves(j, i, wavelenght, (newValue * multiplier)/4);
                    }else {
                        multiplier = 8;
                        field[i][j] += newValue * multiplier *2;
                        createWaves(j, i, wavelenght, newValue);
                    }

                    if (currField == 'c') {
                        field[i][j] += 10000;
                        createWaves(j, i, wavelenght, 25);
                    } else if (currField == 'b') {
                        field[i][j] += 8000;
                        createWaves(j, i, wavelenght, 20);
                    } else if (currField == 'i') {
                        field[i][j] += 9000;
                        createWaves(j, i, wavelenght, 22);
                    }
                }

            }
        }

    }

    /**
     * Marks a special Stone at a position as activated
     */
    public void activateSpecialStone(int x, int y, char type) {
        if (type == 'c') {
            field[y][x] -= 10000;
            createWaves(x, y, playerNumber, -25);
        } else if (type == 'b') {
            field[y][x] -= 8000;
            createWaves(x, y, playerNumber, -20);
        } else if (type == 'i') {
            field[y][x] -= 9000;
            createWaves(x, y, playerNumber, -22);
        }
    }

    public void createReachableField() {

        specialFieldListSidePath = new ArrayList<>();
        specialFieldListMainPath = new ArrayList<>();
        followFieldsPath = new ArrayList<>();

        traverseMapForEachPlayerStone();
        alterCurrentMap();

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

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {

                int currField = reachableField[i][j];

                if (currField == 3) {
                    reachableField[i][j] = 4;
                }
            }
        }
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

    public void createVisibleField(char playerNumber) {

        tmpField = createHelperField();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if(board.getField()[i][j] == playerNumber){
                    findVisibleFields(j, i, board.getPlayerAmount());
                }
            }
            }
        makeFieldsVisible();

    }

    private void findVisibleFields(int x, int y, int radius) {

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

                transition = board.getTransition(x, y, directionValue);

                if (transition != null) {
                    if(tmpField[transition.getY()][transition.getX()] < radius) {
                        findVisibleFields(transition.getX(), transition.getY(), radius - 1);
                    }
                }

            } else {
                if ((tmpField[y][x] - 1) > tmpField[newY][newX]) {
                    findVisibleFields(newX, newY, radius - 1);
                }
            }
        }
    }

    private void makeFieldsVisible() {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (tmpField[i][j] != 0 && tmpField[i][j] != -1) {
                    visibleField[i][j] = 1;
                }
            }
        }
    }


    private void traverseMapForEachPlayerStone() {

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
    public int calculateScoreForPlayerOLD(char playerNumber, Board tmpBoard) {
        int playerScore = 0;
        int minFieldValue = Integer.MAX_VALUE;

        //find smallest field value
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if(minFieldValue > field[i][j]){
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

                if (currField == playerNumber) {
                    playerScore += (field[i][j] + minFieldValue);
                }
            }
        }
        return playerScore;
    }

    //TODO klasse hat als attribut ein int array das die Einzelnen Scores der player verwaltet
    public Board updatePlayerScores(List<int[]> changedFields, char currentPlayer, Board tmpBoard){

        int[] playerScores = tmpBoard.getPlayerScores();
        int[] fieldLocation;
        int fieldScore;
        char oldFieldValue;

        for(int i = 0; i < changedFields.size(); i++){
            fieldLocation = changedFields.get(i);
            fieldScore = field[fieldLocation[0]][fieldLocation[1]];
            oldFieldValue = tmpBoard.getField()[fieldLocation[0]][fieldLocation[1]];


            playerScores[currentPlayer-1] += fieldScore;

            if(oldFieldValue == '1' || oldFieldValue == '2'||
                    oldFieldValue == '3'|| oldFieldValue == '4'|| oldFieldValue == '5'|| oldFieldValue == '6'
                    || oldFieldValue == '7'|| oldFieldValue == '8'){

                playerScores[oldFieldValue-1] -= fieldScore;
            }
        }

        tmpBoard.setPlayerScores(playerScores);
        return tmpBoard;
    }

    public Board initPlayerScores(Board tmpBoard){
        int height = tmpBoard.getHeight();
        int width = tmpBoard.getWidth();
        int minFieldValue = Integer.MAX_VALUE;
        int[] playerScores = new int[tmpBoard.getPlayerAmount()];
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

                    if (currField == '1'||  currField == '2'||currField == '3'||currField == '4'||
                            currField == '5'||currField == '6'||currField == '7'||currField == '8'||field[i][j] != Integer.MIN_VALUE) {
                        playerScores[Integer.parseInt(String.valueOf(currField))] += (field[i][j] + minFieldValue);
                    }
                }
            }

        tmpBoard.setPlayerScores(playerScores);
        return tmpBoard;
    }

    /**
     * Creates a wave in all directions that changes sign every Field and gets smaller.
     *
     */
    private void createWaves(int x, int y, int range, int startValue) {

        int[][] directions = {{0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}};
        int omen = -1;
        int currX ;
        int currY ;
        int oldX = x;
        int oldY = y;
        // go in each direction
        for (int[] direction : directions) {
            // go until the range runs out or there are no more reachable fields
            for (int currRange = 1; currRange < range; currRange++) {

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
                        followTransaction(transition,(range - currRange), startValue);
                    }
                    break;

                } else {
                    // remember last reachable Field in case there is a transaction
                    if (board.getField()[currY][currX] != '-' && field[currY][currX] != Integer.MIN_VALUE) {

                        oldX = currX;
                        oldY = currY;

                            if (currRange % playerNumber == 0) {
                                field[currY][currX] += ((startValue)) ;
                            } else {
                                field[currY][currX] += ((startValue)) *(playerNumber - (currRange % playerNumber)) * omen ;
                            }
                        }
                }
            }
        }

    }

    private void followTransaction(Transition transition, int range, int startValue) {

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
                    followTransaction(transition, range-1, startValue);
                    break;
                }
            }else{
                if (board.getField()[startY][startX] != '-' && field[startY][startX] != Integer.MIN_VALUE) {

                    oldX = startX;
                    oldY = startY;



                        if (range % playerNumber == 0) {
                            field[startY][startX] += (startValue);
                        } else {
                            field[startY][startX] += ((startValue)) *(playerNumber - (range % playerNumber)) * omen ;
                        }
                    }
                }
            }

            range--;
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
                    boardString.append(String.format("%5s", "inf"));
                }else{
                    boardString.append(String.format("%5s", field[y][x]));
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
