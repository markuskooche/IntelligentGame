package loganalyze;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public class GameField {

    public static class GamePanel extends JPanel {

        private int highlightedPlayer = -1;

        private boolean showTransition = false;
        private List<int[]> transitions;

        private List<BackgroundPoint> backgrounds;
        private List<PlayerPoint> players;

        public GamePanel() {
            transitions = new LinkedList<>();
            backgrounds = new LinkedList<>();
            players = new LinkedList<>();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            for (BackgroundPoint background : backgrounds) {
                int cellX = background.x * 15 + 10;
                int cellY = background.y * 15 + 10;

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
                int cellX = player.x * 15 + 11;
                int cellY = player.y * 15 + 11;

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
            g.drawRect(10, 10, 750, 750);

            for (int i = 10; i <= 760; i += 15) {
                g.drawLine(i, 10, i, 760);
            }

            for (int i = 10; i <= 760; i += 15) {
                g.drawLine(10, i, 760, i);
            }

            if (showTransition) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setColor(Color.GREEN);
                g2d.setStroke(new BasicStroke(2));

                for (int[] transition : transitions) {
                    int x1 = (transition[0] * 15) + 18;
                    int y1 = (transition[1] * 15) + 18;
                    int r1 = transition[2];
                    int[] end1 = getLineDirection(x1, y1, r1);
                    g2d.drawLine(x1, y1, end1[0], end1[1]);

                    int x2 = (transition[3] * 15) + 18;
                    int y2 = (transition[4] * 15) + 18;
                    int r2 = transition[5];
                    int[] end2 = getLineDirection(x2, y2, r2);
                    g2d.drawLine(x2, y2, end2[0], end2[1]);

                    g2d.drawLine(end1[0], end1[1], end2[0], end2[1]);
                }
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

        public void setTransitions(List<int[]> transitions) {
            this.transitions = transitions;
        }

        public void showTransitions() {
            showTransition = true;
            repaint();
        }

        public void hideTransitions() {
            showTransition = false;
            repaint();
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

        private int[] getLineDirection(int x, int y, int r) {
            switch (r) {
                case 0:
                    return new int[] {x, (y - 15)};
                case 1:
                    return new int[] {(x + 15), (y - 15)};
                case 2:
                    return new int[] {(x + 15), y};
                case 3:
                    return new int[] {(x + 15), (y + 15)};
                case 4:
                    return new int[] {x, (y + 15)};
                case 5:
                    return new int[] {(x - 15), (y + 15)};
                case 6:
                    return new int[] {(x - 15), y};
                case 7:
                    return new int[] {(x - 15), (y - 15)};
                default:
                    return new int[] {x, y};
            }
        }
    }
}
