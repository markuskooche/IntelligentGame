
package mapanalyze;
import map.Player;
import timelimit.TimeExceededException;
import timelimit.Token;
import loganalyze.additional.AnalyzeParser;
import map.Board;
import map.Direction;
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
    private List<int[]> interestingBonusFieldList;
    private List<int[]> interestingChoiceFieldList;
    private List<int[]> interestingInversionFieldList;
    private List<int[]> interestingCornerFieldList;
    private int[][] visibleField;
    private int[][] tmpField;
    private int playerNumber;
    private int width;
    private int height;
    private int minFieldValue;

    private Token timeToken = new Token();
    private boolean timeLimited = false;
    private boolean failedToSetup = false;


    public MapAnalyzer(Board b, int pNumber, AnalyzeParser analyzeParser) {
        MapAnalyzer.analyzeParser = analyzeParser;

        board = b;
        playerNumber = pNumber/2;
        if(playerNumber < 2){
            playerNumber = 2;
        }

        initAllFields();
        reachableFinished = false;
        createField();
        minFieldValue = getMinFieldValue();
    }

    public void startReachableField(boolean timeLimited, Token timeToken) throws TimeExceededException {
        this.timeLimited = timeLimited;
        this.timeToken = timeToken;
        try {
            createReachableField();
            reachableFinished = true;
            board.setReachableField(reachableField);
        } catch (Exception e) {
            analyzeParser.mapAnalyzerError();
            failedToSetup = true;
        }
    }

    private void initAllFields(){
        //init width and height for future calculations
        width = board.getWidth();
        height = board.getHeight();

        // init reachableField that contains all Fields that are reachable on the current map. 4 = reachable 0 equals not reachable
        reachableField = new int[height][width];
        // init field: field contains the calculated Scores of the map for each field
        field = new int[height][width];
        // initial field is a copy of the field array. It is used to reset the field array on recalculation
        initialField = new int[height][width];
        // init the visibleFiled: all 1's are Fields that are near our stones.
        visibleField = new int[height][width];
        // init the interestingFieldList. It contains interesting fields like corners or the position of special stones
        interestingBonusFieldList = new ArrayList<>();
        interestingCornerFieldList = new ArrayList<>();
        interestingChoiceFieldList = new ArrayList<>();
        interestingInversionFieldList = new ArrayList<>();
    }

    /**
     * creates a Field that contains only reachable Fields and values every Field by location and location to other Fields
     */
    public void createField() {

        //field reset is important to prevent endless addition of values.
        for (int y = 0; y < height; y++) {
            if (width >= 0) System.arraycopy(initialField[y], 0, field[y], 0, width);
        }

        //waveLength is the number of Fields a calculated Field influenced adjacent Fields
        int waveLength = 1 + 1;

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {

                //currField is the value of the current Field of the Board eg. 1,2 or b, c
                char currField = board.getField()[i][j];

                if (currField != '-' && field[i][j] != Integer.MIN_VALUE) {
                    //calculates the value for the current field position
                    int newValue = getLocationValue(j, i);
                    // multiplier is used to increase the values oft he Fields depending on the number of adjacent Fields
                    int multiplier;
                    // basic Case: a field that is only reachable by 1 destination or 7 Sides are blocked
                    if (newValue == 7) {
                        //set multiplier
                        multiplier = 25;
                        //set the value of the current evaluated field
                        field[i][j] += newValue * multiplier * 4;
                        // set the values of the Fields adjacent to the current evaluated field
                        createWaves(j, i, waveLength, (newValue * multiplier)/8);
                    } else if (newValue == 6) {
                        multiplier = 15;
                        field[i][j] += newValue * multiplier * 4;
                        createWaves(j, i, waveLength, (newValue * multiplier)/8);
                    } else if (newValue == 5) {
                        multiplier = 10;
                        field[i][j] += newValue * multiplier * 4;
                        createWaves(j, i, waveLength, (newValue * multiplier)/8);
                    }else {
                        multiplier = 8;
                        field[i][j] += newValue * multiplier * 4;
                        createWaves(j, i, waveLength, newValue/3);
                    }
                    int[] position = new int[2];
                    position[0] = j; //set x position
                    position[1] = i; //set y position
                    // to make them more appealing the current field gets a bonus value if the field is a bonus, choice or inversion field
                    if (currField == 'c') {
                        field[i][j] += 10000;
                        createWaves(j, i, waveLength, 250);
                        interestingChoiceFieldList.add(new int[]{j,i});
                    } else if (currField == 'b') {
                        field[i][j] += 8000;
                        createWaves(j, i, waveLength, 200);
                        interestingBonusFieldList.add(new int[]{j,i});
                    } else if (currField == 'i') {
                        field[i][j] += 9000;
                        createWaves(j, i, waveLength, 220);
                        interestingInversionFieldList.add(new int[]{j,i});
                    }
                }
            }
        }
    }

    /**
     * Marks a special Stone at a position as activated
     *
     * @param type is the type of bonus field c, b or i
     */
    public void activateSpecialStone(int x, int y, char type) {

        int waveLength = playerNumber + 1;

        if (type == 'c') {
            field[y][x] -= 10000;
            createWaves(x, y, waveLength, -250);
        } else if (type == 'b') {
            field[y][x] -= 8000;
            createWaves(x, y, waveLength, -200);
        } else if (type == 'i') {
            field[y][x] -= 9000;
            createWaves(x, y, waveLength, -220);
        }
    }

    /**
     * creates the ReachableField for the current map and
     */
    public void createReachableField() throws TimeExceededException {
        // has all Transitions that are already taken from the recursive SidePaths
        specialFieldListSidePath = new ArrayList<>();
        // has all Transitions that are already taken from the iterative MainPath this is done due to performance reasons and to prevent endless loops
        specialFieldListMainPath = new ArrayList<>();
        followFieldsPath = new ArrayList<>();

        traverseMapForEachPlayerStone();
        alterCurrentMap();
        // safe the current initialField state of the current reachable field
        for (int y = 0; y < height; y++) {
            if (width >= 0) System.arraycopy(field[y], 0, initialField[y], 0, width);
        }
    }

    /**
     *
     * @param currentDirection is the current direction the Fields are marked
     */
    private void changeFields(int x, int y, int[] currentDirection){
        //this is necessary to prevent bugs regarding the skipping and marking of unreachable fields
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
                //create tmpStone
                int[] tempStone = new int[3];
                tempStone[0] = x - currentDirection[0];
                tempStone[1] = y - currentDirection[1];
                tempStone[2] = directionValue;

                //determine whether the transition was already taken
                boolean transitionAlreadyTaken = false;
                for (int[] specialFields : followFieldsPath) {
                    if (Arrays.equals(specialFields, tempStone)) {
                        transitionAlreadyTaken = true;
                        break;
                    }
                }
                //if the transition is new add it to the list, or else stop the loop
                if (transitionAlreadyTaken) {
                    break;
                } else {
                    followFieldsPath.add(tempStone);
                }

                // transition
                Transition transition = board.getTransition(tempStone[0], tempStone[1], tempStone[2]);

                // Follow Transactions
                if (transition != null) {
                    int[] destination = transition.getDestination();
                    //set the new destination
                    x = destination[0];
                    y = destination[1];
                    //alter the direction
                    currentDirection = Direction.getOppositeDirection(destination[2]);
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
    /**
    *Main method to determine the reachable fields
     */
    private void followFields(int x, int y, int[] currentDirection) throws TimeExceededException {

        //Mark the fields ahead
        changeFields(x,y,currentDirection);
        //unlock the transitions that have been used to mark the fields
        followFieldsPath.clear();

        //Follow this direction until the End of the board
        while (true){

            if (timeLimited && timeToken.timeExceeded()) throw new TimeExceededException();
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

                //find out if the transition is already taken
                boolean transitionAlreadyTaken = false;
                for(int[] specialFields : specialFieldListMainPath){
                    if (timeLimited && timeToken.timeExceeded()) throw new TimeExceededException();
                    if(Arrays.equals(specialFields, tempStone)){
                        transitionAlreadyTaken = true;
                        break;
                    }
                }
                //find new transition
                Transition transition = board.getTransition(tempStone[0], tempStone[1] , tempStone[2]);
                // Follow Transactions
                if (transition != null) {
                    if(transitionAlreadyTaken){
                        break;
                    }else{
                        specialFieldListMainPath.add(tempStone);
                    }

                    //set everything for the next iteration
                    int[] destination = transition.getDestination();
                    x = destination[0];
                    y = destination[1];
                    currentDirection = Direction.getOppositeDirection(destination[2]);
                    threeToFour();
                    continue;
                }
                break;
            }

            //look for other stones in all directions
            for (int[] direction : Direction.getList()) {
                if (timeLimited && timeToken.timeExceeded()) throw new TimeExceededException();
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
                        int oppositeDestValue = Direction.getOppositeDirectionValue(directionValue);

                        // Ignore the transition directly after the Function went through it
                        if(oppositeDestValue == Direction.indexOf(currentDirection)){
                            continue;
                        }
                        //Ignore the transition, if the destination is 1
                        if(reachableField[destination[1]][destination[0]] == 1){
                            continue;
                        }
                        //Ignore the transition, if the destination is 0
                        if(board.getField()[destination[1]][destination[0]] == '0' && reachableField[destination[1]][destination[0]] == 0 ){
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
                        reachableField[y][x] = 3;
                        followFields(destination[0], destination[1], Direction.valueOf(oppositeDest));

                        //get the opposite directions
                        int[] oppositeDirection = Direction.getOppositeDirection(direction);

                        int[] oppositeCurrDirection = Direction.getOppositeDirection(currentDirection);

                        //Go in the other direction
                        oppositeX = x + direction[0]*(-1);
                        oppositeY = y + direction[1]*(-1);

                        if (oppositeX < 0 || oppositeX >= board.getWidth() || oppositeY < 0 || oppositeY >= board.getHeight() || board.getField()[oppositeY][oppositeX] == '-') {

                            //Search for transitions
                            directionValue = Direction.indexOf(oppositeCurrDirection);
                            transition = board.getTransition(x, y, directionValue);

                            if (transition != null) {

                                destination = transition.getDestination();

                                    specialFieldListSidePath.add(currStone);
                                    threeToFour();
                                    reachableField[y][x] = 3;
                                    followFields(destination[0], destination[1], Direction.valueOf(destination[2]));
                            }
                        }else{
                            //go in the opposite direction
                            followFields(oppositeX, oppositeY,oppositeCurrDirection);
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

                        //cancel the current iteration if the Neighboring Field is already finished
                        if(reachableField[nextY][nextX] == 4 && !searchForFours){
                            continue;
                        }

                        if(reachableField[nextY][nextX] == 1){
                            continue;
                        }

                        //find opposite directions
                        int[] oppositeDirection = Direction.getOppositeDirection(direction);
                        int newY = y + oppositeDirection[1];
                        int newX = x + oppositeDirection[0];

                        int[] oppositeCurrDirection = Direction.getOppositeDirection(currentDirection);

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
     * changes all 3's in the reachableField to 4's needed for reachable field detection
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

    /**
     * creates a tmp int array that is used for bomb execution
     * @return a int array that contains 0 for reachable and -1 for unreachable fields
     */
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

    /**
     * Creates the visible field for a certain player
     * @param playerNumber is the current player number as char where the visible fields is needed
     */
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

    /**
     * Recursive method to create visible Fields
     * @param radius is the bombRadius needed for recursion
     */
    private void findVisibleFields(int x, int y, int radius) {
        // end recursion if radius equals 0
        if (radius == 0) {
            return;
        }

        int newX, newY;
        //set tmpField to current radius
        tmpField[y][x] = radius;

        for (int[] direction : Direction.getList()) {
            newX = x + (direction[0]);
            newY = y + (direction[1]);

            //search for transitions on not reachable fields and on the edges of the board
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
                //visit fields again if a shorter Path is found
                if ((tmpField[y][x] - 1) > tmpField[newY][newX]) {
                    findVisibleFields(newX, newY, radius - 1);
                }
            }
        }
    }

    /**
     * sets the visible field = 1 if the current field is visible to the current player
     */
    private void makeFieldsVisible() {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (tmpField[i][j] != 0 && tmpField[i][j] != -1) {
                    visibleField[i][j] = 1;
                }
            }
        }
    }

    /**
     * searches for each player and expansion Stone whether a turn is possible or not.
     *
     * Skips the stone if the position is already reachable
     */
    private void traverseMapForEachPlayerStone() throws TimeExceededException {

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (timeLimited && timeToken.timeExceeded()) throw new TimeExceededException();

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
    private void alterCurrentMap() throws TimeExceededException {

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (timeLimited && timeToken.timeExceeded()) throw new TimeExceededException();
                int currField = reachableField[i][j];

                if (currField == 0) {
                    field[i][j] = Integer.MIN_VALUE;
                }
            }
        }
    }

    /**
     * Looks in all directions of the field and over Transitions to find possible Move-directions
     */
    private void traverseMap(int x, int y) throws TimeExceededException {
        int[][] directions = {{0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}};
        int[] currentDirection;

        //mark field as being worked on
        reachableField[y][x] = 3;

        for (int[] direction : directions) {
            if (timeLimited && timeToken.timeExceeded()) throw new TimeExceededException();

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

    public int calculateScoreForPlayers(Player ourPlayer, Board tmpBoard, Player[] players, int factor) {
        String playersNum = "";
        for(Player p : players) {
            playersNum += p.getCharNumber();
        }

        char tmpBoardfield[][] = tmpBoard.getField();
        double playerScore = 0;
        double allPlayerScore = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {

                char currField = tmpBoardfield[i][j];
                if (currField == ourPlayer.getCharNumber()) {
                    playerScore += (field[i][j] + minFieldValue);
                }
                if (playersNum.indexOf(currField) != -1) {
                    allPlayerScore += (field[i][j] + minFieldValue);
                }
            }
        }

        double tmpMapValue =  playerScore / allPlayerScore;
        return (int) (tmpMapValue * factor);
    }

    private int getMinFieldValue() {
        int minFieldValue = Integer.MAX_VALUE;
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
        return minFieldValue;
    }


    //TODO Board hat playerScores as attribute this is way faster than the currently method

    /**
     * Updates the playerScores for all players
     *
     * @param changedFields Fields that changed player number that turn or are owned at first
     * @param currentPlayer the current player the score is calculated for
     * @param tmpBoard board the current turn is played on
     * @return return the Board with the updated scores
     */
    public Board updatePlayerScores(List<int[]> changedFields, char currentPlayer, Board tmpBoard){

        int[] playerScores = tmpBoard.getPlayerScores();
        int[] fieldLocation;
        int fieldScore;
        char oldFieldValue;

        for (int[] changedField : changedFields) {
            fieldLocation = changedField;
            fieldScore = field[fieldLocation[0]][fieldLocation[1]];
            oldFieldValue = tmpBoard.getField()[fieldLocation[0]][fieldLocation[1]];


            playerScores[currentPlayer - 1] += fieldScore;

            if (oldFieldValue == '1' || oldFieldValue == '2' ||
                    oldFieldValue == '3' || oldFieldValue == '4' || oldFieldValue == '5' || oldFieldValue == '6'
                    || oldFieldValue == '7' || oldFieldValue == '8') {

                playerScores[oldFieldValue - 1] -= fieldScore;
            }
        }

        tmpBoard.setPlayerScores(playerScores);
        return tmpBoard;
    }

    // TODO: Board has playerScores as attribute this is way faster than the currently method
    /**
     * inits the playerScores by initializing the playerScores array in a Board
     * @param tmpBoard board that is initialized
     * @return board with the initialized playerScore
     */
    public Board initPlayerScores(Board tmpBoard){
        int height = tmpBoard.getHeight();
        int width = tmpBoard.getWidth();
        int minFieldValue = Integer.MAX_VALUE;
        int[] playerScores = new int[tmpBoard.getPlayerAmount()];
        //find smallest field value
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
     * @param range sets how many fields the wave alters
     * @param startValue is the value the fields adjacent to the mainField start with
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
                    // follow transition if available
                    if (transition != null) {
                        followTransition(transition,(range - currRange), startValue);
                    }
                    break;

                } else {

                    if (board.getField()[currY][currX] != '-' && field[currY][currX] != Integer.MIN_VALUE) {
                        // remember last reachable Field in case there is a transaction
                        oldX = currX;
                        oldY = currY;

                        //calculate the scores of the adjacent players
                        if (playerNumber > 0) {
                            if (currRange % playerNumber == 0) {
                                field[currY][currX] += ((startValue));
                            } else {
                                field[currY][currX] += ((startValue)) * (playerNumber - (currRange % playerNumber)) * omen;
                            }
                        }else{
                            System.err.println("Error with playerNumber" + playerNumber);
                        }
                    }
                }
            }
        }

    }

    /**
     * continues the wave after a transition is followed
     *
     * @param transition is the transition the wave went through
     * @param range the leftover range of the wave
     * @param startValue the startValue of the wave after the transaction
     */
    private void followTransition(Transition transition, int range, int startValue) {

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
                    followTransition(transition, range-1, startValue);
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
            range--;
            }
        }


    /**
     * Get the value of a field depending on the aligning fields that are reachable
     *
     * @return Location Value = the number of adjacent not reachable fields
     */
    private int getLocationValue(int x, int y) {

        int[] currentDirection;
        int currNumbers = 0;
        StringBuilder sb = new StringBuilder();

        for (int[] direction : Direction.getList()) {
            currentDirection = direction;

            int nextX = x + currentDirection[0];
            int nextY = y + currentDirection[1];

            // avoids IndexOutOfBounds exception
            if (nextX < 0 || nextX >= board.getWidth() || nextY < 0 || nextY >= board.getHeight()) {
                int directionValue = Direction.indexOf(direction);

                Transition transition = board.getTransition(x, y, directionValue);

                if (transition != null) {
                    sb.append('.');
                    continue;
                }
                sb.append('-');
                currNumbers++;
            } else {
                //if the adjacent field is not reachable increase currNumbers
                if(board.getField()[nextY][nextX] == '-' || field[nextY][nextX] == Integer.MIN_VALUE){
                    sb.append('-');
                    currNumbers++;
                }else{
                    sb.append('.');
                }
            }
        }
        //if the field is a real Corner add id to the interesting Field list.
        if(isCorner(sb.toString()) && currNumbers >= 5){
            int[] position = new int[2];
            position[0] = x; //set x position
            position[1] = y; //set y position
            interestingCornerFieldList.add(position);
        }
        return currNumbers;
    }

    public static boolean isCorner(String cornerString) {
        boolean isBroken = false;
        boolean isStarted = false;
        boolean lastLife = false;
        boolean startedWithDot = false;

        if(cornerString.length() == 0){
            return false;
        }

        if(cornerString.charAt(0) == '.'){
            startedWithDot = true;
        }

        for (char letter : cornerString.toCharArray()) {
            if (letter == '-') {

                if (!isStarted) {
                    isStarted = true;
                }

                if (isStarted && isBroken) {
                    lastLife = true;
                }

                if(isStarted && isBroken && startedWithDot){
                    return false;
                }

            } else {

                if(lastLife){
                    return false;
                }

                if (isStarted) {
                    isBroken = true;
                    isStarted = false;
                }
            }
        }
        return true;
    }

    /**
     * returns the reachable field of the current map
     */
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

    /**
     * returns the current calculated field with all values
     */
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

    public List<int[]> getInterestingBonusFieldList() {
        return interestingBonusFieldList;
    }

    public void setInterestingBonusFieldList(List<int[]> interestingBonusFieldList) {
        this.interestingBonusFieldList = interestingBonusFieldList;
    }

    public List<int[]> getInterestingCornerFieldList() {
        return interestingCornerFieldList;
    }

    public void setInterestingCornerFieldList(List<int[]> interestingCornerFieldList) {
        this.interestingCornerFieldList = interestingCornerFieldList;
    }

    public List<int[]> getInterestingChoiceFieldList() {
        return interestingChoiceFieldList;
    }

    public void setInterestingChoiceFieldList(List<int[]> interestingChoiceFieldList) {
        this.interestingChoiceFieldList = interestingChoiceFieldList;
    }

    public List<int[]> getInterestingInversionFieldList() {
        return interestingInversionFieldList;
    }

    public void setInterestingInversionFieldList(List<int[]> interestingInversionFieldList) {
        this.interestingInversionFieldList = interestingInversionFieldList;
    }

    public int[][] getReachableField() {
        return reachableField;
    }

    public int getReachablePiece(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            return reachableField[y][x];
        }

        return UNREACHABLE;
    }

    public boolean isReachableFinished() {
        return reachableFinished;
    }

    public void setReachableField(int[][] reachableField) {
        this.reachableField = reachableField;
    }

    public boolean failedToSetup() {
        return failedToSetup;
    }

    public void resetReachableField() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                reachableField[y][x] = REACHABLE;
            }
        }
    }
}
