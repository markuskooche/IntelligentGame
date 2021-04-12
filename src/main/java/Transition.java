/**
 * The Transition class stores the information of a transition from one field to another. The coordinates
 * of both points, as well as the respective direction are stored. With the help of a hash function, you
 * can check transitions for correspondence and determine the counterpart of a position.
 *
 * @author Benedikt Halbritter
 * @author Iwan Eckert
 * @author Markus Koch
 */
public class Transition {

    private final int x1;
    private final int y1;
    private final int r1;
    private final int x2;
    private final int y2;
    private final int r2;

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
    public Transition(int x1, int y1, int r1, int x2, int y2, int r2) {
        this.x1 = x1;
        this.y1 = y1;
        this.r1 = r1;
        this.x2 = x2;
        this.y2 = y2;
        this.r2 = r2;
    }

    /**
     * Returns the counterpart of the transition.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param r the direction
     *
     * @return an array with three elements [x, y, r]
     */
    public int[] getDestination(int x, int y, int r) {
        if (x == x1 && y == y1 && r == r1) {
            return new int[] {x2, y2, r2};
        } else if (x == x2 && y == y2 && r == r2) {
            return new int[] {x1, y1, r1};
        } else {
            return null;
        }
    }

    /**
     * Returns the x-coordinate of the start transition.
     *
     * @return integer from the x coordinate
     */
    public int getX1() {
        return x1;
    }

    /**
     * Returns the y-coordinate of the start transition.
     *
     * @return integer from the y coordinate
     */
    public int getY1() {
        return y1;
    }

    /**
     * Returns the direction of the start transition.
     *
     * @return integer from the direction
     */
    public int getR1() {
        return r1;
    }

    /**
     * Returns the x-coordinate of the end transition.
     *
     * @return integer from the x coordinate
     */
    public int getX2() {
        return x2;
    }

    /**
     * Returns the y-coordinate of the end transition.
     *
     * @return integer from the y coordinate
     */
    public int getY2() {
        return y2;
    }

    /**
     * Returns the direction of the end transition.
     *
     * @return integer from the direction
     */
    public int getR2() {
        return r2;
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
        return x1 + " " + y1 + " " + r1 + " <-> " + x2 + " "+ y2 + " " + r2;
    }
}
