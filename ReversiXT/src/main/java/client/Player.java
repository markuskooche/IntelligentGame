package client;

public class Player {

    private int overrideStones;
    private int bombs;

    public Player(int overrides, int bombs) {
        this.overrideStones = overrides;
        this.bombs = bombs;
    }

    public void setOverridesStones(int overrideStones) {
        this.overrideStones = overrideStones;
    }

    public int getOverridesStones() {
        return this.overrideStones;
    }

    public void setBombs(int bombs) {
        this.bombs = bombs;
    }

    public int getBombs() {
        return this.bombs;
    }

    @Override
    public String toString() {
        return "@Player [o: " +  overrideStones + "] [b: " + bombs + "]";
    }
}