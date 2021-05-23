package loganalyze.controller;

import controller.Game;
import loganalyze.additional.AnalyzeParser;
import loganalyze.additional.IncorrectGroupException;
import loganalyze.colorize.BackgroundPoint;
import loganalyze.tablemodel.PlayerInformation;
import map.Board;
import mapanalyze.MapAnalyzer;
import server.MapParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class GameController {

    private static final boolean reduce = true;
    private Game game;

    private int bombFirstExecuted = -1;

    private final String groupString;
    private final int group;

    private int ownPlayerNumber;
    private int counter;

    private int[] currentPlayerTurn;

    private GamePanelManager gamePanelManager;
    private PlayerTableManager playerTableManager;
    private StatisticWindowManager statisticWindowManager;
    private MenuBarPanelManager menuBarPanelManager;
    private InformationPanelManager informationPanelManager;

    public GameController(String group) {
        this.informationPanelManager = new InformationPanelManager();
        this.menuBarPanelManager = new MenuBarPanelManager();
        this.statisticWindowManager = new StatisticWindowManager();
        this.playerTableManager = new PlayerTableManager();
        this.gamePanelManager = new GamePanelManager();

        this.groupString = "XT" + group + "-";
        this.group = Integer.parseInt(group);
        this.counter = 0;
    }


    public void load(String filename) throws IOException, IncorrectGroupException {
        Path path = Paths.get(filename);
        List<String> file = Files.lines(path).collect(Collectors.toList());
        parseFile(file);
    }

    private void parseFile(List<String> file) throws IncorrectGroupException {
        int disqualified = -1;

        for (String line : file) {
            if (line.length() >= 6) {
                String id = line.substring(0, 7);
                String loadGame = line.substring(5, 7);

                if (loadGame.equals("02")) {
                    if (id.equals(groupString + "02")) {
                        loadGame(line);
                        setupInformationPanel();
                        menuBarPanelManager.setTransitions(game);
                        playerTableManager.addPlayerInformation(game);
                        calculatePossibleFields();
                        menuBarPanelManager.setReachableField(game);
                        informationPanelManager.addPlayerTurn(0);
                    } else {
                        int incorrectGroup = Integer.parseInt(id.substring(2, 4));
                        throw new IncorrectGroupException(incorrectGroup);
                    }
                } else if (id.equals(groupString + "03")) {
                    setOurPlayerNumber(line);
                } else if (id.equals(groupString + "05")) {
                    disqualified = counter;
                    playerTableManager.setDisqualifyReason(line);
                } else if (id.equals(groupString + "06")) {
                    statisticWindowManager.updateStatistic(game, ownPlayerNumber);
                    executeMove(line);
                    playerTableManager.addPlayerInformation(game);

                    counter += 1;
                    disqualified = -1;
                } else if (id.equals(groupString + "07")) {
                    playerTableManager.disqualifyPlayer(line, counter);
                } else if (id.equals(groupString + "08")) {
                    bombFirstExecuted = counter + 1;
                } else if (id.equals(groupString + "98")) {
                    try {
                        String[] elements = line.split("-");
                        int value = Integer.parseInt(elements[3]);
                        statisticWindowManager.addVisitedBoard(value);
                    }
                    catch (Exception ignored) {
                        //ignored.printStackTrace();
                    }
                } else {
                    System.out.println("NOT READABLE: " + line);
                }
            }
        }

        if (disqualified != -1) {
            String disqualifySelf = "XTxx-07-PL-" + ownPlayerNumber;
            playerTableManager.disqualifyPlayer(disqualifySelf, counter);
        }
    }

    private void loadGame(String line) {
        String tmp = line.substring(9, (line.length() - 1));
        String[] lineArray = tmp.split(", ");
        byte[] mapStream = createMapStream(lineArray);

        List<String> gameList = MapParser.createMap(mapStream);
        AnalyzeParser analyzeParser = new AnalyzeParser(group, false, reduce);
        game = new Game(gameList, analyzeParser);

        int height = game.getBoard().getHeight();
        int width = game.getBoard().getWidth();
        gamePanelManager.setGameSize(width, height);
        playerTableManager.setGameSize(width, height);

        int playerAmount = game.getPlayers().length;
        currentPlayerTurn = new int[playerAmount];
        Arrays.fill(currentPlayerTurn, 1);

        gamePanelManager.addGameState(game);
    }

    private void calculatePossibleFields() {
        LinkedList<BackgroundPoint> currentBackgroundPoints = gamePanelManager.getBackgroundPoints(0);

        int height = game.getBoard().getHeight();
        int width = game.getBoard().getWidth();

        int possibleFields = height * width;

        if (game.isReachableFinished()) {
            int[][] reachedField = game.getReachableField();

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (reachedField[y][x] == MapAnalyzer.UNREACHABLE) {
                        possibleFields -= 1;
                    }
                }
            }
        } else {
            for (BackgroundPoint backgroundPoint : currentBackgroundPoints) {
                if (backgroundPoint.piece == '-') {
                    possibleFields -= 1;
                }
            }
        }

        gamePanelManager.setReachableFields(possibleFields);
    }

    private void setupInformationPanel() {
        int bombRadius = game.getBoard().getBombRadius();
        informationPanelManager.setBombRadius(bombRadius);

        int playerAmount = game.getPlayers().length;
        playerTableManager.setPlayerAmount(playerAmount);
        informationPanelManager.setPlayerAmount(playerAmount);
    }

    private byte[] createMapStream(String[] map) {
        byte[] data = new byte[map.length];
        int counter = 0;

        for (String number : map) {
            byte info = Byte.parseByte(number);
            data[counter++] = info;
        }

        return data;
    }

    private void setOurPlayerNumber(String line) {
        String[] lineArray = line.split("-");

        ownPlayerNumber = Integer.parseInt(lineArray[3]);
        informationPanelManager.setOwnPlayer(ownPlayerNumber);
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

        informationPanelManager.addPlayerTurn(currentPlayerTurn[p - 1]++);

        if (p == ownPlayerNumber) {
            informationPanelManager.addOwnPlayerMove(counter);
        }

        if (bombFirstExecuted == -1) {
            game.executeMove(x, y, p, s);
        } else {
            game.executeBomb(x, y);
        }

        gamePanelManager.addGameState(game);
    }

    public String[] getExportMap(int index) {
        if (index < counter) {
            PlayerInformation info = playerTableManager.getPlayerInformation(0).get(0);
            int height = game.getBoard().getHeight();

            String[] transitions = game.getTransitions();
            int length = height + transitions.length + 4;
            String[] exportMap = new String[length];

            int playerAmount = game.getPlayers().length;

            exportMap[0] = String.valueOf(playerAmount);
            exportMap[1] = String.valueOf(info.getOverride());
            exportMap[2] = info.getBomb() + " " + informationPanelManager.getBombRadius();

            exportMap = gamePanelManager.getCurrentMap(exportMap, index);
            System.arraycopy(transitions, 0, exportMap, (height + 4), transitions.length);

            return exportMap;
        }

        return new String[] {};
    }

    public int getGameLength() {
        return counter;
    }

    public GamePanelManager getGamePanelManager() {
        return gamePanelManager;
    }

    public PlayerTableManager getPlayerTableManager() {
        return playerTableManager;
    }

    public StatisticWindowManager getStatisticWindowManager() {
        return statisticWindowManager;
    }

    public MenuBarPanelManager getMenuBarPanelManager() {
        return menuBarPanelManager;
    }

    public InformationPanelManager getInformationPanelManager() {
        return informationPanelManager;
    }
}
