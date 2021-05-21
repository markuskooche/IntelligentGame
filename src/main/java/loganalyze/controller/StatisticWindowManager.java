package loganalyze.controller;

import controller.Game;
import java.util.*;

public class StatisticWindowManager {

    private final LinkedList<Integer> playerMobility;
    private final LinkedList<Integer> playerCoinParity;
    private final LinkedList<Integer> playerMapValue;
    private final LinkedList<Integer> playerHeuristic;
    private final LinkedList<Integer> visitedBoards;

    public StatisticWindowManager() {
        playerMobility = new LinkedList<>();
        playerCoinParity = new LinkedList<>();
        playerMapValue = new LinkedList<>();
        playerHeuristic = new LinkedList<>();
        visitedBoards = new LinkedList<>();
    }

    public boolean hasMobility() {
        return playerMobility.size() > 0;
    }

    public List<Integer> getMobility() {
        return playerMobility;
    }

    public boolean hasCoinParity() {
        return playerCoinParity.size() > 0;
    }

    public List<Integer> getCoinParity() {
        return playerCoinParity;
    }

    public boolean hasMapValue() {
        return playerMapValue.size() > 0;
    }

    public List<Integer> getMapValue() {
        return playerMapValue;
    }

    public boolean hasHeuristic() {
        return playerHeuristic.size() > 0;
    }

    public List<Integer> getHeuristic() {
        return playerHeuristic;
    }

    public boolean hasVisitedBoards() {
        return visitedBoards.size() > 0;
    }

    public List<Integer> getVisitedBoards() {
        return visitedBoards;
    }

    public void addVisitedBoard(int value) {
        visitedBoards.add(value);
    }

    public void updateStatistic(Game game, int player) {
        playerMobility.add(game.getMobility(player));
        playerCoinParity.add(game.getCoinParity(player));
        playerMapValue.add(game.getMapValue(player));
        playerHeuristic.add(game.getHeuristic(player));
    }
}