package loganalyze.controller;

import controller.Game;
import loganalyze.colorize.BackgroundPoint;
import loganalyze.colorize.PlayerPoint;
import loganalyze.tablemodel.PlayerInformation;
import map.Transition;
import mapanalyze.MapAnalyzer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class MenuBarPanelManager {

    private final List<int[]> transitionList;
    private LinkedList<BackgroundPoint> reachableField;

    public MenuBarPanelManager() {
        transitionList = new LinkedList<>();
    }

    public void setTransitions(Game game) {
        Collection<Transition> transitions = game.getBoard().getAllTransitions().values();

        ArrayList<Transition> tmpList = new ArrayList<>();

        for (Transition transition : transitions) {
            int x = transition.getX();
            int y = transition.getY();
            int r = transition.getR();

            Transition opposite = game.getBoard().getTransition(x, y, r);

            if (!tmpList.contains(transition) || !tmpList.contains(opposite)) {
                int oppositeX = opposite.getX();
                int oppositeY = opposite.getY();
                int oppositeR = opposite.getR();

                int[] newTransition = new int[6];
                newTransition[0] = x;
                newTransition[1] = y;
                newTransition[2] = r;
                newTransition[3] = oppositeX;
                newTransition[4] = oppositeY;
                newTransition[5] = oppositeR;

                transitionList.add(newTransition);

                tmpList.add(transition);
                tmpList.add(opposite);
            }
        }
    }

    public List<int[]> getTransitions() {
        return transitionList;
    }

    public boolean hasReachableField() {
        return ((reachableField != null) && (reachableField.size() > 0));
    }

    public void setReachableField(Game game) {
        reachableField = new LinkedList<>();

        int height = game.getBoard().getHeight();
        int width = game.getBoard().getWidth();

        game.startReachableField();
        int[][] field = game.getReachableField();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int piece = field[y][x];
                if (piece == MapAnalyzer.REACHABLE) {
                    reachableField.add(new BackgroundPoint(x, y, 'R'));
                } else if (piece == MapAnalyzer.UNREACHABLE) {
                    if (game.getBoard().getField()[y][x] == '-') {
                        reachableField.add(new BackgroundPoint(x, y, '-'));
                    } else {
                        reachableField.add(new BackgroundPoint(x, y, 'N'));
                    }
                }
            }
        }

        if (height < 50) {
            for (int x = 0; x < width; x++) {
                reachableField.add(new BackgroundPoint(x, height, ' '));
            }
            if (width < 50) {
                reachableField.add(new BackgroundPoint(width, height, ' '));
            }
        }

        if (width < 50) {
            for (int y = 0; y < height; y++) {
                reachableField.add(new BackgroundPoint(width, y, ' '));
            }
            if (height < 50) {
                reachableField.add(new BackgroundPoint(width, height, ' '));
            }
        }
    }
    /*
    public boolean isReachableFinished() {
        return game.isReachableFinished();
    }
    */

    public LinkedList<BackgroundPoint> getReachableField() {
        if (reachableField != null) {
            return reachableField;
        }

        return new LinkedList<>();
    }
}