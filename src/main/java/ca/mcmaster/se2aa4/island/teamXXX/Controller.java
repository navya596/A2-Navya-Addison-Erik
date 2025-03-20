package ca.mcmaster.se2aa4.island.teamXXX;

import org.json.JSONObject;

import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class Controller {
    //attributes
    private Drone drone; 
    private String pastState; //need a past state verifying to get to an island
    private Navigator navigator; 
    private String[] currentState = new String[2];
    private JSONObject decision = new JSONObject();
    private Map<String, JSONObject> commands; //Keys will be used to return command as a JSON object back to acknowledge results
    private String front;
    private String left;
    private String right;

    //Constructor
    public Controller(Drone drone) {
        this.drone = drone;
        commands = new HashMap<>();
        commands.put("fly", new JSONObject().put("action", "fly"));
        commands.put("stop", new JSONObject().put("action", "stop"));
        commands.put("scan", new JSONObject().put("action", "scan"));

        

    }

    //getCurrentstate returns String[] where first index is battery and second index is heading
    public String[] getCurrentState() {
        currentState[0] = String.valueOf(drone.getBatteryLevel());
        currentState[1] = drone.getHeading().toString();
        return currentState;
    }

    //getRespectiveDirections() gets drone's current heading and sets its respective front, left, and right directions
    public void getRespectiveDirections() {
        front = drone.getHeading().toString();
        if ("EAST".equals(front)) {
            front = "E";
            left = "N";
            right = "S";
            
        } else if ("SOUTH".equals(front)) {
            front = "S";
            left = "E";
            right = "W";
        } else if ("WEST".equals(front)) {
            front = "W";
            left = "S";
            right = "N";
        } else if ("NORTH".equals(front)) {
            front = "N";
            left = "W";
            right = "E";
        }
    }

    /*createCommand() takes the action as a string parameter and
    dynamically creates the respective heading or echo command based on 
    current respective directions */
    private JSONObject createCommand(String action, String directionType) {
        //get respective direction
        getRespectiveDirections();
        //create put action in json ojectt
        JSONObject command = new JSONObject();
        command.put("action", action);

        //create parameters object based on action type
        JSONObject parameters = new JSONObject();

        //Select appropriate direction based on passed directionType
        if (front.equals(directionType)) {
            parameters.put("direction", front);
        }
        else if (left.equals(directionType)) {
            parameters.put("direction", left);
        }
        else if (right.equals(directionType)) {
            parameters.put("direction", right);
        }

        command.put("parameters", parameters);

        return command;

    }

    
    //I lowkey dont like this way of identifying previous commands because it uses a lot of if statements
    //Let me know if you guys have a better way of going around this logic
    public JSONObject getDecision(JSONObject previousDecision, JSONObject previousResult){
        pastState = (String) previousDecision.get("action");
        //decrease battery based on previousResult

        if (pastState == null) { 
            return previousNull();
        }
        else if (pastState == "heading") {
            return previousHeading();
        }
        else if (pastState == "stop") {
            return previousStop(); //idk if we will ever use this
        }
        else if (pastState ==  "fly") { 
            return previousFly();
        }
        else if (pastState == "scan") {
            return previousScan();
        }
        else if (pastState == "echo") { //previousResult is only needed for this to get the range of the ground cell
            return previousEcho(previousDecision, previousResult);
        }

    } 

    //REMEMBER TO DECREASE BATTERY ACCORDINGLY

    private JSONObject previousNull() {

         //if previous was null -> means initial state
            //echo in facing direction
            
    }

    private JSONObject previousHeading() {
        //if previous was heading 
            //fly
    }
    
    private JSONObject previousFly() {
        //if previous was fly
            //echo front direction
    }

    private JSONObject previousScan() {
        //if previous was scan
            //if creek found call navigator
    }

    private JSONObject previousEcho(JSONObject previousAction, JSONObject previousResult) {
        
        //if previous was echo (echo always checks front, left, then right for our convention)
            //check where drone is facing right now
            //check which direction echo was in and if anything was found
            //If previous direction is current direction
                //fly
            //turn in that direction if something was found
            //echo in another direction if it wasnt
            //check if range was 0 and a ground cell was found
                //call scan
    }


    
}