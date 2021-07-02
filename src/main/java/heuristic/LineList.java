package heuristic;

import map.Move;

import java.util.ArrayList;
import java.util.List;

public class LineList {

    private List<Line> cornerLines;
    private List<Line> edgeLines;
    private List<Line> openLines;
    private List<Line> controlLines;
    private List<Line> caughtLines;
    private List<Move> bonusMoves;
    private List<Move> inversionMoves;
    private List<Move> choiceMoves;

    public LineList() {
        cornerLines = new ArrayList<>();
        edgeLines = new ArrayList<>();
        openLines = new ArrayList<>();
        controlLines = new ArrayList<>();
        caughtLines = new ArrayList<>();
        bonusMoves = new ArrayList<>();
        inversionMoves = new ArrayList<>();
        choiceMoves = new ArrayList<>();
    }

    public void add(Line line, LineState lineState) {
        if (lineState == LineState.CORNER) cornerLines.add(line);
        if (lineState == LineState.EDGE) edgeLines.add(line);
        if (lineState == LineState.CONTROL) controlLines.add(line);
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

    public List<Line> getCornerLines() {
        return cornerLines;
    }

    public List<Line> getEdgeLines() {
        return edgeLines;
    }

    public List<Line> getOpenLines() {
        return openLines;
    }

    public List<Line> getControlLines() {
        return controlLines;
    }

    public List<Line> getCaughtLines() {
        return caughtLines;
    }
}
