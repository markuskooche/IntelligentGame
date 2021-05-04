package loganalyze.tablemodel;

public class PlayerInformation {

    private final int occupiedFields;
    private final int override;
    private final int bomb;

    private final char player;

    public PlayerInformation(char player, int bomb, int override, int occupiedFields) {
        this.player = player;

        this.occupiedFields = occupiedFields;
        this.override = override;
        this.bomb = bomb;
    }

    public char getPlayer() {
        return player;
    }

    public int getBomb() {
        return bomb;
    }

    public int getOverride() {
        return override;
    }

    public int getOccupiedFields() {
        return occupiedFields;
    }
}
