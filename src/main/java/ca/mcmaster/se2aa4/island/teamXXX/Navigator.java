package ca.mcmaster.se2aa4.island.teamXXX;

import java.util.*;

public class Navigator {

    private Position curr_position;
    private Position target_position;
    private Direction curr_heading;

    public Navigator(Position curr_position, Position target_position, Direction curr_heading) {
        this.curr_position = curr_position;
        this.target_position = target_position;
        this.curr_heading = curr_heading;
    }

    public void setCurrPosition(Position p) {
        curr_position = p;
    }

    public void SetTargetPosition(Position p) {
        target_position = p;
    }

    public void setHeading(Direction heading) {
        curr_heading = heading;
    }

    private int pathDistance(Position start, Position end) {
        return Math.abs(start.x() - end.x()) + Math.abs(start.y() - end.y());
    }

    




}