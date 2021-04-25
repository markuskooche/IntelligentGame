import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapAnalyzer {

    private int[][] field;
    private Board board;
    private int[][] reachableField; // bool array
    private List<int[]> specialFieldList;

    public MapAnalyzer(Board b) {
        board = b;
        createReachableField();
        createField();

    }

    /**
     * creates a Field that contains only reachable Fields and values every Field by location and location to other Fields
     */
    public void createField() {

        int height = board.getHeight();
        int width = board.getWidth();
        field = new int[height][width];


        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {

                char currField = board.getField()[i][j];

                if (currField != '-') {
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

    private void createReachableField() {
        int height = board.getHeight();
        int width = board.getWidth();

        specialFieldList = new ArrayList<>();
        reachableField = new int[height][width];

        traverseMapForEachPlayerStone();
        alterCurrentMap();
    }

    /**
     * this Method has been replaced with an iterative implementation and will be removed in a future version
     */
    private void followFields(int x, int y, int[] currentDirection){
            followFieldsIterative(x,y,currentDirection);
        /*
        System.out.println("x: " + x + " y: " + y);
       // if(y == 23){
       //     System.out.println("ohne");
       // }
        int[] startStone;
        int [] currStone = new int[3];
        currStone[0] = x;
        currStone[1] = y;

        //System.out.println(printReachableField());
        if (x < 0 || x >= board.getWidth() || y < 0 || y >= board.getHeight() || board.getField()[y][x] == '-') {


/*

            int prevX =  x + currentDirection[0]*(-1);
            int prevY =  y + currentDirection[1]*(-1);

            int directionValue = Direction.indexOf(currentDirection);
            Transition transition = board.getTransition(prevX, prevY, directionValue);

            // Follow Transactions
            if (transition != null) {

                int[] destination = transition.getDestination(prevX, prevY, directionValue);
                //Destination needs to start form the opposite direction;

                followFields(destination[0], destination[1], Direction.valueOf((destination[2]+4)%8));

            }

 */
           /*     return;
        }else{
            // TODO entfernen
            if(reachableField[y][x] == 1){
               return;
            }


            int[][] directions = {{0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}};

    //        if(reachableField[y][x] != 4){
    //            reachableField[y][x] = 3;
    //        }
            for (int[] direction : directions) {

                int nextX = x + direction[0];
                int nextY = y + direction[1];
                boolean transitionAlreadyTaken = false;
                currStone[2] = Direction.indexOf(direction);
                //abbrechen wenn man die Transition bereits abgegagen ist!
                for(int[] specialFields : specialFieldList){
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

                        int[] destination = transition.getDestination(x, y, directionValue);
                        int oppositeDestValue = (directionValue + 4) % 8;
                        int[] destinationValue = Direction.valueOf((destination[2]+4)%8);


                        if(oppositeDestValue == Direction.indexOf(currentDirection)){
                            continue;
                        }
/*
                        if (board.getField()[destination[0] + destinationValue[0]][destination[1] + destinationValue[1]] == 'x' ||
                            board.getField()[destination[0] + destinationValue[0]][destination[1] + destinationValue[1]] == 'b' ||
                            board.getField()[destination[0] + destinationValue[0]][destination[1] + destinationValue[1]] == 'c' ||
                            board.getField()[destination[0] + destinationValue[0]][destination[1] + destinationValue[1]] == 'i' ||
                            board.getField()[destination[0] + destinationValue[0]][destination[1] + destinationValue[1]] == '0' &&
                            reachableField[destination[0] + destinationValue[0]][destination[1] + destinationValue[1]] != 3) {


 *//*
                            int oppositeDest = (destination[2] + 4) % 8;
                            //TODO Fixen dass transaktionen ihren vorhänger aufrufen dürfen


                            specialFieldList.add(currStone);

                                followFields(destination[0], destination[1], Direction.valueOf(oppositeDest));

                            int[] oppositeDirection = new int[2];
                            oppositeDirection[1] = direction[1]*(-1);
                            oppositeDirection[0] = direction[0]*(-1);
                            int newY = y + oppositeDirection[1];
                            int newX = x + oppositeDirection[0];

                            int[] oppsiteCurrDirection = new int[2];
                            oppsiteCurrDirection[0] = currentDirection[0]*(-1);
                            oppsiteCurrDirection[1] = currentDirection[1]*(-1);

                            //Go in the other direction
                             oppositeX = x + direction[0]*(-1);
                             oppositeY = y + direction[1]*(-1);
                            oppositeDirection[0] = direction[1]*(-1);
                            oppositeDirection[1] = direction[0]*(-1);

                            if (oppositeX < 0 || oppositeX >= board.getWidth() || oppositeY < 0 || oppositeY >= board.getHeight() || board.getField()[oppositeY][oppositeX] == '-') {

                                directionValue = Direction.indexOf(oppositeDirection);
                                transition = board.getTransition(oppositeX, oppositeY, directionValue);

                                if (transition != null) {

                                     destination = transition.getDestination(x, y, directionValue);

                                    /*if (board.getField()[destination[0] + destinationValue[0]][destination[1] + destinationValue[1]] == 'x' ||
                                            board.getField()[destination[0] + destinationValue[0]][destination[1] + destinationValue[1]] == 'b' ||
                                            board.getField()[destination[0] + destinationValue[0]][destination[1] + destinationValue[1]] == 'c' ||
                                            board.getField()[destination[0] + destinationValue[0]][destination[1] + destinationValue[1]] == 'i' ||
                                            board.getField()[destination[0] + destinationValue[0]][destination[1] + destinationValue[1]] == '0' &&
                                                    reachableField[destination[0] + destinationValue[0]][destination[1] + destinationValue[1]] != 3) {


                                     *//*
                                        if(oppositeDest == Direction.indexOf(currentDirection) || Direction.indexOf(direction) == Direction.indexOf(oppsiteCurrDirection)) {

                                        followFields(destination[0], destination[1], Direction.valueOf(destination[2]));
                                        }
                                    }
                               // }
                           // }
                        }
                    }

                } else {
                    // Follow fields if they are reachable

                    boolean searchForFours = false;

                    // TODO Wird irgendwie nicht aufgerufen!!
                    if(reachableField[y][x] != 4){
                        searchForFours = true;
                    }

                    char currChar = board.getField()[nextY][nextX];
                    if ((currChar != '-' &&  currChar != '0' && currChar != 'b' && currChar != 'c'&& currChar != 'i') || reachableField[nextY][nextX] == 3 || reachableField[nextY][nextX] == 4) {

                        if((currChar != '-' &&  currChar != '0' && currChar != 'b' && currChar != 'c'&& currChar != 'i') && reachableField[nextY][nextX] == 4 && !searchForFours){
                            continue;
                        }

                        if(reachableField[nextY][nextX] == 4 && !searchForFours){
                            continue;
                        }

                        int[] oppositeDirection = new int[2];
                        oppositeDirection[1] = direction[1]*(-1);
                        oppositeDirection[0] = direction[0]*(-1);
                        int newY = y + oppositeDirection[1];
                        int newX = x + oppositeDirection[0];

                        int[] oppsiteCurrDirection = new int[2];
                        oppsiteCurrDirection[0] = currentDirection[0]*(-1);
                        oppsiteCurrDirection[1] = currentDirection[1]*(-1);

                            //dont follow fields in the direction the function is heading and backwards / Directions are required for comparison
                        if(!(Direction.indexOf(currentDirection) == Direction.indexOf(direction) || Direction.indexOf(direction) == Direction.indexOf(oppsiteCurrDirection))){
                            treeToFour();
                            reachableField[y][x] = 3;
                            followFields(newX,newY,oppositeDirection);
                            treeToFour();
                            reachableField[y][x] = 3;
                            followFields(nextX,nextY, direction);
                            treeToFour();
                            reachableField[y][x] = 3;
                        }


                    }
                }

            }

        }
         /*
        int i = 0;
        for(int[] specialFields : specialFieldList){
            if(Arrays.equals(specialFields, currStone)){
                specialFieldList.remove(i);
                break;
            }
            i++;
        }

        reachableField[y][x] = 4;
        followFields(x + currentDirection[0],y + currentDirection[1], currentDirection);
         */

    }

    private void followFieldsIterative(int x, int y, int[] currentDirection){

        int[][] directions = {{0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}};

        //Follow this direction until the End of the board
        while (true){

        //currentStone is the current Stone and Direction, this is needed to prevent infinite loops where transactions are towards another
        int [] currStone = new int[3];
        currStone[0] = x;
        currStone[1] = y;

            // end the loop if the end of the board is reached
            if (x < 0 || x >= board.getWidth() || y < 0 || y >= board.getHeight() || board.getField()[y][x] == '-') {
                //TODO Sollte noch transitionen folgen!!
                break;
            }
            //look for other stones in all directions
            for (int[] direction : directions) {

                int nextX = x + direction[0];
                int nextY = y + direction[1];
                boolean transitionAlreadyTaken = false;
                currStone[2] = Direction.indexOf(direction);

                //if the function already went along this transaction, prevent it from doing it again
                for(int[] specialFields : specialFieldList){
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

                        int[] destination = transition.getDestination(x, y, directionValue);
                        int oppositeDestValue = (directionValue + 4) % 8;

                        // Ignore the transition directly after the Function went through it
                        if(oppositeDestValue == Direction.indexOf(currentDirection)){
                            continue;
                        }
/*
                        if (board.getField()[destination[0] + destinationValue[0]][destination[1] + destinationValue[1]] == 'x' ||
                            board.getField()[destination[0] + destinationValue[0]][destination[1] + destinationValue[1]] == 'b' ||
                            board.getField()[destination[0] + destinationValue[0]][destination[1] + destinationValue[1]] == 'c' ||
                            board.getField()[destination[0] + destinationValue[0]][destination[1] + destinationValue[1]] == 'i' ||
                            board.getField()[destination[0] + destinationValue[0]][destination[1] + destinationValue[1]] == '0' &&
                            reachableField[destination[0] + destinationValue[0]][destination[1] + destinationValue[1]] != 3) {

 */
                        int oppositeDest = (destination[2] + 4) % 8;

                        //Saft the current Stone as the start of the transitions
                        specialFieldList.add(currStone);

                        //Follow the transition
                        followFields(destination[0], destination[1], Direction.valueOf(oppositeDest));

                        int[] oppositeDirection = new int[2];
                        oppositeDirection[1] = direction[1]*(-1);
                        oppositeDirection[0] = direction[0]*(-1);

                        int[] oppositeCurrDirection = new int[2];
                        oppositeCurrDirection[0] = currentDirection[0]*(-1);
                        oppositeCurrDirection[1] = currentDirection[1]*(-1);

                        //Go in the other direction
                        oppositeX = x + direction[0]*(-1);
                        oppositeY = y + direction[1]*(-1);
                        oppositeDirection[0] = direction[1]*(-1);
                        oppositeDirection[1] = direction[0]*(-1);

                        if (oppositeX < 0 || oppositeX >= board.getWidth() || oppositeY < 0 || oppositeY >= board.getHeight() || board.getField()[oppositeY][oppositeX] == '-') {

                            directionValue = Direction.indexOf(oppositeDirection);
                            transition = board.getTransition(oppositeX, oppositeY, directionValue);

                            if (transition != null) {

                                destination = transition.getDestination(x, y, directionValue);

                                    /*if (board.getField()[destination[0] + destinationValue[0]][destination[1] + destinationValue[1]] == 'x' ||
                                            board.getField()[destination[0] + destinationValue[0]][destination[1] + destinationValue[1]] == 'b' ||
                                            board.getField()[destination[0] + destinationValue[0]][destination[1] + destinationValue[1]] == 'c' ||
                                            board.getField()[destination[0] + destinationValue[0]][destination[1] + destinationValue[1]] == 'i' ||
                                            board.getField()[destination[0] + destinationValue[0]][destination[1] + destinationValue[1]] == '0' &&
                                                    reachableField[destination[0] + destinationValue[0]][destination[1] + destinationValue[1]] != 3) {


                                     */
                                if(oppositeDest == Direction.indexOf(currentDirection) || Direction.indexOf(direction) == Direction.indexOf(oppositeCurrDirection)) {

                                    followFields(destination[0], destination[1], Direction.valueOf(destination[2]));
                                }
                            }
                            // }
                            // }
                        }
                    }

                } else {

                    // Follow fields if they are reachable
                    boolean searchForFours = false;

                    if(reachableField[y][x] != 4){
                        searchForFours = true;
                    }

                    char currChar = board.getField()[nextY][nextX];
                    if ((currChar != '-' &&  currChar != '0' && currChar != 'b' && currChar != 'c'&& currChar != 'i') || reachableField[nextY][nextX] == 3 || reachableField[nextY][nextX] == 4) {

                        //remains not active until further research
                       // if((currChar != '-' &&  currChar != '0' && currChar != 'b' && currChar != 'c'&& currChar != 'i') && reachableField[nextY][nextX] == 4 && !searchForFours){
                       //     continue;
                       // }

                        //cancel the current iteration if the Neighboring Field is already finished
                        if(reachableField[nextY][nextX] == 4 && !searchForFours){
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
                            treeToFour();
                            reachableField[y][x] = 3;
                            followFields(newX,newY,oppositeDirection);
                            treeToFour();
                            reachableField[y][x] = 3;
                            followFields(nextX,nextY, direction);
                            treeToFour();
                            reachableField[y][x] = 3;
                        }

                    }
                }

            }

            //Mark current Field as finished
            reachableField[y][x] = 4;
            int i = 0;
            //Remove the finished field from the blocked List
            for(int[] specialFields : specialFieldList){
                if(Arrays.equals(specialFields, currStone)){
                    specialFieldList.remove(i);
                    break;
                }
                i++;
            }
            //Go to the next Field
             x = x + currentDirection[0];
             y = y + currentDirection[1];

        }
    }

    /**
     * alters all 3's in the reachableField to 4's
     */
    private void treeToFour(){
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
                    int[] destination = transition.getDestination(x, y, directionValue);

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

                currX = x + (direction[1] * currRange);
                currY = y + (direction[0] * currRange);

                if (currX < 0 || currX >= board.getWidth() || currY < 0 || currY >= board.getHeight()) {

                    int directionValue = Direction.indexOf(direction);

                    Transition transition = board.getTransition(oldX, oldY, directionValue);

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

        int[][] directions = {{0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}};
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

            //Skip the first iteration, because the field the transaction ends in must also be counted
            if (!first) {
                startX += (newDirection[1]);
                startY += (newDirection[0]);
            } else {
                first = false;
            }

            if (startX < 0 || startX >= board.getWidth() || startY < 0 || startY >= board.getHeight()) {

                directionValue = Direction.indexOf(newDirection);

                transition = board.getTransition(oldX, oldY, directionValue);

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

        int[][] directions = {{0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}};
        int[] currentDirection;
        int currnumbers = 0;

        for (int[] direction : directions) {
            currentDirection = direction;

            int nextX = x + currentDirection[1];
            int nextY = y + currentDirection[0];

            // avoids IndexOutOfBounds exception
            if (nextX < 0 || nextX >= board.getWidth() || nextY < 0 || nextY >= board.getHeight()) {
                int directionValue = Direction.indexOf(direction);

                Transition transition = board.getTransition(x, y, directionValue);

                if (transition != null) {
                    continue;
                }
                currnumbers++;
            } else {
                if(board.getField()[nextY][nextX] == '-')
                currnumbers++;
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
