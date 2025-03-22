package ca.mcmaster.se2aa4.island.teamXXX;

public class Coordinate {
    private final int x;
    private final int y;

    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Coordinate(Position p) {
        this.x = p.x();
        this.y = p.y();
    }   

    // Public getters
    public int x() {
        return x;
    }

    public int y() {
        return y;
    }
}