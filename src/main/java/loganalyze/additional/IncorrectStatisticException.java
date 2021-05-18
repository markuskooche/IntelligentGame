package loganalyze.additional;

/**
 * An IncorrectStatisticException should be thrown, if a statistic could not compared two different statistic formats.
 *
 * @author Benedikt Halbritter
 * @author Iwan Eckert
 * @author Markus Koch
 */
public class IncorrectStatisticException extends Exception {

    private final String statisticName;

    /**
     * Create a new IncorrectStatisticException.
     *
     * @param statisticName the name of the statistic which would be compared
     */
    public IncorrectStatisticException(String statisticName) {
        super("The selected group does not match the group of this log file!");
        this.statisticName = statisticName;
    }

    /**
     * Get the name of the statistic which could not be compared.
     *
     * @return the name of the statistic
     */
    public String getStatisticName() {
        return statisticName;
    }
}
