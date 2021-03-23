package client;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class Game {

    private Player[] players;
    public Board board;

    private int bombRadius;

    public Game(String filePath) {
        Path path = Paths.get(filePath);

        try {
            List<String> lines = Files.lines(path).collect(Collectors.toList());

            int playerAmount = Integer.parseInt(lines.get(0));
            int overrideStones = Integer.parseInt(lines.get(1));

            String[] bombInfo = lines.get(2).split(" ");
            int bombs = Integer.parseInt(bombInfo[0]);
            bombRadius = Integer.parseInt(bombInfo[1]);

            players = new Player[playerAmount];
            for (int i = 0; i < playerAmount; i++) {
                players[i] = new Player(overrideStones, bombs);
            }

            String[] sizeInfo = lines.get(3).split(" ");
            int height = Integer.parseInt(sizeInfo[0]);
            int width = Integer.parseInt(sizeInfo[1]);

            board = new Board(height, width);

            for (int i = 0; i < height; i++) {
                String[] mapLine = lines.get(i + 4).split(" ");

                for (int j = 0; j < width; j++) {
                    board.setPiece(i, j, mapLine[j].charAt(0));
                }
            }

            int currentLine = height + 4;
            int x1, y1, r1, x2, y2, r2;

            while (currentLine < lines.size()) {
                String[] line = lines.get(currentLine).split(" ");

                x1 = Integer.parseInt(line[0]);
                y1 = Integer.parseInt(line[1]);
                r1 = Integer.parseInt(line[2]);
                x2 = Integer.parseInt(line[4]);
                y2 = Integer.parseInt(line[5]);
                r2 = Integer.parseInt(line[6]);

                //System.out.println("'" +  x1 + "' '" +  y1 + "' '" +  r1 + "' '" +  x2 + "' '" +  y2 + "' '" +  r2 + "'");

                board.setTransition(x1, y1, r1, x2, y2, r2);
                currentLine++;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void transitions(int height, int width) {
        for (String item : board.getElement(height, width).getTransition()) {
            System.out.println(item);
        }
    }

    @Override
    public String toString() {
        StringBuilder gameString = new StringBuilder();

        gameString.append(String.format("Anzahl der Spieler: %s\n", players.length));
        gameString.append(String.format("Anzahl der Überschreibsteine: %s\n", players[0].getOverridesStones()));
        gameString.append(String.format("Anzahl der Bomben: %s\n", players[0].getBombs()));
        gameString.append(String.format("Stärke der Bomben: %s\n", bombRadius));

        gameString.append(String.format("\nZeilenanzahl des Spielfelds: %s\n", board.getHeight()));
        gameString.append(String.format("Spaltenanzahl des Spielfelds: %s\n\n", board.getWidth()));

        gameString.append(String.format("%s\n", board.toString()));

        return gameString.toString();
    }
}