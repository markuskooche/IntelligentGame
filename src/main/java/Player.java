public class Player {

    private int bomb;
    private int overrideStone;

    public Player(int bomb, int changeStone) {
        this.bomb = bomb;
        this.overrideStone = changeStone;
    }

    public int getBomb() {
        return bomb;
    }

    public void setBomb(int bomb) {
        this.bomb = bomb;
    }

    public int getOverrideStone() {
        return overrideStone;
    }

    public void setOverrideStone(int overrideStone) {
        this.overrideStone = overrideStone;
    }
}