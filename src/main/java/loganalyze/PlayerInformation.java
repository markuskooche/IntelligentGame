package loganalyze;

public class PlayerInformation {

    private final int moveCount;

    private final int occupiedFields;
    private final int override;
    private final int bomb;

    private final char player;

    public PlayerInformation(char player, int bomb, int override, int occupiedFields, int moveCount) {
        this.player = player;
        this.moveCount = moveCount;

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

    public int getMoveCount() {
        return moveCount;
    }

    public int getOccupiedFields() {
        return occupiedFields;
    }
}
