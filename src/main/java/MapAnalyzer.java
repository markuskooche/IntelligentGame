import java.util.ArrayList;
import java.util.List;

public class MapAnalyzer {

    private int[][] field;
    private Board board;
    private int[][] reachableField; // bool array
    private List<int[]> specialFieldList;

    public MapAnalyzer(Board b) {
        board = b;
        createField();
        createReachableField();
    }

    /**
     * creates a Field that contains only reachable Fields and values every Field by location and location to other Fields
     */
    public void createField() {

        int height = board.getHeight();
        int width = board.getWidth();
        field = new int[height][width];
        specialFieldList = new ArrayList<>();

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {

                char currField = board.getField()[i][j];

                if (currField != '-') {
                    //create RechableField for the First field that is Writable (The most performant way would by around the middle of the Map
//                    createReachableField(j, i);
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
        reachableField = new int[height][width];
        List<int[]> tmpList = new ArrayList<>();
        for (int[] field : specialFieldList) {
            if (hasAdjacentFields(field[0], field[1], '-', '-')) {
                tmpList.add(field);
                reachableField[field[1]][field[0]] = 2;
            }
        }

        specialFieldList = tmpList;


        traverseMapForEachPlayerStone();
        calculateReachableFieldsFromSpecialFields();
        //traverseMap(x, y);
        alterCurrentMap();
    }

    private void calculateReachableFieldsFromSpecialFields() {
        List<int[]> newDirectionsList;
        for (int[] field : specialFieldList) {
           newDirectionsList = findSpecialFieldDirections(field[0], field[1]);

           for(int[] direction : newDirectionsList){
               followFields(field[0], field[1], direction);
               treeToFour();
           }

        }
    }

    private void followFields(int x, int y, int[] currentDirection){


        if (x < 0 || x >= board.getWidth() || y < 0 || y >= board.getHeight() || board.getField()[y][x] == '-') {




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
                return;
        }else{

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

                // avoids IndexOutOfBounds exception                                                     required for intermap transactions
                if (nextX < 0 || nextX >= board.getWidth() || nextY < 0 || nextY >= board.getHeight() || board.getField()[nextY][nextX] == '-') {
                    int directionValue = Direction.indexOf(direction);

                    Transition transition = board.getTransition(x, y, directionValue);

                    // Follow Transactions
                    if (transition != null) {
                        int oppositeX;
                        int oppositeY;
                        int[] oppositeDirection = new int[2];

                        int[] destination = transition.getDestination(x, y, directionValue);

                        if (board.getField()[destination[0]][destination[1]] == 'x' ||
                            board.getField()[destination[0]][destination[1]] == 'b' ||
                            board.getField()[destination[0]][destination[1]] == 'v' ||
                            board.getField()[destination[0]][destination[1]] == 'i' ||
                            board.getField()[destination[0]][destination[1]] == '0' &&
                            reachableField[destination[0]][destination[1]] != 3) {

                            followFields(destination[0], destination[1], Direction.valueOf(destination[2]));

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

                                    if (board.getField()[destination[0]][destination[1]] == 'x' ||
                                        board.getField()[destination[0]][destination[1]] == 'b' ||
                                        board.getField()[destination[0]][destination[1]] == 'v' ||
                                        board.getField()[destination[0]][destination[1]] == 'i' ||
                                        board.getField()[destination[0]][destination[1]] == '0' &&
                                        reachableField[destination[0]][destination[1]] != 3) {

                                        followFields(destination[0], destination[1], Direction.valueOf(destination[2]));
                                    }
                                }
                            }
                        }
                    }

                } else {
                    // Follow fields if they are reachable

                    boolean searchForFours = false;

                    if(reachableField[y][x] != 4){
                        searchForFours = true;
                    }

                    char currChar = board.getField()[nextY][nextX];
                    if ((currChar != '-' && currChar != 'b'&& currChar != 'c'&&  currChar != 'i'&& currChar != '0') || reachableField[nextY][nextX] == 3 || (reachableField[nextY][nextX] == 4) && searchForFours) {


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
        reachableField[y][x] = 3;
        followFields(x + currentDirection[0],y + currentDirection[1], currentDirection);

    }

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

    private List<int[]> findSpecialFieldDirections(int x, int y) {
        int[][] directions = {{-1, 0}, {-1, 1}, {0, 1}, {1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}};
        List<int[]> newDirectionsList = new ArrayList<>();
        boolean oneFound;
        boolean isSpecialField;

        for (int i = 0; i < directions.length; i++) {

            oneFound = false;

            int nextX = x + directions[i][1];
            int nextY = y + directions[i][0];

            if (nextX < 0 || nextX >= board.getWidth() || nextY < 0 || nextY >= board.getHeight()) {
                int directionValue = Direction.indexOf(directions[i]);

                Transition transition = board.getTransition(x, y, directionValue);

                // Follow Transactions
                if (transition == null) {
                    continue;
                } else {
                    int[] destination = transition.getDestination(x, y, directionValue);
                    if(reachableField[destination[1]][destination[0]] == 1){
                        oneFound = true;
                    }
                }
            } else {
                // Follow fields if they are reachable
                if (reachableField[nextY][nextX] == 1) {
                    oneFound = true;
                }

            }

            if(oneFound){

                int nextX2 = x + directions[(i + 4) % 7][1];
                int nextY2 = y + directions[(i + 4) % 7][0];
                isSpecialField = false;

                if (nextX2 < 0 || nextX2 >= board.getWidth() || nextY2 < 0 || nextY2 >= board.getHeight()) {
                    int directionValue = Direction.indexOf(directions[i]);

                    Transition transition = board.getTransition(x, y, directionValue);

                        // Follow Transactions
                    if (transition == null) {
                            continue;
                    } else {
                        int[] destination = transition.getDestination(x, y, directionValue);
                        if(reachableField[destination[1]][destination[0]] == 2 || reachableField[destination[1]][destination[0]] == 0 ){
                            isSpecialField = true;
                        }
                    }
                } else {
                    if (reachableField[nextY2][nextX2] == 2 || reachableField[nextY2][nextX2] == 0) {
                        isSpecialField = true;
                    }
                    }

                    if(isSpecialField){
                        int[] newDirection = new int[2];
                        newDirection[1] = directions[i][0]*(-1);
                        newDirection[0] = directions[i][1]*(-1);
                        newDirectionsList.add(newDirection);
                    }


            }

        }
        return newDirectionsList;
    }





    private void traverseMapForEachPlayerStone() {

        int height = board.getHeight();
        int width = board.getWidth();

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                char currField = board.getField()[i][j];

                if(currField == '1' || currField == '2' ||currField == '3' ||currField == '4' ||
                        currField == '5' ||currField == '6' ||currField == '7' || currField == '8' ||currField == 'x'){
                    traverseMap(j,i);
                }

            }
        }
    }

    private boolean hasAdjacentFields(int x, int y, char firstSearched, char secondSearched) {
        int[][] directions = {{-1, 0}, {-1, 1}, {0, 1}, {1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}};
        int[] currentDirection;
        char first;
        char second;

        //System.out.println("(" + x + ")(" + y + ") Wurde gelockt");
        for(int i = 0; i < directions.length/2; i++){

            int nextX = x + directions[i][1];
            int nextY = y + directions[i][0];

            // avoids IndexOutOfBounds exception                                                     required for intermap transactions
            if (nextX < 0 || nextX >= board.getWidth() || nextY < 0 || nextY >= board.getHeight()){
                int directionValue = Direction.indexOf(directions[i]);

                Transition transition = board.getTransition(x, y, directionValue);

                // Follow Transactions
                if (transition == null) {
                    first = '-';
                    } else{
                    first = '0';
                    }
            } else {
                // Follow fields if they are reachable
                first = board.getField()[nextY][nextX];
            }

            int nextX2 = x + directions[i+4][1];
            int nextY2 = y + directions[i+4][0];

            // avoids IndexOutOfBounds exception
            if (nextX2 < 0 || nextX2 >= board.getWidth() || nextY2 < 0 || nextY2 >= board.getHeight()){
                int directionValue = Direction.indexOf(directions[i+4]);

                Transition transition = board.getTransition(x, y, directionValue);

                // Follow Transactions
                if (transition == null) {
                    second = '-';
                } else{
                    second = '0';
                }

            } else {
                // Follow fields if they are reachable
                second = board.getField()[nextY2][nextX2];
            }

            if(first == firstSearched && second == secondSearched || first == secondSearched && second == firstSearched){
                return true;
            }
        }

        return false;
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
        //TODO abbrechen falls in allen directions eine 0 ist denn es sind so keine züge möglich
        reachableField[y][x] = 1;

        //System.out.println("(" + x + ")(" + y + ") Wurde gelockt");
        for (int[] direction : directions) {
            currentDirection = direction;

            int nextX = x + currentDirection[1];
            int nextY = y + currentDirection[0];

            // avoids IndexOutOfBounds exception                                                     required for intermap transactions
            if (nextX < 0 || nextX >= board.getWidth() || nextY < 0 || nextY >= board.getHeight() || board.getField()[nextY][nextX] == '-') {
                int directionValue = Direction.indexOf(direction);

                Transition transition = board.getTransition(x, y, directionValue);

                // Follow Transactions
                if (transition != null) {
                    if (reachableField[transition.getY1()][transition.getX1()] != 1 && reachableField[transition.getY2()][transition.getX2()] != 2) {
                        //TODO nach Transaktion darf man sich nur gerade ausbreiten und nicht in alle Richtungen
                        traverseMap(transition.getX1(), transition.getY1());
                    } else if (reachableField[transition.getY2()][transition.getX2()] != 1 && reachableField[transition.getY2()][transition.getX2()] != 2) {
                        traverseMap(transition.getX2(), transition.getY2());
                    }
                }

            } else {
                // Follow fields if they are reachable
                char currChar = board.getField()[nextY][nextX];
                if (currChar != '-' && reachableField[nextY][nextX] != 1 && reachableField[nextY][nextX] != 2) {
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

        int[][] directions = {{-1, 0}, {-1, 1}, {0, 1}, {1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}};
        int[] currentDirection;
        int currnumbers = 0;
        char[][] tmpfield = board.getField();

        //create a String to analyze the direct neighbours
        StringBuilder neighbourhoodString = new StringBuilder();

        for (int[] direction : directions) {
            currentDirection = direction;

            int nextX = x + currentDirection[1];
            int nextY = y + currentDirection[0];

            // avoids IndexOutOfBounds exception
            if (nextX < 0 || nextX >= board.getWidth() || nextY < 0 || nextY >= board.getHeight()) {
                int directionValue = Direction.indexOf(direction);

                Transition transition = board.getTransition(x, y, directionValue);

                if (transition != null) {
                    neighbourhoodString.append('0');
                    continue;
                }else{
                    neighbourhoodString.append('-');
                }
                currnumbers++;
            } else {

                char currChar = tmpfield[nextY][nextX];
                if (currChar == '-') {
                    currnumbers++;
                    neighbourhoodString.append('-');
                }else{
                    neighbourhoodString.append('0');
                }
            }
        }
        if(analyzeField(neighbourhoodString.toString())){
            int[] field = new int[2];
            field[0] = x;
            field[1] = y;
            specialFieldList.add(field);
        }
        return currnumbers;
    }

    private boolean analyzeField(String neighbourhood){

        int i = 0;

        while(true){

            if(i == 7){
                if(neighbourhood.charAt(i) == '0' &&  neighbourhood.charAt(0) == '-'){
                    i = 0;
                    break;
                }
            }

            if(i >= 7){
                return false;
            }

            if(neighbourhood.charAt(i) == '0' &&  neighbourhood.charAt(i+1) == '-'){
                i++;
                break;
            }

            i++;
           

        }

        boolean isContinues = true;
        int startIndex = i;
        while(true){

            if(i == 8){
                i = 0;
            }

            if(!isContinues && neighbourhood.charAt(i) == '-'){
                return true;
            }

            if(neighbourhood.charAt(i) != '-'){
                if(isContinues){
                    isContinues = false;
                }
            }

            i++;
            if(startIndex == i){
                break;
            }



        }
       return false;
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
