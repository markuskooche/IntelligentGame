package loganalyze.controller;

import loganalyze.tablemodel.PlayerInformation;

import java.util.ArrayList;
import java.util.LinkedList;

public class InformationPanelManager {

    /*
    private static final boolean reduce = true;

    private final int groupNumber;
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

    private int[] currentPlayerTurn;
    private final ArrayList<Integer> playerTurn;
    private final ArrayList<Integer> playerMoves;

    private final LinkedList<Integer> playerMobility;
    private final LinkedList<Integer> playerCoinParity;
    private final LinkedList<Integer> playerMapValue;
    private final LinkedList<Integer> playerHeuristic;
    private final LinkedList<Integer> visitedBoards;

    private final ArrayList<LinkedList<PlayerPoint>> playerPoints;
    private final ArrayList<LinkedList<BackgroundPoint>> backgroundPoints;
     */

    private int ownPlayer;
    private int bombRadius;
    private int playerAmount;
    private int percentageDistribution;

    private final ArrayList<Integer> playerTurn;
    private final ArrayList<Integer> playerMoves;

    private final ArrayList<LinkedList<PlayerInformation>> playerInformation;

    public InformationPanelManager() {
        playerTurn = new ArrayList<>();
        playerMoves = new ArrayList<>();

        playerInformation = new ArrayList<>();
    }

    /*
    public int getCurrentPlayerTurn(int index) {
        return playerTurn.get(index);
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

    private void calculatePossibleFields(int index) {
        LinkedList<BackgroundPoint> currentBackgroundPoints = backgroundPoints.get(index);
        possibleFields = height * width;

        for (BackgroundPoint backgroundPoint : currentBackgroundPoints) {
            if (backgroundPoint.piece == '-') {
                possibleFields -= 1;
            }
        }
    }
    */

    public void addOwnPlayerMove(int index) {
        playerMoves.add(index);
    }

    public int getOwnPlayerMove(int playerMove) {
        if (playerMove < playerMoves.size()) {
            return playerMoves.get(playerMove);
        }

        int length = playerMoves.size();
        return playerMoves.get(length - 1);
    }

    public void addPlayerTurn(int turn) {
        playerTurn.add(turn);
    }

    public int getPlayerTurn(int index) {
        if (index < playerTurn.size()) {
            return playerTurn.get(index);
        }

        return -1;
    }

    public void setOwnPlayer(int ownPlayer) {
        this.ownPlayer = ownPlayer;
    }

    public int getOwnPlayer() {
        return ownPlayer;
    }

    public void setBombRadius(int radius) {
        this.bombRadius = radius;
    }

    public int getBombRadius() {
        return bombRadius;
    }

    public void setPlayerAmount(int playerAmount) {
        this.playerAmount = playerAmount;
    }

    public int getPlayerAmount() {
        return playerAmount;
    }
}