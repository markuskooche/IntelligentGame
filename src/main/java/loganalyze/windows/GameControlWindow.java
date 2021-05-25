package loganalyze.windows;

import controller.Game;
import loganalyze.additional.OSValidator;
import loganalyze.colorize.BackgroundPoint;
import loganalyze.colorize.PlayerPoint;
import loganalyze.windows.panel.GameField;
import map.Move;
import map.Player;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;

public class GameControlWindow extends JDialog {

    private int[] selectedPosition = new int[] {-1, -1};
    private List<Move> legalMoves = new LinkedList<>();
    private char previousPiece;

    private final GameField.GamePanel gamePanel;
    private final Game game;

    private final int height;
    private final int width;

    public GameControlWindow(JFrame parent, Game currentGame, LinkedList<BackgroundPoint> background, LinkedList<PlayerPoint> players) {
        super(parent);
        this.game = new Game(currentGame);

        height = game.getBoard().getHeight();
        width = game.getBoard().getWidth();

        setModal(false);
        setLayout(null);
        setResizable(false);
        if (OSValidator.isMac()) {
            setSize(770, 800);
        } else {
            setSize(785, 810);
        }
        setTitle("Manuelle Spielf\u00fchrung");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        gamePanel = new GameField.GamePanel();
        gamePanel.setBounds(0, 0, 770, 770);
        gamePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int x = (e.getX() / 15) - 1;
                int y = (e.getY() / 15) - 1;

                if ((x >= 0 && x < width) && (y >= 0 && y < height)) {
                    if (selectedPosition[0] != x || selectedPosition[1] != y) {
                        char selectedPiece = getSelectedPlayer(x, y);

                        if (legalMoves.size() > 0) {
                            for (Move legalMove : legalMoves) {
                                if (legalMove.isMove(new int[]{x, y})) {
                                    int player = previousPiece - '0';
                                    game.executeMove(x, y, player, 0);
                                    resetStorage();
                                    updateBoard();
                                }
                            }
                        } else {
                            if ("12345678".indexOf(selectedPiece) != -1) {
                                boolean hasLegalMoves = activeLegalMoves(selectedPiece);

                                if (hasLegalMoves) {
                                    selectedPosition = new int[]{x, y};
                                    previousPiece = selectedPiece;
                                } else {
                                    resetStorage();
                                    updateBoard();
                                }
                            }
                        }
                    } else {
                        resetStorage();
                        updateBoard();
                    }
                }
            }
        });
        add(gamePanel);

        gamePanel.updateFrame(background, players);
        setVisible(true);
    }

    private void resetStorage() {
        selectedPosition = new int[] {-1, -1};
        legalMoves = new LinkedList<>();
        previousPiece = 'S';
    }

    private char getSelectedPlayer(int selectedX, int selectedY) {
        char[][] field = game.getBoard().getField();

        if ((selectedX >= 0 && selectedX < width) && (selectedY >= 0 && selectedY < height)) {
            return field[selectedY][selectedX];
        }

        return '0';
    }

    private boolean activeLegalMoves(char playerCharacter) {
        int playerNumber = playerCharacter - '0';
        Player player = game.getPlayer(playerNumber);
        System.out.println(player);

        legalMoves = game.getBoard().getLegalMoves(player, true);
        boolean hasLegalMoves = legalMoves.size() > 0;

        if (hasLegalMoves) {
            activateMovePoints(player);
            return true;
        }

        return false;
    }

    private void activateMovePoints(Player player) {
        LinkedList<BackgroundPoint> backgroundPoints = new LinkedList<>();
        LinkedList<PlayerPoint> playerPoints = new LinkedList<>();
        char[][] field = game.getBoard().getField();
        boolean[][] alreadyAdded = new boolean[height][width];

        List<Move> normalMoves = game.getBoard().getLegalMoves(player, false);

        for (Move normalMove : normalMoves) {
            int x = normalMove.getX();
            int y = normalMove.getY();

            playerPoints.add(new PlayerPoint(x, y, 9));
            alreadyAdded[y][x] = true;
        }

        for (Move legalMove : legalMoves) {
            int x = legalMove.getX();
            int y = legalMove.getY();

            if (!alreadyAdded[y][x]) {
                playerPoints.add(new PlayerPoint(x, y, 10));
                alreadyAdded[y][x] = true;
            }
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (!alreadyAdded[y][x]) {
                    if (field[y][x] == player.getCharNumber()) {
                        int playerNumber = player.getCharNumber() - '0';
                        playerPoints.add(new PlayerPoint(x, y, playerNumber));
                    } else if ("12345678".indexOf(field[y][x]) != -1) {
                        playerPoints.add(new PlayerPoint(x, y, 11));
                    } else {
                        if ("bicx-".indexOf(field[y][x]) != -1) {
                            backgroundPoints.add(new BackgroundPoint(x, y, field[y][x]));
                        }
                    }

                    alreadyAdded[y][x] = true;
                }
            }
        }

        backgroundPoints.addAll(addBackgroundShadow());
        gamePanel.updateFrame(backgroundPoints, playerPoints);
    }

    private void updateBoard() {
        char[][] field = game.getBoard().getField();

        LinkedList<BackgroundPoint> backgroundPoints = getBackgroundPoints(field);
        LinkedList<PlayerPoint> playerPoints = getPlayerPoints(field);

        gamePanel.updateFrame(backgroundPoints, playerPoints);
    }

    private LinkedList<PlayerPoint> getPlayerPoints(char[][] field) {
        LinkedList<PlayerPoint> tmp = new LinkedList<>();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if ("12345678".indexOf(field[y][x]) != -1) {
                    tmp.add(new PlayerPoint(x, y, (field[y][x] - '0')));
                }
            }
        }

        return tmp;
    }

    private LinkedList<BackgroundPoint> getBackgroundPoints(char[][] field) {
        LinkedList<BackgroundPoint> tmp = new LinkedList<>();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if ("12345678".indexOf(field[y][x]) == -1) {
                    tmp.add(new BackgroundPoint(x, y, field[y][x]));
                }
            }
        }

        tmp.addAll(addBackgroundShadow());
        return tmp;
    }

    private LinkedList<BackgroundPoint> addBackgroundShadow() {
        LinkedList<BackgroundPoint> tmp = new LinkedList<>();

        if (height < 50) {
            for (int x = 0; x < width; x++) {
                tmp.add(new BackgroundPoint(x, height, ' '));
            }
            if (width < 50) {
                tmp.add(new BackgroundPoint(width, height, ' '));
            }
        }

        if (width < 50) {
            for (int y = 0; y < height; y++) {
                tmp.add(new BackgroundPoint(width, y, ' '));
            }
            if (height < 50) {
                tmp.add(new BackgroundPoint(width, height, ' '));
            }
        }

        return tmp;
    }
}
