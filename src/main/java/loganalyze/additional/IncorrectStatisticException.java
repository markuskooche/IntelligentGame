package loganalyze.additional;

public class IncorrectStatisticException extends Exception {

    private final String statisticName;

    public IncorrectStatisticException(String statisticName) {
        super("The selected group does not match the group of this log file!");
        this.statisticName = statisticName;
    }

    public String getStatisticName() {
        return statisticName;
    }
}
