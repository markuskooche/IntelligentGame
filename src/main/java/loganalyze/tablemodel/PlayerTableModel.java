package loganalyze.tablemodel;

import loganalyze.colorize.PlayerPoint;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class PlayerTableModel extends AbstractTableModel {
    private static final long serialVersionUID = 1L;

    public static final int ITEMS = 6;

    private final List<PlayerInformation> players;
    private int[] disqualified;

    public PlayerTableModel(List<PlayerInformation> players) {
        this.players = players;
    }

    public void setDisqualified(int[] disqualified) {
        this.disqualified = disqualified;
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0: return "ID";
            case 1: return "Information";
            default: return null;
        }
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public int getRowCount() {
        if (players == null) {
            return 0;
        }

        return ITEMS * players.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        int playerNumber = rowIndex / ITEMS;
        int playerRow = rowIndex % ITEMS;

        PlayerInformation player = players.get(playerNumber);

        if (columnIndex == 0 && playerRow == 0) {
            return player.getPlayer();
        } else if (columnIndex == 1) {
            if (playerRow == 0) {
                return "Farbe: " + PlayerPoint.getColor(player.getPlayer() - '0');
            } else if (playerRow == 1) {
                return "Aktuell belegt: " + player.getOccupiedFields();
            } else if (playerRow == 2) {
                int disqualifiedMove = disqualified[playerNumber];
                if (disqualifiedMove == -1) {
                    return "Disqualifiziert: -";
                } else {
                    return "Disqualifiziert: " + disqualifiedMove;
                }
            } else if (playerRow == 3) {
                return "Überschreibsteine: " + player.getOverride();
            } else if (playerRow == 4) {
                return "Bomben: " + player.getBomb();
            }
        }

        return null;
    }
}
