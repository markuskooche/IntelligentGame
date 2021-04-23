package loganalyze;

import java.awt.*;

public class BackgroundPoint {

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
            return new Color(245, 171, 171);
        } else if (field == 'i') {
            return new Color(139, 177, 223);
        } else if (field == 'b') {
            return new Color(153, 223, 133);
        } else if (field == 'x') {
            return new Color(203, 133, 223);
        } else {
            return new Color(255, 0,0);
        }
    }

    @Override
    public String toString() {
        return "[(" + x + ", " + y + ") -> " + field + "]";
    }
}
