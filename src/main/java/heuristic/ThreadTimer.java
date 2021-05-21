package heuristic;

public class ThreadTimer implements Runnable {

    private Token token;

    public ThreadTimer (Token token) {
        this.token = token;
    }

    @Override
    public void run() {
//        long time = System.currentTimeMillis();
        while (!Thread.interrupted()) { }
        token.stop();

//        time = System.currentTimeMillis() - time;
//        System.out.println("Time: " + time);
//        time = 0;
    }
}
