package loganalyze;

import controller.Game;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class GameFileManager {

    private int playerAmount;
    private int height;
    private int width;
    private int player;
    private Game game;
    private final String filename;
    private ArrayList<FieldStatistic> boardArray;
    private ArrayList<String[]> playerArray;

    public GameFileManager(String filename) {
        this.filename = filename;
    }

    public void load() throws IOException {
        boardArray = new ArrayList<>();
        playerArray = new ArrayList<>();
        player = -1;

        if (!filename.equals("")) {
            Path path = Paths.get(filename);
            List<String> file = Files.lines(path).collect(Collectors.toList());
            parseFile(file);
        }
    }

    public String[] getBoard(int index) {
        String[] returnBoard = new String[height + 2];

        returnBoard[0] = "    ";
        for (int i = 0; i < width; i++) {
            returnBoard[0] += ((i % 10) + " ");
        }

        returnBoard[1] = "  /";
        for (int i = 0; i < width; i++) {
            returnBoard[1] += "--";
        }

        for (int y = 0; y < height; y++) {
            returnBoard[y + 2] = (y % 10) + " | ";
            for (int x = 0; x < width; x++) {
                returnBoard[y + 2] += boardArray.get(index).getPiece(x, y);
            }
        }
        return returnBoard;
    }

    public String[] getPlayer(int index) {
        return playerArray.get(index);
    }

    public int getMoveSize() {
        return boardArray.size() - 1;
    }

    public int getGameSize() {
        return boardArray.size();
    }

    public int getPlayerAmount() {
        return playerAmount;
    }

    public int getBombRadius() {
        return game.getBoard().getBombRadius();
    }

    private void parseFile(List<String> file) {
        for (String line : file) {
            if (line.length() >= 6) {
                String id = line.substring(0, 7);

                switch (id) {
                    case "XT01-01":
                        System.out.println("Type 1");
                        break;
                    case "XT01-02":
                        loadGame(line);
                        break;
                    case "XT01-03":
                        setPlayer(line);
                        break;
                    case "XT01-06":
                        executeMove(line);
                        break;
                    default:
                        System.out.println("NOT READABLE: " + line);
                        break;
                }
            }
        }
    }

    private void setPlayer(String line) {
        // ["XT01", "03", "PL", "0?"] -> the '?' is your player
        String[] lineArray = line.split("-");
        player = Integer.parseInt(lineArray[3]);
    }

    public char getPlayer() {
        if (player == -1) {
            return '?';
        }

        return (char) (player + '0');
    }

    private char[] getGameState(String[] map) {
        char[] data = new char[map.length];
        int counter = 0;

        for (String number : map) {
            char info = (char) Byte.parseByte(number);
            data[counter++] = info;
        }

        return data;
    }

    private void loadGame(String line) {
        String tmp = line.substring(9, (line.length() - 1));
        String[] lineArray = tmp.split(", ");
        char[] data = getGameState(lineArray);

        String string = String.valueOf(data);
        String[] lines = string.split("\n");

        List<String> map = new LinkedList<>(Arrays.asList(lines));
        game = new Game(map);

        height = game.getBoard().getHeight();
        width = game.getBoard().getWidth();
        playerAmount = game.getPlayers().length;

        addBoardArray();
        setPlayerArray();
    }

    private void executeMove(String line) {
        // ["XT01", "06", "PL", "0p", "xx", "yy", "SF", "sm"]
        // xx => x-Coordinate
        // yy => y-Coordinate
        // sm => Special Move
        String[] lineArray = line.split("-");
        int p = Integer.parseInt(lineArray[3]);
        int x = Integer.parseInt(lineArray[4]);
        int y = Integer.parseInt(lineArray[5]);
        int s = Integer.parseInt(lineArray[7]);
        game.executeMove(x, y, p, s);

        addBoardArray();
        setPlayerArray();
    }

    private void addBoardArray() {
        FieldStatistic field = new FieldStatistic(game.getBoard().getField());
        boardArray.add(field);
    }

    private void setPlayerArray() {
        PlayerStatistic player = new PlayerStatistic(game.getPlayers(), playerAmount);
        playerArray.add(player.getPlayer());
    }

    private void disqualifyPlayer(String line) {

    }
}
