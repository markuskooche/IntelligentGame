package map;

/**
 * The player class is created for each player and contains the number of bombs and
 * override stones, as well as the number of his starting position. All information
 * can be queried and changed during the course of the game.
 *
 * @author Benedikt Halbritter
 * @author Iwan Eckert
 * @author Markus Koch
 */
public class Player {

    private int bomb;
    private int overrideStone;
    private final char number;

    /**
     * Creates a player with his number, bombs and override stones.
     *
     * @param number representing the number of the player
     * @param bomb the number of bombs a player has to start with
     * @param overrideStone the number of change stones a player has to start with
     */
    public Player(int number, int bomb, int overrideStone) {
        this.number = (char) (number + '0');
        this.bomb = bomb;
        this.overrideStone = overrideStone;
    }

    /**
     * A method to get the number of the player.
     *
     * @return a char representing the number of the player
     */
    public char getNumber() {
        return number;
    }

    /**
     * Returns true if the player has a bomb.
     *
     * @return true or false
     */
    public boolean hasBomb() {
        return bomb > 0;
    }

    /**
     * A method to get the number of bombs.
     *
     * @return an integer of the player's bombs
     */
    public int getBomb() {
        return bomb;
    }

    /**
     * A method to add a bomb to the current number of bombs.
     */
    public void increaseBomb() {
        bomb = bomb + 1;
    }

    /**
     * A method of subtracting a bomb from the current number of bombs.
     */
    public void decreaseBomb() {
        bomb = bomb - 1;
    }

    /**
     * Returns true if the player has an overridestone.
     *
     * @return true or false
     */
    public boolean hasOverrideStone() {
        return overrideStone > 0;
    }

    /**
     * A method to get the number of override stones.
     *
     * @return an integer of the player's override stones
     */
    public int getOverrideStone() {
        return overrideStone;
    }

    /**
     * A method to add an override stone to the current number of override stones.
     */
    public void increaseOverrideStone() {
        overrideStone = overrideStone + 1;
    }

    /**
     * A method of subtracting an override stone from the current number of override stones.
     */
    public void decreaseOverrideStone() {
        overrideStone = overrideStone - 1;
    }

    @Override
    public String toString() {
        return "map.Player " + number + " [o=" + overrideStone + " b=" + bomb + "]";
    }
}