package loganalyze;

import java.awt.*;

public class BackgroundPoint {

    public static final Color EXPANSION = new Color(203, 133, 223);
    public static final Color INVERSION = new Color(139, 177, 223);
    public static final Color CHOICE = new Color(245, 171, 171);
    public static final Color BONUS = new Color(153, 223, 133);

    public static final Color NOT_REACHABLE = new Color(248, 99, 99);
    public static final Color REACHABLE = new Color(119, 247, 119);

    public int x;
    public int y;
    public char field;

    public BackgroundPoint(int x, int y, char field) {
        this.field = field;
        this.x = x;
        this.y = y;
    }

    public Color getColor() {
        if (field == '0') {
            return new Color(238, 238, 238);
        } else if (field == '-') {
            return new Color(35, 35, 35);
        } else if (field == 'c') {
            return BackgroundPoint.CHOICE;
        } else if (field == 'i') {
            return BackgroundPoint.INVERSION;
        } else if (field == 'b') {
            return BackgroundPoint.BONUS;
        } else if (field == 'x') {
            return BackgroundPoint.EXPANSION;
        } else if (field == 'R') {
            return BackgroundPoint.REACHABLE;
        } else if (field == 'N') {
            return BackgroundPoint.NOT_REACHABLE;
        } else {
            return new Color(255, 0,0);
        }
    }

    @Override
    public String toString() {
        return "[(" + x + ", " + y + ") -> " + field + "]";
    }
}
