package ca.mcmaster.se2aa4.island.teamXXX;

public class Drone {
    private Integer battery_level;
    private String heading;
    protected Position dronePosition;

    //Constructor
    public Drone(Integer battery_level, String heading) {
        this.battery_level = battery_level;
        this.heading = heading;
        this.dronePosition = new Position(1,1);
    }

    //Getter for battery level
    public Integer getBatteryLevel () {
        return battery_level;
    }

    //Getter for heading
    public String getHeading () {
        return heading;
    }

    public Position getPosition() {
        return dronePosition;
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
        battery_level -= amount;
        //we can return some sort of value here to indicate if battery gets full drained
    }

    //Change heading 
    public void changeHeading (Boolean isleftturn) {
        if (isleftturn) {
            if (heading == "E") heading = "N";
            else if (heading == "S") heading = "E";
            else if (heading == "W") heading = "S";
            else if (heading == "N") heading = "W";
        } else {
            if (heading == "E") heading = "S";
            else if (heading == "S") heading = "W";
            else if (heading == "W") heading = "N";
            else if (heading == "N") heading = "E";
        }
    }
}