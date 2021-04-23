package loganalyze;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public class GameField {

    public static class GamePanel extends JPanel {

        private int highlightedPlayer = -1;

        private List<BackgroundPoint> backgrounds;
        private List<PlayerPoint> players;

        public GamePanel() {
            backgrounds = new LinkedList<>();
            players = new LinkedList<>();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            for (BackgroundPoint background : backgrounds) {
                int cellX = background.x * 15;
                int cellY = background.y * 15;

                if (highlightedPlayer == -1) {
                    g.setColor(background.getColor());
                } else {
                    if ("bicx".indexOf(background.field) != -1) {
                        g.setColor(Color.LIGHT_GRAY);
                    } else {
                        g.setColor(background.getColor());
                    }
                }

                g.fillRect(cellX, cellY, 15, 15);
            }

            for (PlayerPoint player : players) {
                int cellX = player.x * 15 + 1;
                int cellY = player.y * 15 + 1;

                if (highlightedPlayer == -1) {
                    g.setColor(player.getColor());
                } else {
                    if (highlightedPlayer == player.player) {
                        g.setColor(player.getColor());
                    } else {
                        g.setColor(Color.LIGHT_GRAY);
                    }
                }

                g.fillRoundRect(cellX, cellY, 13, 13, 13, 13);
            }
            g.setColor(Color.BLACK);
            g.drawRect(0, 0, 750, 750);

            for (int i = 0; i <= 750; i += 15) {
                g.drawLine(i, 0, i, 750);
            }

            for (int i = 0; i <= 750; i += 15) {
                g.drawLine(0, i, 750, i);
            }
        }

        public void updateFrame(List<BackgroundPoint> backgrounds, List<PlayerPoint> players) {
            this.backgrounds = backgrounds;
            this.players = players;
            repaint();
        }

        public void highlightPlayer(int x, int y) {
            for (PlayerPoint player : players) {
                if (player.x == x && player.y == y) {
                    if (highlightedPlayer == player.player) {
                        highlightedPlayer = -1;
                    } else {
                        highlightedPlayer = player.player;
                    }

                    repaint();
                    break;
                }
            }
        }

        public void highlightPlayer(int playerNumber) {
            if (highlightedPlayer != playerNumber) {
                for (PlayerPoint player : players) {
                    if (player.player == playerNumber) {
                        highlightedPlayer = player.player;
                        break;
                    }
                }
            } else {
                highlightedPlayer = -1;
            }

            repaint();
        }

        public void disableHighlighting() {
            highlightedPlayer = -1;
        }

        /*
        public void addBackground(int x, int y, char field) {
            backgrounds.add(new BackgroundPoint(x, y, field));
            repaint();
        }

        public void addPlayer(int x, int y, int player) {
            players.add(new PlayerPoint(x, y, player));
            repaint();
        }
         */
    }
}
