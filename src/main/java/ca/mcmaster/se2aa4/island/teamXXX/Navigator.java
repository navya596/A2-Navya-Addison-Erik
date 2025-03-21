package ca.mcmaster.se2aa4.island.teamXXX;

public class Navigator {

    private Position curr_position;
    private Position target_position;
    private Direction curr_heading;

    public Navigator(Position curr_position, Position target_position, Direction curr_heading) {
        this.curr_position = curr_position;
        this.target_position = target_position;
        this.curr_heading = curr_heading;
    }


}