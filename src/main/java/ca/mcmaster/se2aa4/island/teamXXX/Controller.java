package ca.mcmaster.se2aa4.island.teamXXX;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.json.JSONTokener;

public class Controller {
    //attributes
    private Drone drone; 
    private String pastState; //need a past state verifying to get to an island
    private Navigator navigator; 
    private JSONObject decision = new JSONObject();
    private Map<String, JSONObject> commands; //Keys will be used to return command as a JSON object back to acknowledge results
    private String front;
    private String left;
    private String right;
    private final Logger logger = LogManager.getLogger();

    //Constructor
    public Controller(Drone drone) {
        this.drone = drone;
        commands = new HashMap<>();
        commands.put("fly", new JSONObject().put("action", "fly"));
        commands.put("stop", new JSONObject().put("action", "stop"));
        commands.put("scan", new JSONObject().put("action", "scan"));

        

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
                if (Integer.parseInt(parameters.getString("range")) <= 2 ) { //if range of previous echo direction was <= 2 ie close enough 
                    return commands.get("stop"); //stop and return to base (only for the mvp) 
                }
                else { //if range of previous echo direction was not <= 2 ie its not close enough (this is just an estimation) in previousResult
                    return commands.get("fly");
                }                    
                
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

    //getResults() called by acknkowledgeResults, returns Json object thats related to cost status and everything
    //gets the cost and status and extra info
    //decreases battery accordingly, MIA throw error if battery too low
    //deal with incorrect commands
    //spits back action and result json objects
    //explorer can then set previousresult as those json objects
    public JSONObject getResult(String s) {
        JSONObject response = new JSONObject(new JSONTokener(new StringReader(s)));
        logger.info("** Response received:\n"+response.toString(2));
        Integer cost = response.getInt("cost");
        logger.info("The cost of the action was {}", cost);
        String status = response.getString("status");
        logger.info("The status of the drone is {}", status);
        JSONObject extraInfo = response.getJSONObject("extras");
        logger.info("Additional information received: {}", extraInfo);

        try {
            drone.decreaseBattery(cost);
            if (drone.getBatteryLevel() <= 0) {
                throw new ArithmeticException();
            }
        } catch (ArithmeticException e) {
            logger.info("Battery too low, terminating program");
        }

        return response;

    }

    
}