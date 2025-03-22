package ca.mcmaster.se2aa4.island.teamXXX;

public class Position {
    private int x;
    private int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Position(Position p) {
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

    public void setX(int newX) {
        x = newX;
    }

    public void setY(int newY) {
        y = newY;
    }

    public void setCoords(int newX, int newY) {
        x = newX;
        y = newY;
    }
}