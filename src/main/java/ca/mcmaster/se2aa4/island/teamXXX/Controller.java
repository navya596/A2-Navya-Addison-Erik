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
    private String[] currentState;
    private JSONObject decision = new JSONObject();
    private Map<String, JSONObject> commands; //Keys will be used to return command as a JSON object back to acknowledge results

    //Constructor
    public Controller(Drone drone) {
        this.drone = drone;
        commands = new HashMap<>();
        commands.put("fly", new JSONObject().put("action", "fly"));
        commands.put("heading", new JSONObject().put("action", "heading"));
        commands.put("echo", new JSONObject().put("action", "echo"));
        commands.put("stop", new JSONObject().put("action", "stop"));
        commands.put("scan", new JSONObject().put("action", "scan"));

    }

    //getCurrentstate returns String[] where first index is battery and second index is heading
    public String[] getCurrentState() {
        currentState[0] = drone.getBatteryLevel().toString();
        currentState[1] = drone.getHeading().toString();
        return currentState;
    }
    
    //I lowkey dont like this way of identifying previous commands because it uses a lot of if statements
    //Let me know if you guys have a better way of going around this logic
    public JSONObject getDecision(JSONObject previousDecision, JSONObject previousResult){
        pastState = previousDecision.get("action");
        if (pastState == null) { 
            return previousNull();
        }
        else if (previousDecision == "heading") {
            return previousHeading();
        }
        else if (previousDecision == "stop") {
            return previousStop(); //idk if we will ever use this
        }
        else if (previousDecision ==  "fly") { 
            return previousFly();
        }
        else if (previousDecision == "scan") {
            return previousScan();
        }
        else if (previousDecision == "echo") { //previousResult is only needed for this to get the range of the ground cell
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
            //turn in that direction if something was found
            //echo in another direction if it wasnt
            //check if range was 0 and a ground cell was found
                //call scan
    }


    
}