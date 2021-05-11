package loganalyze.colorize;

import java.awt.*;

public class PlayerPoint {

    private static final Color[] colors = {
            new Color(214, 8, 7),
            new Color(4, 64, 195),
            new Color(0, 172, 2),
            new Color(247, 239, 20),
            new Color(24, 192, 255),
            new Color(101, 33, 130),
            new Color(247, 134, 50),
            new Color(255, 0, 175),
    };

    public int x;
    public int y;
    public int player;

    public PlayerPoint(int x, int y, int player) {
        this.player = player;
        this.x = x;
        this.y = y;
    }

    public Color getColor() {
        return colors[player - 1];
    }

    public static String getColor(int player) {
        switch (player) {
            case 1:
                return "ROT";
            case 2:
                return "BLAU";
            case 3:
                return "GR\u00dcN";
            case 4:
                return "GELB";
            case 5:
                return "CYAN";
            case 6:
                return "VIOLET";
            case 7:
                return "ORANGE";
            case 8:
                return "PINK";
            default:
                return null;
        }
    }

    @Override
    public String toString() {
        return "[(" + x + ", " + y + ") -> " + player + "]";
    }
}