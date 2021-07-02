package heuristic;

import map.*;
import mapanalyze.MapAnalyzer;
import timelimit.TimeExceededException;
import timelimit.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LineFinder {

    private MapAnalyzer mapAnalyzer;

    public LineFinder(MapAnalyzer mapAnalyzer) {
        this.mapAnalyzer = mapAnalyzer;
    }

    public LineList findLines(List<Move> moves, Board board, Player ourPlayer, boolean timeLimited, Token timeToken) throws TimeExceededException {
        int size = moves.size();
        char ourNumber = ourPlayer.getCharNumber();
        LineList lineList = new LineList();
        for (int i = 0; i < size; i++) {
            if (timeLimited && timeToken.timeExceeded()) throw new TimeExceededException();

            Move move = moves.get(i);
            char field = board.getField()[move.getY()][move.getX()];
            Map<int[],int[]> playerDirections = move.getPlayerDirections();

            if (field == 'b') lineList.addBonus(move);
            if (field == 'c') lineList.addChoice(move);
            if (field == 'i') lineList.addInversion(move);
            if ("bic".indexOf(field) > -1) continue;

            //Build initial stoneRow
            List<Character> stoneRow = new ArrayList<>();
            for (int j = 0; j < move.getList().size(); j++) stoneRow.add(ourNumber);

            boolean wallFront = false;
            boolean wallBack = false;
            for (Map.Entry<int[], int[]> entry : playerDirections.entrySet()) {
                if (timeLimited && timeToken.timeExceeded()) throw new TimeExceededException();

                int [] pos = entry.getKey();
                int[] direction = new int[2];
                direction[0] = entry.getValue()[0];
                direction[1] = entry.getValue()[1];
                stoneRow.add(ourNumber); // for every direction +1 Stone

                //Get positions and directions
                int xFront = move.getX();
                int yFront = move.getY();
                int [] dirFront = {direction[0] * (-1), direction[1] * (-1)};

                int xBack = pos[0];
                int yBack = pos[1];
                int [] dirBack = {direction[0], direction[1]};

                //build the line
                boolean tmpWallFront = buildLine(xFront, yFront, dirFront, board, stoneRow, true, ourPlayer, xBack, yBack);
                boolean tmpWallBack = buildLine(xBack, yBack, dirBack, board, stoneRow, false, ourPlayer,
                        xFront, yFront);
                if (tmpWallFront) wallFront = true;
                if (tmpWallBack) wallBack = true;
            }

            if (wallFront || wallBack) { // Can be added earlier.
                List<int[]> corners = mapAnalyzer.getInterestingCornerFieldList();
                boolean isCorner = false;
                for (int[] corner : corners) {
                    if (corner[0] == move.getX() && corner[1] == move.getY()) {
                        lineList.add(new Line(move, stoneRow), LineState.CORNER);
                        isCorner = true;
                        break;
                    }
                }

                if (!isCorner) lineList.add(new Line(move, stoneRow), LineState.EDGE);
                continue; // We will get corner/wall place -> safe
            }
            int lastInLines = stoneRow.size() - 1;
            if (stoneRow.get(0) == ourNumber && stoneRow.get(lastInLines) == ourNumber) {
                //We will control this line, we are first and last player
                lineList.add(new Line(move, stoneRow), LineState.CONTROL);
                continue;
            }

            if (stoneRow.get(0) == stoneRow.get(lastInLines)) {
                //We are trapped between the enemy but can safely move in between
                lineList.add(new Line(move, stoneRow), LineState.CAUGHT);
                continue;
            }

            //This move might be bad -> Opponent can cover us completely in his turn
            lineList.add(new Line(move, stoneRow), LineState.OPEN);
        }

        return lineList;
    }

    private boolean buildLine(int x, int y, int[] direction, Board board, List<Character> stoneRow,  boolean front, Player ourPlayer, int xEnd, int yEnd) {
        char [][] boardField = board.getField();
        int maxX  = board.getWidth() - 1;
        int maxY = board.getHeight() - 1;
        char stone = boardField[y][x];
        char tmp;
        if(front) stone = ourPlayer.getCharNumber();

        int xStart = x;
        int yStart = y;
        while ("-0bic".indexOf(stone) == -1) {
            int xNew = x + direction[0];
            int yNew = y + direction[1];

            Transition transition = board.getTransition(x, y, Direction.indexOf(direction));
            if (transition != null) {
                // Get new direction and new position
                direction[0] = Direction.valueOf(transition.getR())[0] * (-1);
                direction[1] = Direction.valueOf(transition.getR())[1] * (-1);
                xNew = transition.getX();
                yNew = transition.getY();
            }

            if (xNew > maxX || yNew > maxY || xNew < 0 || yNew < 0) {
                if (xStart != x && yStart != y) {
                    if (front) stoneRow.add(boardField[y][x]);
                    else stoneRow.add(0, boardField[y][x]);
                }

                if (stone == ourPlayer.getCharNumber()) return true; // We reached a wall
                return false; //Opponent is already at a wall
            }

            stone = boardField[yNew][xNew];
            if (stone == '-') { // Check for transition (Case 2)
                if (xStart != x && yStart != y) {
                    if (front) stoneRow.add(boardField[y][x]);
                    else stoneRow.add(0, boardField[y][x]);
                }

                if (stone == ourPlayer.getCharNumber()) return true; // We reached a wall
                return false; //Opponent is already at a wall
            }
            if ("-0bic".indexOf(stone) > -1) break;
            if (xNew == xEnd && yNew == yEnd) {
                if (!front) stoneRow.remove(stoneRow.size()-1); //Already added in front
                break; // Stop when reaching end of the row, don't bite you ass
            }
            if (front) stoneRow.add(stone); else stoneRow.add(0, stone);

            if (xStart == xNew && yStart == yNew) break; // avoid Loop

            x = xNew;
            y = yNew;
        }
        return false;
    }
}
