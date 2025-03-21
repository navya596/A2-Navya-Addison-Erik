package ca.mcmaster.se2aa4.island.teamXXX;

import static ca.mcmaster.se2aa4.island.teamXXX.Direction.*;

public class Drone {
    private double battery_level;
    private Direction heading;
    private Position drone_position;

    //Constructor
    public Drone(double battery_level, Direction heading) {
        this.battery_level = battery_level;
        this.heading = heading;
        this.drone_position = new Position(1,1);
    }

    //Getter for battery level
    public double getBatteryLevel () {
        return battery_level;
    }

    //Getter for heading
    public Direction getHeading () {
        return heading;
    }

    //Setter for batter level
    public void setBatteryLevel (double battery) {
        this.battery_level = battery;
    }

    //Setter for heading
    public void setHeading (Direction head) {
        this.heading = head;
    }

    //Decrease battery level
    public void decreaseBattery (double amount) {
        if (battery_level - amount >= 0) {
            battery_level-=amount;

        } else {
            battery_level = 0; 
        }
        //we can return some sort of value here to indicate if battery gets full drained
    }

    //Change heading 
    public void changeHeading (Boolean isleftturn) {
        if (isleftturn) {
            if (heading == EAST) heading = NORTH;
            else if (heading == SOUTH) heading = EAST;
            else if (heading == WEST) heading = SOUTH;
            else if (heading == NORTH) heading = WEST;
        } else {
            if (heading == EAST) heading = SOUTH;
            else if (heading == SOUTH) heading = WEST;
            else if (heading == WEST) heading = NORTH;
            else if (heading == NORTH) heading = EAST;
        }
    }

    //primitive move forward
    public void move() {
        if (heading == EAST) {
            drone_position.setX(drone_position.x() + 1);
        } else if (heading == SOUTH) {
            drone_position.setY(drone_position.y() + 1);
        } else if (heading == WEST) {
            drone_position.setX(drone_position.x() - 1);
        } else if (heading == NORTH) {
            drone_position.setY(drone_position.y() - 1);
        }
    }
}