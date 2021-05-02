package loganalyze;

import controller.Game;
import loganalyze.additionals.IncorrectGroupException;
import map.Player;
import map.Transition;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class GamePanelManager {

    private final String group;

    private Game game;
    private int possibleFields;
    private int bombFirstExecuted = -1;

    private int[] disqualifiedPlayers;
    String disqualifyReason;

    private int counter;

    private int width;
    private int height;
    private int ownPlayer;
    private int playerAmount;
    private final String filename;

    private final ArrayList<Integer> playerMoves;

    private final LinkedList<Integer> playerMobility;
    private final LinkedList<Integer> playerCoinParity;
    private final LinkedList<Integer> playerMapValue;
    private final LinkedList<Integer> playerHeuristic;
    private final LinkedList<Integer> visitedBoards;

    private final ArrayList<LinkedList<PlayerPoint>> playerPoints;
    private final ArrayList<LinkedList<BackgroundPoint>> backgroundPoints;
    private final ArrayList<LinkedList<PlayerInformation>> playerInformation;

    public GamePanelManager(String filename, String group) {
        this.filename = filename;
        this.group = "XT" + group + "-";
        counter = 0;

        playerInformation = new ArrayList<>();
        backgroundPoints = new ArrayList<>();
        playerPoints = new ArrayList<>();

        playerMoves = new ArrayList<>();

        playerMobility = new LinkedList<>();
        playerCoinParity = new LinkedList<>();
        playerMapValue = new LinkedList<>();
        playerHeuristic = new LinkedList<>();
        visitedBoards = new LinkedList<>();
    }

    public LinkedList<PlayerInformation> getPlayerInformation(int index) {
        return playerInformation.get(index);
    }

    public LinkedList<BackgroundPoint> getBackground(int index) {
        return backgroundPoints.get(index);
    }

    public LinkedList<PlayerPoint> getPlayer(int index) {
        return playerPoints.get(index);
    }

    public String getPercentageDistribution(int index) {
        LinkedList<PlayerInformation> players = playerInformation.get(index);
        int allOccupiedFields = 0;

        for (PlayerInformation player : players) {
            allOccupiedFields += player.getOccupiedFields();
        }

        calculatePossibleFields(index);
        int percentage = ((allOccupiedFields * 100) / possibleFields);
        return ("Verteilung: " + percentage + "% " + allOccupiedFields + "/" + possibleFields);
    }

    public List<int[]> getTransitions() {
        Collection<Transition> transitions = game.getBoard().getAllTransitions().values();
        List<int[]> transitionList = new LinkedList<>();

        ArrayList<Transition> tmpList = new ArrayList<>();

        for (Transition transition : transitions) {
            int x = transition.getX();
            int y = transition.getY();
            int r = transition.getR();

            Transition opposite = game.getBoard().getTransition(x, y, r);

            if (!tmpList.contains(transition) || !tmpList.contains(opposite)) {
                int oppositeX = opposite.getX();
                int oppositeY = opposite.getY();
                int oppositeR = opposite.getR();

                int[] newTransition = new int[6];
                newTransition[0] = x;
                newTransition[1] = y;
                newTransition[2] = r;
                newTransition[3] = oppositeX;
                newTransition[4] = oppositeY;
                newTransition[5] = oppositeR;

                transitionList.add(newTransition);

                tmpList.add(transition);
                tmpList.add(opposite);
            }
        }

        return transitionList;
    }

    public void load() throws IOException, IncorrectGroupException {
        Path path = Paths.get(filename);
        List<String> file = Files.lines(path).collect(Collectors.toList());
        parseFile(file);
    }

    public String[] getCurrentMap(int index) {
        String[] transitions = game.getTransitions();
        int length = height + transitions.length + 4;
        String[] board = new String[length];

        PlayerInformation info = playerInformation.get(0).get(0);

        board[0] = String.valueOf(playerAmount);
        board[1] = String.valueOf(info.getOverride());
        board[2] = info.getBomb() + " " + getBombRadius();
        board[3] = height + " " + width;

        char[][] currentField = new char[height][width];

        for (BackgroundPoint backgroundPoint : backgroundPoints.get(index)) {
            int x = backgroundPoint.x;
            int y = backgroundPoint.y;
            char field = backgroundPoint.field;

            if (y < height && x < width) {
                currentField[y][x] = field;

            }
        }

        for (PlayerPoint playerPoint : playerPoints.get(index)) {
            int x = playerPoint.x;
            int y = playerPoint.y;
            char player = (char) (playerPoint.player + '0');

            currentField[y][x] = player;
        }

        for (int y = 0; y < height; y++) {
            board[y + 4] = "";
            for (int x = 0; x < width; x++) {
                board[y + 4] += currentField[y][x] + " ";
            }
        }

        for (int i = 0; i < transitions.length; i ++) {
            board[(height + 4) + i] = transitions[i];
        }

        return board;
    }

    public int getRealPlayerMove(int playerMove) {
        return playerMoves.get(playerMove);
    }

    public List<Integer> getMobility() {
        return playerMobility;
    }

    public List<Integer> getCoinParity() {
        return playerCoinParity;
    }

    public List<Integer> getMapValue() {
        return playerMapValue;
    }

    public List<Integer> getHeuristic() {
        return playerHeuristic;
    }

    public List<Integer> getVisitedBoards() {
        return visitedBoards;
    }

    public int getGameSize() {
        return playerPoints.size();
    }

    public int getOwnPlayer() {
        return ownPlayer;
    }

    public int getBombRadius() {
        return game.getBoard().getBombRadius();
    }

    public int getPlayerAmount() {
        return playerAmount;
    }

    public int[] getDisqualifiedPlayer() {
        return disqualifiedPlayers;
    }

    public String getDisqualifyReason() {
        return disqualifyReason;
    }

    private void parseFile(List<String> file) throws IncorrectGroupException {
        int disqualified = -1;
        for (String line : file) {
            if (line.length() >= 6) {
                String id = line.substring(0, 7);
                String loadGame = line.substring(5, 7);

                if (loadGame.equals("02")) {
                    if (id.equals(group + "02")) {
                        loadGame(line);
                        addPlayerInformation();
                        calculatePossibleFields(0);
                    } else {
                        int incorrectGroup = Integer.parseInt(id.substring(2, 4));
                        throw new IncorrectGroupException(incorrectGroup);
                    }
                } else if (id.equals(group + "03")) {
                    setOwnPlayer(line);
                } else if (id.equals(group + "05")) {
                    disqualified = counter;
                    setDisqualifyReason(line);
                } else if (id.equals(group + "06")) {
                    updateStatistic(disqualified, line);
                    executeMove(line);
                    addPlayerInformation();

                    counter += 1;
                    disqualified = -1;
                } else if (id.equals(group + "07")) {
                    disqualifyPlayer(line, counter);
                } else if (id.equals(group + "08")) {
                    bombFirstExecuted = counter + 1;
                } else if (id.equals(group + "98")) {
                    addVisitedBoards(line);
                } else {
                    System.out.println("NOT READABLE: " + line);
                }
            }
        }

        disqualifiedPlayers[ownPlayer - 1] = disqualified;
    }

    private void loadGame(String line) {
        String tmp = line.substring(9, (line.length() - 1));
        String[] lineArray = tmp.split(", ");
        char[] data = getGameState(lineArray);

        String string = String.valueOf(data);
        String[] lines = string.split("\n");

        List<String> map = new LinkedList<>(Arrays.asList(lines));
        game = new Game(map);

        height = game.getBoard().getHeight();
        width = game.getBoard().getWidth();
        playerAmount = game.getPlayers().length;

        disqualifiedPlayers = new int[playerAmount];
        Arrays.fill(disqualifiedPlayers, -1);

        addBackgroundPoints();
        addPlayerPoints();
    }

    private int calculateOccupiedFields(char player) {
        char[][] field = game.getBoard().getField();
        int occupiedFields = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (field[y][x] == player) {
                    occupiedFields += 1;
                }
            }
        }

        return occupiedFields;
    }

    private char[] getGameState(String[] map) {
        char[] data = new char[map.length];
        int counter = 0;

        for (String number : map) {
            char info = (char) Byte.parseByte(number);
            data[counter++] = info;
        }

        return data;
    }

    private void setOwnPlayer(String line) {
        // ["XT01", "03", "PL", "0?"] -> the '?' is your player
        String[] lineArray = line.split("-");
        ownPlayer = Integer.parseInt(lineArray[3]);
    }

    private void executeMove(String line) {
        // ["XT01", "06", "PL", "0p", "xx", "yy", "SF", "sm"]
        // xx => x-Coordinate
        // yy => y-Coordinate
        // sm => Special Move
        String[] lineArray = line.split("-");
        int p = Integer.parseInt(lineArray[3]);
        int x = Integer.parseInt(lineArray[4]);
        int y = Integer.parseInt(lineArray[5]);
        int s = Integer.parseInt(lineArray[7]);

        if (p == ownPlayer) {
            playerMoves.add(counter);
        }

        if (bombFirstExecuted == -1) {
            game.executeMove(x, y, p, s);
        } else {
            game.executeBomb(x, y);
        }

        addBackgroundPoints();
        addPlayerPoints();
    }

    private void disqualifyPlayer(String line, int count) {
        // ["XT01", "07", "PL", "0?"]
        String[] lineArray = line.split("-");
        int disqualified = Integer.parseInt(lineArray[3]);
        disqualifiedPlayers[disqualified - 1] = count;
    }

    private void setDisqualifyReason(String line) {
        String[] lineArray = line.split("-");
        int x = Integer.parseInt(lineArray[4]);
        int y = Integer.parseInt(lineArray[5]);

        disqualifyReason = "Der Client wollte auf x = " + x + " y = " + y + " ziehen.";
    }

    private void addPlayerInformation() {
        LinkedList<PlayerInformation> tmp = new LinkedList<>();

        for (Player player : game.getPlayers()) {
            char number = player.getNumber();

            int override = player.getOverrideStone();
            int bomb = player.getBomb();

            int occupied = calculateOccupiedFields(number);
            tmp.add(new PlayerInformation(number, bomb, override, occupied));
        }

        playerInformation.add(tmp);
    }

    private void calculatePossibleFields(int index) {
        LinkedList<BackgroundPoint> currentBackgroundPoints = backgroundPoints.get(index);
        possibleFields = height * width;

        for (BackgroundPoint backgroundPoint : currentBackgroundPoints) {
            if (backgroundPoint.field == '-') {
                possibleFields -= 1;
            }
        }
    }

    private void addPlayerPoints() {
        char[][] field = game.getBoard().getField();
        LinkedList<PlayerPoint> tmp = new LinkedList<>();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if ("12345678".indexOf(field[y][x]) != -1) {
                    tmp.add(new PlayerPoint(x, y, (field[y][x] - '0')));
                }
            }
        }

        playerPoints.add(tmp);
    }

    private void addBackgroundPoints() {
        char[][] field = game.getBoard().getField();
        LinkedList<BackgroundPoint> tmp = new LinkedList<>();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if ("12345678".indexOf(field[y][x]) == -1) {
                    tmp.add(new BackgroundPoint(x, y, field[y][x]));
                }
            }
        }

        if (height < 50) {
            for (int x = 0; x < width; x++) {
                tmp.add(new BackgroundPoint(x, height, ' '));
            }
            if (width < 50) {
                tmp.add(new BackgroundPoint(width, height, ' '));
            }
        }

        if (width < 50) {
            for (int y = 0; y < height; y++) {
                tmp.add(new BackgroundPoint(width, y, ' '));
            }
            if (height < 50) {
                tmp.add(new BackgroundPoint(width, height, ' '));
            }
        }

        backgroundPoints.add(tmp);
    }

    private void addVisitedBoards(String line) {
        String[] elements = line.split("-");
        visitedBoards.add(Integer.parseInt(elements[3]));
    }

    private void updateStatistic(int value, String line) {
        String[] data = line.split("-");
        int player = Integer.parseInt(data[3]);

        if (value != -1) {
            playerMobility.add(game.getMobility(player));
            playerCoinParity.add(game.getCoinParity(player));
            playerMapValue.add(game.getMapValue(player));
            playerHeuristic.add(game.getHeuristic(player));
        }
    }
}
