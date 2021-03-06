package loganalyze.colorize;

import java.awt.*;

/**
 * The BackgroundPoint classes is used to color rectangular fields on the GamePanel
 *
 * @author Benedikt Halbritter
 * @author Iwan Eckert
 * @author Markus Koch
 */
public class BackgroundPoint {

    public static final Color EXPANSION = new Color(203, 133, 223);
    public static final Color INVERSION = new Color(139, 177, 223);
    public static final Color CHOICE = new Color(245, 171, 171);
    public static final Color BONUS = new Color(153, 223, 133);

    public static final Color NOT_REACHABLE = new Color(248, 99, 99);
    public static final Color REACHABLE = new Color(119, 247, 119);
    public static final Color BORDER = new Color(255, 0,0);

    public final char piece;
    public final int x;
    public final int y;

    /**
     * Create a new BackgroundPoint
     *
     * @param x x coordinate
     * @param y y coordinate
     * @param piece piece of the selected field
     */
    public BackgroundPoint(int x, int y, char piece) {
        this.piece = piece;
        this.x = x;
        this.y = y;
    }

    /**
     * Get the color from the specific Piece
     *
     * @return the color form the piece
     */
    public Color getColor() {
        if (piece == '0') {
            return new Color(238, 238, 238);
        } else if (piece == '-') {
            return new Color(35, 35, 35);
        } else if (piece == 'c') {
            return CHOICE;
        } else if (piece == 'i') {
            return INVERSION;
        } else if (piece == 'b') {
            return BONUS;
        } else if (piece == 'x') {
            return EXPANSION;
        } else if (piece == 'R') {
            return REACHABLE;
        } else if (piece == 'N') {
            return NOT_REACHABLE;
        } else {
            return BORDER;
        }
    }

    @Override
    public String toString() {
        return "[(" + x + ", " + y + ") -> " + piece + "]";
    }
}
