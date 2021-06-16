package heuristic.bombposition;

import map.Direction;
import map.Transition;

import java.util.Arrays;
import java.util.HashMap;

public class BombHeuristic {

    private final int BOMB_EVALUATION;

    private final HashMap<Integer, Transition> transitions;

    private final int[] coinParities;
    private final int[] sortedPlayer;
    private int ourSortPosition;

    private final int[][] evaluatedField;
    private final int[][] cachedField;
    private final int[][] bombField;

    private final int height;
    private final int width;

    private final char ourPlayerNumber;
    private final int startRadius;

    private char[][] field;

    public BombHeuristic(HashMap<Integer, Transition> transitions, int height, int width, int playerAmount, char ourPlayerNumber, int radius) {
        this.transitions = transitions;
        this.height = height;
        this.width = width;

        this.coinParities = new int[playerAmount];
        this.sortedPlayer = new int[playerAmount];
        this.ourPlayerNumber = ourPlayerNumber;
        this.startRadius = radius;

        this.evaluatedField = new int[height][width];
        this.cachedField = new int[height][width];
        this.bombField = new int[height][width];

        this.BOMB_EVALUATION = (2 * radius + 1);
    }

    public int[] getBombPosition(char[][] field) {
        this.field = field;

        countingPlayers();
        evaluatePlayers();

        char importantPlayer = getEvaluationSetup();

        evaluateField(importantPlayer);
        return getBestBombPosition();
    }

    private void countingPlayers() {
        Arrays.fill(coinParities, 0);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if ("12345678".indexOf(field[y][x]) != -1) {
                    int player = field[y][x] - '0';
                    coinParities[player - 1] += 1;
                }
            }
        }
    }

    public void evaluatePlayers(char[][] field) {
        this.field = field;

        countingPlayers();
        evaluatePlayers();
    }

    private void evaluatePlayers() {
        int playerAmount = sortedPlayer.length;

        for (int i = 0; i < playerAmount; i++) {
            sortedPlayer[i] = (i + 1);
        }

        //System.out.println(Arrays.toString(coinParities));
        //System.out.println(Arrays.toString(sortedPlayer));

        for(int t = 0; t < playerAmount; t++) {
            for(int i = 0; i < (playerAmount - 1); i++) {
                if (coinParities[i] < coinParities[i+1]) {
                    int tmpCoin = coinParities[i];
                    coinParities[i] = coinParities[i+1];
                    coinParities[i+1] = tmpCoin;

                    int tmpSort = sortedPlayer[i];
                    sortedPlayer[i] = sortedPlayer[i+1];
                    sortedPlayer[i+1] = tmpSort;
                }
            }
        }

        for (int i = 0; i < playerAmount; i++) {
            if (sortedPlayer[i] == ourPlayerNumber - '0') {
                ourSortPosition = i;
                break;
            }
        }

        /*
        System.out.println(Arrays.toString(coinParities));
        int allCoins = 0;
        for (int i = 0; i < playerAmount; i++) {
            allCoins += coinParities[i];
        }

        String[] percentage = new String[playerAmount];
        for (int i = 0; i < playerAmount; i++) {
            double value = ((double) coinParities[i] / allCoins) * 100;
            percentage[i] = String.format("%1$,.2f%%", value);
        }
        System.out.println(Arrays.toString(percentage));
        System.out.println(Arrays.toString(sortedPlayer));
         */
    }

    private char getEvaluationSetup() {
        char importantPlayer = '9';

        // If we are not the player with the best ranking, we choose the player in front of us.
        if (ourSortPosition > 0) {
            importantPlayer = (char) (sortedPlayer[ourSortPosition - 1] + '0');
        }

        // If we are the player with the best ranking, we choose the player behind us.
        if (ourSortPosition == 0) {
            importantPlayer = (char) (sortedPlayer[ourSortPosition + 1] + '0');
        }

        return importantPlayer;
    }

    private void evaluateField(char importantPlayer) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (field[y][x] == ourPlayerNumber) {
                    if (ourSortPosition == 0) {
                        evaluatedField[y][x] = - (int) (2 * (Math.pow(BOMB_EVALUATION, 2.0) - BOMB_EVALUATION));
                    } else {
                        evaluatedField[y][x] = - (int) (2 * (Math.pow(BOMB_EVALUATION, 2.0) + BOMB_EVALUATION));
                    }
                } else if (field[y][x] == importantPlayer) {
                    evaluatedField[y][x] = (int) Math.pow(BOMB_EVALUATION, 2.0);
                } else if ("12345678".indexOf(field[y][x]) != -1) {
                    evaluatedField[y][x] = BOMB_EVALUATION;
                } else {
                    evaluatedField[y][x] = 0;
                }
            }
        }
    }

    private void prepareHelperField() {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if(field[i][j] != '-'){
                    bombField[i][j] = 0;
                }else{
                    bombField[i][j] = -1;
                }
            }
        }
    }

    private int[] getBestBombPosition() {
        int[] bestPosition = new int[] {-1, -1};
        int bestValue = Integer.MIN_VALUE;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (field[y][x] != '-') {
                    prepareHelperField();
                    updateBombValue(x, y);

                    if (cachedField[y][x] > bestValue) {
                        bestValue = cachedField[y][x];
                        bestPosition = new int[]{x, y};
                    }
                } else {
                    cachedField[y][x] = Integer.MIN_VALUE;
                }
            }
        }

        //FieldPrinter.print(evaluatedField, " \"EF\"");
        //FieldPrinter.print(cachedField, " \"CF\"");
        //FieldPrinter.print(bombField, " \"BF\"");

        return bestPosition;
    }

    private void updateBombValue(int startX, int startY) {
        updateBombValueRecursive(startX, startY, startX, startY, (startRadius + 1));
    }

    private void updateBombValueRecursive(int startX, int startY, int x, int y, int radius) {
        // end recursion on radius = 0
        if (radius == 0) {
            return;
        }

        int newX, newY;
        bombField[y][x] = radius;
        cachedField[startY][startX] += evaluatedField[y][x];

        for (int[] direction : Direction.getList()) {
            //set next field
            newX = x + (direction[0]);
            newY = y + (direction[1]);

            //if field is not reachable or outside the map search for transition
            if (newX < 0 || newX >= width || newY < 0 || newY >= height || field[newY][newX] == '-') {

                int directionValue = Direction.indexOf(direction);
                Transition transition = getTransition(x, y, directionValue);

                if (transition != null) {
                    int transitionX = transition.getX();
                    int transitionY = transition.getY();

                    if(field[transitionY][transitionX] < radius) {
                        updateBombValueRecursive(startX, startY, transitionX, transitionY, (radius - 1));
                    }
                }
            } else {
                //update the field if a shorter path is found
                if ((bombField[y][x] - 1) > bombField[newY][newX]) {
                    updateBombValueRecursive(startX, startY, newX, newY, radius - 1);
                }
            }

        }
    }

    private Transition getTransition(int x, int y, int direction) {
        int transitionKey = Transition.hash(x, y, direction);
        return transitions.get(transitionKey);
    }
}
