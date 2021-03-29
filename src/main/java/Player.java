public class Player {

    private final char number;

    private int bomb;
    private int overrideStone;

    public Player(int number, int bomb, int changeStone) {
        this.number = (char) (number + '0');
        this.bomb = bomb;
        this.overrideStone = changeStone;
    }

    public char getNumber() {
        return number;
    }

    public int getBomb() {
        return bomb;
    }

    public void setBomb(int bomb) {
        this.bomb = bomb;
    }

    public boolean hasOverrideStone() {
        return overrideStone > 0;
    }

    public int getOverrideStone() {
        return overrideStone;
    }

    public void setOverrideStone(int overrideStone) {
        this.overrideStone = overrideStone;
    }

    public String toString() {
        return "Player " + number + " [o=" + overrideStone + " b=" + bomb + "]";
    }
}