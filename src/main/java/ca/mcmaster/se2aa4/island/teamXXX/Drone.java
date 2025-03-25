package ca.mcmaster.se2aa4.island.teamXXX;


public class Drone {
    private Integer battery_level;
    private String heading;

    //Constructor
    public Drone(Integer battery_level, String heading) {
        this.battery_level = battery_level;
        this.heading = heading;
    }

    //Getter for battery level
    public Integer getBatteryLevel () {
        return battery_level;
    }

    //Getter for heading
    public String getHeading () {
        return heading;
    }

    //Setter for batter level
    public void setBatteryLevel (Integer battery) {
        this.battery_level = battery;
    }

    //Setter for heading
    public void setHeading (String head) {
        this.heading = head;
    }

    //Decrease battery level
    public void decreaseBattery (Integer amount) {
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
            if (heading.equals("E")) heading = "N";
            else if (heading.equals("S")) heading = "E";
            else if (heading.equals("W")) heading = "S";
            else if (heading.equals("N")) heading = "W";
        } else {
            if (heading.equals("E")) heading = "S";
            else if (heading.equals("S")) heading = "W";
            else if (heading.equals("W")) heading = "N";
            else if (heading.equals("N")) heading = "E";
        }
    }

    

    //primitive move forward
    public void move() {
        // if (heading == Direction.EAST) {
        //     x_coord++;
        // } else if (heading == Direction.SOUTH) {
        //     y_coord++;
        // } else if (heading == Direction.WEST) {
        //     x_coord--;
        // } else if (heading == Direction.NORTH) {
        //     y_coord--;
        // }
    }
}