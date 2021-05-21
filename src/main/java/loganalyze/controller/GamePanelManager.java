package loganalyze.controller;


import controller.Game;
import loganalyze.colorize.BackgroundPoint;
import loganalyze.colorize.PlayerPoint;
import loganalyze.tablemodel.PlayerInformation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class GamePanelManager {

    private int width;
    private int height;

    private int reachableFields = -1;

    private final ArrayList<LinkedList<PlayerPoint>> playerPoints;
    private final ArrayList<LinkedList<BackgroundPoint>> backgroundPoints;

    public GamePanelManager() {
        backgroundPoints = new ArrayList<>();
        playerPoints = new ArrayList<>();
    }

    public void setGameSize(int width, int height) {
        this.height = height;
        this.width = width;
    }

    public void addGameState(char[][] field) {
        addBackgroundPoints(field);
        addPlayerPoints(field);
    }

    private void addPlayerPoints(char[][] field) {
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

    private void addBackgroundPoints(char[][] field) {
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

    public String[] getCurrentMap(String[] exportMap, int index) {

        exportMap[3] = height + " " + width;

        char[][] currentField = new char[height][width];

        for (BackgroundPoint backgroundPoint : backgroundPoints.get(index)) {
            int x = backgroundPoint.x;
            int y = backgroundPoint.y;
            char piece = backgroundPoint.piece;

            if (y < height && x < width) {
                currentField[y][x] = piece;

            }
        }

        for (PlayerPoint playerPoint : playerPoints.get(index)) {
            int x = playerPoint.x;
            int y = playerPoint.y;
            char player = (char) (playerPoint.player + '0');

            currentField[y][x] = player;
        }

        for (int y = 0; y < height; y++) {
            exportMap[y + 4] = "";
            for (int x = 0; x < width; x++) {
                exportMap[y + 4] += currentField[y][x] + " ";
            }
        }

        return exportMap;
    }

    public void setReachableFields(int amount) {
        reachableFields = amount;
    }

    public String getPercentageDistribution(int index) {
        int allOccupiedFields = playerPoints.get(index).size();

        int percentage = ((allOccupiedFields * 100) / reachableFields);
        return ("Verteilung: " + percentage + "% " + allOccupiedFields + "/" + reachableFields);
    }

    public LinkedList<BackgroundPoint> getBackgroundPoints(int index) {
        if (index < backgroundPoints.size()) {
            return backgroundPoints.get(index);
        }

        return null;
    }

    public LinkedList<PlayerPoint> getPlayerPoints(int index) {
        if (index < backgroundPoints.size()) {
            return playerPoints.get(index);
        }

        return null;
    }
}