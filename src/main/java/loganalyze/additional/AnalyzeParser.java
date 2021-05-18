package loganalyze.additional;

import map.Board;
import map.Move;
import map.Player;

import java.util.Arrays;
import java.util.List;

public class AnalyzeParser {

    private final String group;
    private final boolean output;
    private final boolean reduce;

    public AnalyzeParser(int group, boolean output, boolean reduce) {
        this.group =  ("XT0" + group + "-");
        this.output = output;
        this.reduce = reduce;
    }

    public void printGameInformation(boolean alphaBeta) {
        if (output) {
            if (alphaBeta) {
                System.out.println(group + "ALPHA-BETA-ON");
            } else {
                System.out.println(group + "ALPHA-BETA-OFF");
            }
        }
    }

    public void printCurrentTime(int player) {
        if (output) {
            System.out.println("XT01-PL-0" + player + "-" + System.currentTimeMillis());
        }
    }
    
    public void parseBoard(byte[] map) {
        if (output) {
            System.out.println(group + "02-" + Arrays.toString(map));
        }
    }

    public void mapAnalyzerError() {
        if (output) {
            System.out.println(group + "COULD_NOT_SETUP_MAP_CORRECTLY");
        }
    }

    public void setPlayer(int number) {
        if (output) {
            System.out.println(group + "03-PL-" + String.format("%02d", number));
        }
    }

    public void sendMove(int x, int y, int player, int special) {
        if (output) {
            System.out.println(group + "05-PL-" + String.format("%02d", player) + "-"
                    + String.format("%02d", x) + "-" + String.format("%02d", y)
                    + "-SF-" + String.format("%02d", special));
        }
    }

    public void parseMove(int x, int y, int player, int special) {
        if (output) {
            System.out.println(group + "06-PL-" + String.format("%02d", player) + "-"
                    + String.format("%02d", x) + "-" + String.format("%02d", y)
                    + "-SF-" + String.format("%02d", special));
        }
    }

    public void disqualifyPlayer(byte player) {
        if (output) {
            System.out.println(group + "07-PL-" + String.format("%02d", player));
        }
    }

    public void disqualifiedSelf(byte ourPlayer, Board board) {
        if (output && !reduce) {
            System.out.println("WE WERE DISQUALIFIED (PLAYER " + ourPlayer + ")\n");
            System.out.println(board.toString());
        }
    }

    public void startBombPhase() {
        if (output) {
            System.out.println(group + "08");
        }
    }

    public void endGame() {
        if (output) {
            System.out.println(group + "09");
        }
    }

    public void loggingBoard(char[][] field) {
        if (output && !reduce) {
            System.out.println("[MAP]");

            int height = field.length;
            int width = field[0].length;

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    System.out.print(field[y][x] + " ");
                }
                System.out.println();
            }

            System.out.println("[END]");
        }
    }

    public void loggingBoard(Board board, Player player) {
        if (output && !reduce) {
            System.out.println("[MAP]");
            List<Move> legalMoves = board.getLegalMoves(player, true);
            char[][] field = board.getField();

            for (int y = 0; y < board.getHeight(); y++) {
                for (int x = 0; x < board.getWidth(); x++) {
                    System.out.print(field[y][x]);

                    boolean isMove = false;
                    for (Move legalMove : legalMoves) {
                        if (legalMove.isMove(new int[]{x, y})) {
                            System.out.print("'");
                            isMove = true;
                            break;
                        }
                    }

                    if (!isMove) {
                        System.out.print(" ");
                    }
                }
                System.out.println();
            }

            System.out.println("[END]");
        }
    }

    public void couldNotSend() {
        if (output) {
            System.out.println("Client could not sent a message.");
            System.out.println("Server has been terminated.");
        }
    }

    public void couldNotReceive() {
        if (output) {
            System.out.println("Client could not receive a message.");
            System.out.println("Server has been terminated.");
        }
    }

    public boolean isPrintable() {
        return (output && !reduce);
    }
}
