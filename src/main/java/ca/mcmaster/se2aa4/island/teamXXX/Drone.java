package ca.mcmaster.se2aa4.island.teamXXX;

public class Drone {
    private double battery_level;
    private Direction heading;

    //Constructor
    public Drone(double battery_level, Direction heading) {
        this.battery_level = battery_level;
        this.heading = heading;
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
    public void changeHeading (Direction newHeading) {
        heading = newHeading;
    }
}