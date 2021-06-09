package heuristic;

import map.Move;

import java.util.ArrayList;
import java.util.List;

public class LineList {

    private List<Line> wallLines;
    private List<Line> openLines;
    private List<Line> controllLines;
    private List<Line> caughtLines;
    private List<Move> bonusMoves;
    private List<Move> inversionMoves;
    private List<Move> choiceMoves;

    public LineList() {
        wallLines = new ArrayList<>();
        openLines = new ArrayList<>();
        controllLines = new ArrayList<>();
        caughtLines = new ArrayList<>();
        bonusMoves = new ArrayList<>();
        inversionMoves = new ArrayList<>();
        choiceMoves = new ArrayList<>();
    }

    public void add(Line line, LineState lineState) {
        if (lineState == LineState.WALL) wallLines.add(line);
        if (lineState == LineState.CONTROLL) controllLines.add(line);
        if (lineState == LineState.OPEN) openLines.add(line);
        if (lineState == LineState.CAUGHT) caughtLines.add(line);
    }

    public void addBonus(Move bonusMove) {
        bonusMoves.add(bonusMove);
    }

    public void addInversion(Move inversionMove) {
        inversionMoves.add(inversionMove);
    }

    public void addChoice(Move choiceMove) {
        choiceMoves.add(choiceMove);
    }

    public List<Move> getBonusMoves() {
        return bonusMoves;
    }

    public List<Move> getInversionMoves() {
        return inversionMoves;
    }

    public List<Move> getChoiceMoves() {
        return choiceMoves;
    }

    public List<Line> getWallLines() {
        return wallLines;
    }

    public List<Line> getOpenLines() {
        return openLines;
    }

    public List<Line> getControllLines() {
        return controllLines;
    }
}
