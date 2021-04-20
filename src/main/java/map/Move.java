package map;

import java.util.ArrayList;
import java.util.List;

/**
 * The map.Move class creates an allowed move in one direction. If there is a valid position with several playing
 * fields to be colored in different directions, several map.Move classes must be created. In a map.Move class, both
 * fields to be colored in and any kind of additional information, such as inversion, choice, etc., are stored.
 *
 * @author Benedikt Halbritter
 * @author Iwan Eckert
 * @author Markus Koch
 */
public class Move {

    private List<int[]> list;
    private boolean inversion;
    private boolean override;
    private boolean choice;
    private boolean bonus;

    /**
     * Creates a map.Move class that contains all information about a legal move.
     */
    public Move() {
        list = new ArrayList<>();
        inversion = false;
        override = false;
        choice = false;
    }

    /**
     * Creates a map.Move class that contains all information about a legal move.
     * This constructor is called if it is an override stone.
     *
     * @param element a integer array with a coordinate [x, y]
     */
    public Move(int[] element) {
        list = new ArrayList<>();
        list.add(element);
        inversion = false;
        override = true;
        choice = false;
    }

    /**
     * Creates a map.Move class that contains all information about a legal move.
     * This constructor is called when you want to create a new instance of an existing move.
     *
     * @param move a move class which should be copied
     */
    public Move(Move move) {
        list = new ArrayList<>();
        for (int[] item : move.getList()) {
            add(item);
        }
        this.inversion = move.isInversion();
        this.override = move.isOverride();
        this.choice = move.isChoice();
        this.bonus = move.isBonus();
    }

    /**
     * Adds a new coordinate to the current list.
     *
     * @param element an array with two elements [x, y]
     */
    public void add(int[] element) {
        list.add(element);
    }

    /**
     * Returns a list of expired coordinates.
     *
     * @return a list of arrays with two elements [x, y]
     */
    public List<int[]> getList() {
        return list;
    }

    /**
     * Sets the Bonus property to true.
     */
    public void setBonus() {
        bonus = true;
    }

    /**
     * Returns true if the Bonus property is true.
     *
     * @return status of Bonux property
     */
    public boolean isBonus() {
        return bonus;
    }

    /**
     * Sets the Choice property to true.
     */
    public void setChoice() {
        choice = true;
    }

    /**
     * Returns true if the Choice property is true.
     *
     * @return status of Choice property
     */
    public boolean isChoice() {
        return choice;
    }

    /**
     * Sets the Inversion property to true.
     */
    public void setInversion() {
        this.inversion = true;
    }

    /**
     * Returns true if the Inversion property is true.
     *
     * @return status of Inversion property
     */
    public boolean isInversion() {
        return inversion;
    }

    /**
     * Sets the Override property to true.
     */
    public void setOverride() {
        this.override = true;
    }

    /**
     * Returns true if the Override property is true.
     *
     * @return status of Override property
     */
    public boolean isOverride() {
        return override;
    }

    /**
     * Returns true if this list contains no elements.
     *
     * @return true if this list contains no elements
     */
    public boolean isEmpty() {
        return list.isEmpty();
    }

    /**
     * Returns true if this list contains the passed coordinate.
     *
     * @param x integer of the x coordinate
     * @param y integer of the y coordinate
     *
     * @return true if this list contains the passed coordinate
     */
    public boolean contains(int x, int y) {
        for (int[] item : list) {
            if (item[0] == x && item[1] == y) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the number of elements in this list.
     *
     * @return the number of elements in this list
     */
    public int size() {
        return list.size();
    }

    /**
     * Returns true if it is a direction of the direction to be colored.
     *
     * @param move coordinates of the end position [x, y]
     *
     * @return true if it is a direction to be colorized.
     */
    public boolean isMove(int[] move) {
        return move[0] == getX() && move[1] == getY();
    }

    public int getX() {
        return list.get(0)[0];
    }

    public int getY() {
        return list.get(0)[1];
    }

    public void merge(Move two) {
        if (two.isInversion()) {
            setInversion();
        }
        if (two.isOverride()) {
            setOverride();
        }
        if (two.isChoice()) {
            setChoice();
        }
        if (two.isBonus()) {
            setBonus();
        }

        for (int[] position : two.getList()) {
            add(position);
        }
    }

    @Override
    public String toString() {
        StringBuilder moveString = new StringBuilder();

        if (list.size() != 0) {
            moveString.append("(" + getX() + ", " + getY() + ")\t | {I: " + inversion + " C: " + choice + " B: " + bonus + " O: " + override + "}");
            moveString.append("\t -> [");

            for (int i = 0; i < list.size(); i++) {
                moveString.append("(" + list.get(i)[0] + ", " + list.get(i)[1] + ")");
            }
            moveString.append("]");
        } else {
            moveString.append("EMPTY");
        }

        return moveString.toString();
    }
}