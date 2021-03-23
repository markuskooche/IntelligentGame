public class Player {
    private int bomb;
    private int changeStone;
    private boolean qualified;

    public Player(int bomb, int changeStone, boolean qualified) {
        this.bomb = bomb;
        this.changeStone = changeStone;
        this.qualified = qualified;
    }

    public int getBomb() {
        return bomb;
    }

    public void setBomb(int bomb) {
        this.bomb = bomb;
    }

    public int getChangeStone() {
        return changeStone;
    }

    public void setChangeStone(int changeStone) {
        this.changeStone = changeStone;
    }

    public boolean isQualified() {
        return qualified;
    }

    public void setQualified(boolean qualified) {
        this.qualified = qualified;
    }
}