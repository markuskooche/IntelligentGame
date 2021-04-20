package loganalyze;

import java.util.Arrays;

public class AnalyzeParser {
    
    private static final String group = "XT01-";
    
    public static void parseBoard(byte[] map) {
        System.out.println(group + "02-" + Arrays.toString(map));
    }

    public static void setPlayer(int number) {
        System.out.println(group + "03-PL-" + String.format("%02d", number));
    }

    public static void parseMove(int x, int y, int player, int special) {
        System.out.println(group + "06-PL-" + String.format("%02d", player) + "-"
                + String.format("%02d", x) + "-" + String.format("%02d", y)
                + "-SF-" + String.format("%02d", special));
    }

    public static void disqualifyPlayer(byte player) {
        System.out.println(group + "07-PL-" + String.format("%02d", player));
    }

    public static void startBombPhase() {
        System.out.println(group + "08");
    }

    public static void endGame() {
        System.out.println(group + "09");
    }

    public static void main(String[] args) {
        //loganalyze.AnalyzeParser.parseBoard();
        AnalyzeParser.parseMove(1, 2, (byte) 3, (byte) 0);
        AnalyzeParser.disqualifyPlayer((byte) 2);
        AnalyzeParser.startBombPhase();
        AnalyzeParser.endGame();
    }
}
