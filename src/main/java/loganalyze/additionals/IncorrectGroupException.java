package loganalyze.additionals;

public class IncorrectGroupException extends Exception {

    private final int number;

    public IncorrectGroupException(int number) {
        super("The selected group does not match the group of this log file!");
        this.number = number;
    }

    public int getIncorrectGroup() {
        return number;
    }
}
