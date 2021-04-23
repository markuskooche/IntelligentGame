package loganalyze;

public class PlayerInformation {

    private final int occupiedFields;
    private final int override;
    private final int bomb;

    private final char player;

    /*
    ------------------------------------------------------
    WAS IST WIRKLICH WICHTIG FÜR EINEN SPIELER ???
     ->> Welche Zahl hat der Spieler?
         > Welche Farbe hat der Spieler?
         > Anzahl an Überschreibsteinen
         > Anzahl an Bomben
         > Wurde er disqualifiziert?

            > Wie ist seine aktuelle Mobilität
            > Wie ist seine aktuelle CoinParity
            > Wie ist seine aktuelle PositionQuality
    ------------------------------------------------------
     */

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
