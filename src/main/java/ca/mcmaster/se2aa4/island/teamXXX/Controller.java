package ca.mcmaster.se2aa4.island.teamXXX;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

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
        if ("front".equals(directionType)) {
            parameters.put("direction", front);
        }
        else if ("left".equals(directionType)) {
            parameters.put("direction", left);
        }
        else if ("right".equals(directionType)) {
            parameters.put("direction", right);
        }

        command.put("parameters", parameters);

        return command;

    }

    
    //I lowkey dont like this way of identifying previous commands because it uses a lot of if statements
    //Let me know if you guys have a better way of going around this logic
    public JSONObject getDecision(JSONObject previousDecision, JSONObject previousResult){
        pastState = (String) previousDecision.get("action");
        // Extract "extras" from previousResult if available
        JSONObject extras = null;
        if (previousResult != null && previousResult.has("extras")) {
            extras = previousResult.getJSONObject("extras");
        }

        //decrease battery based on previousResult

        if (pastState == null) { 
            return previousNull();
        }
        else if (pastState == "heading") {
            return previousHeading();
        }
        else if (pastState == "stop") {
            //return previousStop(); //idk if we will ever use this
        }
        else if (pastState ==  "fly") { 
            return previousFly();
        }
        else if (pastState == "scan") {
            //return previousScan(previousResult);
        }
        else if (pastState == "echo") { //previousResult is only needed for this to get the range of the ground cell
            return previousEcho(previousDecision, previousResult, extras);
        }
        return null;
    } 

    //REMEMBER TO DECREASE BATTERY ACCORDINGLY

    //if previous was null -> means initial state
    private JSONObject previousNull() {
        return createCommand("echo", "front");
            
    }

    private JSONObject previousHeading() {
        return commands.get("fly");
    }
    
    private JSONObject previousFly() {
        return createCommand("echo", "front");
    }

    /*private JSONObject previousScan(JSONObject previousResult) { //need to modify this later
        
    }*/

    private JSONObject previousEcho(JSONObject previousAction, JSONObject previousResult, JSONObject extras) {
        JSONObject parameters = previousAction.getJSONObject("parameters");
        String previousDirection = parameters.getString("direction");
        //if ground was found in extras
        if ("GROUND".equals(extras.getString("found"))) {
            
            // Update current respective directions
            getRespectiveDirections(); 

            //if current direction is same as previous echo direction in previousAction
            if (drone.getHeading().equals(parameters.getString("direction")) ) {
                //if range of previous echo direction was not <= 2 ie its not close enough (this is just an estimation) in previousResult
                    //fly
                //if range of previous echo direction was <= 2 ie close enough 
                    //scan ground 
                return commands.get("fly");
            }
            else { //change heading to that direction 
                // Determine if previous echo was done on respective left or right
                if (previousDirection.equals(left)) {
                    drone.changeHeading(true);
                    return createCommand("heading", "left");
                } else if (previousDirection.equals(right)) {
                    drone.changeHeading(false);
                    return createCommand("heading", "right");
                    
                }
    
            }
        } else if ("OUT_OF_RANGE".equals(extras.getString("found"))) {
            //echo in next direction (front -> left -> right by convention)
            // Determine if previous echo was done on respective left, right, or front
            if (previousDirection.equals(left)) {
                return createCommand("echo", "right");
            } else if (previousDirection.equals(front)) {
                return createCommand("echo", "left");
            }
            else if (previousDirection.equals(right)) {
                drone.changeHeading(false); //try changing heading if all echo directions were out of range
                return createCommand("heading", "right");

            }
        } 
            
        return null;
            
        
        



    }


    
}