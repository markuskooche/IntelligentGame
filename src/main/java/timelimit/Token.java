package timelimit;

public class Token {
    private boolean noTime = false;

    public void start() {
        noTime = false;
    }

    public void stop() {
        noTime = true;
    }

    public boolean timeExceeded() {
        return  noTime;
    }
}
