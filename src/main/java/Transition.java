public class Transition {

    private int x1;
    private int y1;
    private int r1;
    private int x2;
    private int y2;
    private int r2;

    public Transition(int x1, int y1, int r1, int x2, int y2, int r2) {
        this.x1 = x1;
        this.y1 = y1;
        this.r1 = r1;
        this.x2 = x2;
        this.y2 = y2;
        this.r2 = r2;
    }

    public int getX1() {
        return x1;
    }

    public int getY1() {
        return y1;
    }

    public int getR1() {
        return r1;
    }

    public int getX2() {
        return x2;
    }

    public int getY2() {
        return y2;
    }

    public int getR2() {
        return r2;
    }
}
