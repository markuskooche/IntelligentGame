package heuristic;

import map.Board;
import map.Move;
import map.Player;
import mapanalyze.MapAnalyzer;
import timelimit.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MoveFilter {

    private MapAnalyzer mapAnalyzer;
    private Player[] players;
    private Board board;

    public MoveFilter(MapAnalyzer mapAnalyzer , Player[] players, Board board) {
        this.mapAnalyzer = mapAnalyzer;
        this.players = players;
        this.board = board;
    }

    public void filterMoves(LineList lineList, List<Move> moves, Player ourPlayer) {

        //Bonus
        List<Move> bonus = lineList.getBonusMoves();
        if (!bonus.isEmpty()) {
            moves.clear();
            for (Move m : bonus) {
                int value = m.getList().size(); //Kills
                m.setMoveValue(value);
            }
            bonus.sort((m1, m2) -> m2.compareToMoveValue(m1));
            moves.add(bonus.get(0));
            return;
        }

        //Choice
        List<Move> choice = lineList.getChoiceMoves();
        if (!choice.isEmpty()) {
            PlayerEvaluation evaluation = new PlayerEvaluation(mapAnalyzer, players);
            int bestPlayer = evaluation.getBestPlayer(ourPlayer.getIntNumber(), board);
            for (Move m : choice) m.setChoicePlayer(bestPlayer);
            moves.clear();
            moves.addAll(choice);
            return;
        }

        //Inversion
        List<Move> inversion = lineList.getInversionMoves();
        if (!inversion.isEmpty()) {
            PlayerEvaluation evaluation = new PlayerEvaluation(mapAnalyzer, players);
            boolean take = evaluation.takeInversion(ourPlayer, board);
            if(take) {
                moves.clear();
                moves.addAll(inversion);
                return;
            }
        }

        //Back up for moves
        List<Move> tmpMove = new ArrayList<>();
        tmpMove.addAll(moves);
        moves.clear();

        //Corner Moves
        List<Line> cornerLines = lineList.getCornerLines();
        if (!cornerLines.isEmpty()) {
            sortCornerLines(cornerLines);
            for (Line line : cornerLines) moves.add(line.getMove());
        }
        if (!moves.isEmpty()) return;

        //Edge Moves
        List<Line> edgeLines = lineList.getEdgeLines();
        if (!edgeLines.isEmpty()) {
            sortEdgeLines(edgeLines);
            for (Line line : edgeLines) moves.add(line.getMove());
        }

        //Good Moves
        List<Line> goodLines = lineList.getControlLines();
        goodLines.addAll(lineList.getCaughtLines()); // CaughtLines
        if (!goodLines.isEmpty()) {
            sortGoodLines(goodLines);
            for (Line line : goodLines) moves.add(line.getMove());
        }
        if (!moves.isEmpty()) return;

        //Bad Moves
        List<Line> badLines = lineList.getOpenLines();
        if (!badLines.isEmpty()) {
            sortBadLines(badLines);
            for (Line line : badLines) moves.add(line.getMove());
        }

        if (!moves.isEmpty()) return;

        //Everything is negativ
        moves.addAll(tmpMove);
    }

    private void sortEdgeLines(List<Line> edgeLines) {
        getBiggestMoveValue(edgeLines);
        removeUnderValue(edgeLines, 0);

        //Evaluate by summing map value
        int maxSum = Integer.MIN_VALUE;
        for(Line line : edgeLines) {
            Move move = line.getMove();
            List<int[]> positions = move.getList();
            int sum = move.getList().size(); // Kills;
            int[][] field = mapAnalyzer.getField();
            for(int[] pos : positions) {
                sum += field[pos[1]][pos[0]];
            }
            move.setMoveValue(sum);
            if (sum > maxSum)  maxSum = sum;
        }
    }

    private void sortBadLines(List<Line> badLines) {
        getBiggestMoveValue(badLines);
        removeUnderValue(badLines, 0);

        int [][] mapVal = mapAnalyzer.getField();
        int notSoBad = Integer.MIN_VALUE;
        for (Line line : badLines) {
            Move move = line.getMove();
            int value = mapVal[move.getY()][move.getX()];
            if (value >= 0) value = move.getList().size() * (-1);
            if (value > notSoBad) notSoBad = value;
            move.setMoveValue(value);
        }
        removeUnderValue(badLines, notSoBad);
    }

    private void sortCornerLines(List<Line> cornerLines) {
        int biggest = getBiggestMoveValue(cornerLines);
        removeUnderValue(cornerLines, biggest);
    }

    private void sortGoodLines(List<Line> goodLines) {
        //delete moves to negative fields
        getBiggestMoveValue(goodLines);
        removeUnderValue(goodLines, 0);

        //Evaluate by summing map value
        int maxSum = Integer.MIN_VALUE;
        for(Line line : goodLines) {
            Move move = line.getMove();
            List<int[]> positions = move.getList();
            int sum = move.getList().size(); // Kills
            int[][] field = mapAnalyzer.getField();
            for(int[] pos : positions) {
                sum += field[pos[1]][pos[0]];
            }
            move.setMoveValue(sum);
            if (sum > maxSum)  maxSum = sum;
        }
        //removeUnderValue(goodLines, maxSum);
        // Try: Analyze all moves
    }

    private int getBiggestMoveValue(List<Line> lines) {
        int [][] mapVal = mapAnalyzer.getField();
        int biggest = Integer.MIN_VALUE;
        for (Line line : lines) {
            Move move = line.getMove();
            int value = mapVal[move.getY()][move.getX()];
            if (value > biggest) biggest = value;
            move.setMoveValue(value);
        }
        return biggest;
    }

    private void removeUnderValue(List<Line> lines, int maxValue) {
        for (int i = 0; i < lines.size(); i++) {
            Line line = lines.get(i);
            Move move = line.getMove();
            if (move.getMoveValue() < maxValue) {
                lines.remove(i);
                i--;
            }
        }
    }
}
