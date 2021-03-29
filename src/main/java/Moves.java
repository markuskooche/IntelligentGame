import java.util.ArrayList;
import java.util.List;

public class Moves {

    private List<int[]> list;
    private boolean inversion;
    private boolean choice;
    private boolean bonus;

    public Moves() {
        list = new ArrayList<>();
        inversion = false;
        choice = false;
    }

    public int getX() {
        return list.get(list.size()-1)[0];
    }

    public int getY() {
        return list.get(list.size()-1)[1];
    }

    public void add(int[] element) {
        list.add(element);
    }

    public List<int[]> getList() {
        return list;
    }

    public void setBonus() {
        bonus = true;
    }

    public boolean getBonus() {
        return bonus;
    }

    public void setChoice() {
        choice = true;
    }

    public boolean getChoice() {
        return choice;
    }

    public void setInversion() {
        this.inversion = true;
    }

    public boolean getInversion() {
        return inversion;
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public int size() {
        return list.size();
    }

    public boolean isMove(int[] move) {
        return move[0] == getX() && move[1] == getY();
    }

    public String toString() {
        StringBuilder moveString = new StringBuilder();

        if (list.size() != 0) {
            moveString.append("(" + getX() + ", " + getY() + ")");
            moveString.append(" -> [");

            for (int i = 0; i < list.size(); i++) {
                moveString.append("(" + list.get(i)[0] + ", " + list.get(i)[1] + ")");
            }
            moveString.append("]");
        } else {
            moveString.append("EMPTY");
        }

        return moveString.toString();
    }
}
