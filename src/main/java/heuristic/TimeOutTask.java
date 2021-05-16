package heuristic;

import java.util.Timer;
import java.util.TimerTask;

public class TimeOutTask extends TimerTask {

    private Thread t;
    private Timer timer;

    public TimeOutTask(Thread t, Timer timer) {
        this.t = t;
        this.timer = timer;
    }

    @Override
    public void run() {
        if (t != null && t.isAlive()) {
            t.interrupt();
            timer.cancel();
        }
    }
}
