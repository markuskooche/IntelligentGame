public class MapAnalyzer {

     private int[][] field;
     private Board board;

     public MapAnalyzer(Board b){
        board = b;
     }

     public void createField(){

         int height = board.getHeight();
         int width = board.getWidth();
         field = new int[height][width];


         for(int i = 0; i < height; i++){
             for(int j = 0; j < width; j++){

                 char currField = board.getField()[i][j];

                 if(currField != '-'){
                     int newValue = getLocationValue(j, i);
                    field[i][j] += newValue*3;

                    if(newValue >= 5){
                        createWaves(j, i, newValue, newValue/2);
                    }else{
                        createWaves(j, i, newValue, newValue);
                    }
                 }
                 if(currField == 'c'){
                     field[i][j] += 20;
                     createWaves(j, i, 50, 20);
                 }
                 else if(currField == 'b'){
                     field[i][j] += 15;
                     createWaves(j, i, 50, 15);
                 }
                 else if(currField == 'i'){
                     field[i][j] += 10;
                     createWaves(j, i, 50, 10);
                 }

             }
         }

     }

     public int calculateScoreForPlayer(char playernumber){
         int height = board.getHeight();
         int width = board.getWidth();
         int playerScore = 0;

         for(int i = 0; i < height; i++) {
             for (int j = 0; j < width; j++) {

                 char currField = board.getField()[i][j];

                 if(currField == playernumber){
                    playerScore += field[i][j];
                 }
             }
         }

         return playerScore;
     }


     public void createWaves(int x, int y, int range, int startValue){

         int[][] directions = {{-1, 0 }, {-1, 1}, {0, 1}, {1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}};
         int exhaustion = 1;
         int omen = -1;

         for(int currRange = 1; currRange <= range; currRange++){

             for(int i = 0; i < directions.length; i++){

                 int[] startDirection = directions[i];

                 int startX = x + (startDirection[1]*currRange);
                 int startY = y + (startDirection[0]*currRange);

                 if (startX < 0 || startX >= board.getWidth() || startY < 0 || startY >= board.getHeight()) {
                     continue;
                 }
                 if(board.getField()[startY][startX] != '-'){

                     if(((startValue)-exhaustion) > 0) {

                         if (currRange % 2 == 1) {
                             field[startY][startX] += ((startValue) - exhaustion) * omen;
                         } else {
                             field[startY][startX] += ((startValue) - exhaustion);
                         }
                     }
                 }
             }
             exhaustion++;

         }

     }

     public int getLocationValue(int x, int y){

         int[][] directions = {{-1, 0 }, {-1, 1}, {0, 1}, {1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}};
         int[] currentDirection;
         int currnumbers = 0;
         char[][] tmpfield = board.getField();

         for (int[] direction : directions) {
             Moves checkMove = new Moves();
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
             }else{

                 char currChar =  tmpfield[nextY][nextX];
                 if(currChar == '-'){
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

}
