package loganalyze.additional;

/**
 * An IncorrectGroupException should be thrown, if a group chooses an incorrect logfile.
 *
 * @author Benedikt Halbritter
 * @author Iwan Eckert
 * @author Markus Koch
 */
public class IncorrectGroupException extends Exception {

    private final int number;

    /**
     * Create a new IncorrectGroupException.
     *
     * @param number set the number of the incorrect group
     */
    public IncorrectGroupException(int number) {
        super("The selected group does not match the group of this log file!");
        this.number = number;
    }

    /**
     * Get the group number of the incorrect group.
     */
    public int getIncorrectGroup() {
        return number;
    }
}
