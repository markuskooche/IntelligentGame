package map;

/**
 * The map.Transition class stores the information of a transition from one field to another. The coordinates
 * of both points, as well as the respective direction are stored. With the help of a hash function, you
 * can check transitions for correspondence and determine the counterpart of a position.
 *
 * @author Benedikt Halbritter
 * @author Iwan Eckert
 * @author Markus Koch
 */
public class Transition {

    private final int x;
    private final int y;
    private final int r;

    /**
     * Creates a transition with the start and destination points and the respective transition directions.
     *
     * @param x1 the x coordinate from the start transition
     * @param y1 the y coordinate from the start transition
     * @param r1 the direction from the start transition
     * @param x2 the x coordinate from the end transition
     * @param y2 the y coordinate from the end transition
     * @param r2 the direction from the end transition
     */
    public Transition(int x, int y, int r) {
        this.x = x;
        this.y = y;
        this.r = r;
    }

    /**
     * Returns the counterpart of the transition.
     *
     * @return an array with three elements [x, y, r]
     */
    public int[] getDestination() {
        return new int[] {x, y, r};
    }

    /**
     * Returns the x-coordinate of the destination.
     *
     * @return integer from the x coordinate
     */
    public int getX() {
        return x;
    }

    /**
     * Returns the y-coordinate of the destination.
     *
     * @return integer from the y coordinate
     */
    public int getY() {
        return y;
    }

    /**
     * Returns the direction of the destination.
     *
     * @return integer from the direction
     */
    public int getR() {
        return r;
    }

    /**
     * Static hash function to compare transitions
     *
     * @param x the x coordinate which should be hashed
     * @param y the y coordinate which should be hashed
     * @param r the direction which should be hashed
     *
     * @return An Integer which represents a transition
     */
    public static int hash(int x, int y, int r) {
        return 1000 * x + 10 * y + r;
    }

    @Override
    public String toString() {
        return x + " " + y + " " + r;
    }
}
