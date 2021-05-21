package loganalyze.controller;

import controller.Game;
import loganalyze.tablemodel.PlayerInformation;
import map.Player;
import java.util.*;

public class PlayerTableManager {

    private int[] disqualifiedPlayers;
    String disqualifyReason;

    private int counter;

    private int width;
    private int height;
    private int ownPlayer;

    private final ArrayList<LinkedList<PlayerInformation>> playerInformation;

    public PlayerTableManager() {
        playerInformation = new ArrayList<>();
    }

    public void setGameSize(int width, int height) {
        this.height = height;
        this.width = width;
    }

    public void setPlayerAmount(int amount) {
        disqualifiedPlayers = new int[amount];
    }

    public void addPlayerInformation(Game game) {
        LinkedList<PlayerInformation> tmp = new LinkedList<>();

        for (Player player : game.getPlayers()) {
            char playerNumber = player.getNumber();

            int override = player.getOverrideStone();
            int bomb = player.getBomb();

            int occupied = calculateOccupiedFields(game, playerNumber);
            tmp.add(new PlayerInformation(playerNumber, bomb, override, occupied));
        }

        playerInformation.add(tmp);
    }

    private int calculateOccupiedFields(Game game, char playerNumber) {
        char[][] field = game.getBoard().getField();
        int occupiedFields = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (field[y][x] == playerNumber) {
                    occupiedFields += 1;
                }
            }
        }

        return occupiedFields;
    }

    public LinkedList<PlayerInformation> getPlayerInformation(int index) {
        if (index < playerInformation.size()) {
            return playerInformation.get(index);
        }

        return null;
    }

    public int[] getDisqualifiedPlayers() {
        return disqualifiedPlayers;
    }

    public String getDisqualifyReason() {
        return disqualifyReason;
    }

    public void disqualifyPlayer(String line, int moveCount) {
        String[] lineArray = line.split("-");
        int disqualified = Integer.parseInt(lineArray[3]);
        disqualifiedPlayers[disqualified - 1] = moveCount;
    }

    public void setDisqualifyReason(String line) {
        String[] lineArray = line.split("-");
        int x = Integer.parseInt(lineArray[4]);
        int y = Integer.parseInt(lineArray[5]);

        disqualifyReason = "Der Client wollte auf x = " + x + " y = " + y + " ziehen.";
    }
}