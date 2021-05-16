package heuristic;

public class Token {
    private boolean noTime = false;

    void stop() {
        noTime = true;
    }

    boolean timeExceeded() {
        return  noTime;
    }
}
