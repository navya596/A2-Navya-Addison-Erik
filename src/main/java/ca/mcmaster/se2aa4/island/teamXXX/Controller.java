package ca.mcmaster.se2aa4.island.teamXXX;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import ca.mcmaster.se2aa4.island.teamXXX.TileValue.*;

public class Controller {
    //attributes
    
    //Just added a logger to verify logic
    private final Logger logger = LogManager.getLogger();

    private Drone drone; 
    private String pastState; //need a past state verifying to get to an island
    private Navigator navigator; 
    private String[] currentState = new String[2];
    private Map<String, JSONObject> commands; //Keys will be used to return command as a JSON object back to acknowledge results
    private String front;
    private String left;
    private String right;
    private Integer cost;
    private String status;
    private JSONObject extraInfo;

    private Queue<JSONObject> decisionQ = new LinkedList<>();

    //Constructor
    public Controller(Drone drone) {
        this.drone = drone;
        commands = new HashMap<>();
        commands.put("fly", new JSONObject().put("action", "fly"));
        commands.put("stop", new JSONObject().put("action", "stop"));
        commands.put("scan", new JSONObject().put("action", "scan"));

        

    }

    //returns the information from the action that was selected and called in Explorer.takeDecision()
    public void resultOfDecision(Integer cost, String status, JSONObject extraInfo){
        this.cost = cost;
        this.status = status;
        this.extraInfo = extraInfo; 
    
    }

    //getCurrentstate returns String[] where first index is battery and second index is heading
    public String[] getCurrentState() {
        currentState[0] = String.valueOf(drone.getBatteryLevel());
        currentState[1] = drone.getHeading().toString();
        return currentState;
    }

    //getRespectiveDirections() gets drone's current heading and sets its respective front, left, and right directions
    public void getRespectiveDirections() {
        front = this.drone.getHeading();
        if ("E".equals(front)) {
            this.front = "E";
            this.left = "N";
            this.right = "S";
            
        } else if ("S".equals(front)) {
            this.front = "S";
            this.left = "E";
            this.right = "W";
        } else if ("W".equals(front)) {
            this.front = "W";
            this.left = "S";
            this.right = "N";
        } else if ("N".equals(front)) {
            this.front = "N";
            this.left = "W";
            this.right = "E";
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

    //Following methods below is just to find a ground tile
    //it uses a Queue to store the predetermined decisions to make
    public void findGroundDecisions(){

        getRespectiveDirections();
        //fly east 15 times
        for(int i = 0; i < 15; i++){
            decisionQ.add(commands.get("fly"));
        }

        logger.info("CHECKING HERE {}", right);

        //turn towards the south
        decisionQ.add(createCommand("heading", "right"));

        //echo south for a ground tile
        decisionQ.add(createCommand("echo", "right"));
    }

    public String executeFindGroundDecisions(){
        if (!decisionQ.isEmpty()) {
            //Check if the drone batterylevel is decreasing after each updateDrone() call
            logger.info("BATTERY LEVEL {}", drone.getBatteryLevel());

            JSONObject decision = decisionQ.remove();

            //once we get to the heading decision it needs to change the heading of the drone accordingly
            getRespectiveDirections();
            if((decision.get("action")).equals("heading")){
            
                String newDirection = decision.getJSONObject("parameters").getString("direction");
                drone.setHeading(newDirection);
            }

            //check if the direction was changed
            logger.info("New heading of the drone is {}", drone.getHeading());

            return decision.toString();
        }
        else { //means queue is empty, all steps have been performed
            //go back to explore class
            return "foundGround";
        }
    }



    public void goToGroundDecisions(JSONObject extraInfo) {
        if (extraInfo.has("range") && extraInfo.get("found").equals("GROUND")) {
            getRespectiveDirections();
            int range = (int) extraInfo.get("range");
            if (range != 0) {
                //enqueue fly to ground based on range
                for(int i = 0; i < range; i++){
                    decisionQ.add(commands.get("fly"));
                }

                logger.info("GOING TO GROUND IN RANGE: " + range);
                
                decisionQ.add(commands.get("scan"));
            }
            //********else if the range is 0, we might wanna call scan command 
            
        } else if (extraInfo.get("found").equals("OUT_OF_RANGE")) {
            //*********if out of range we might wanna echo left or right of current heading
        }
    } 

    public String goToGround() {
        logger.info("BATTERY LEVEL {}", drone.getBatteryLevel());
        JSONObject groundDecision = decisionQ.remove();
        getRespectiveDirections();
        return groundDecision.toString();
    }

    public TileValue analyzeScan() {
        if (extraInfo.has("biomes")) {
            ArrayList biomesFound = (ArrayList) extraInfo.get("biomes");
            if (!biomesFound.contains("OCEAN")) { //no ocean biome (ground)
                return TileValue.GROUND;
            } else if (biomesFound.size() > 1) { //both ocean and some other biome (coastline)
                return TileValue.COAST;
            } else {
                return TileValue.OCEAN;
            }
        } else {
            return TileValue.NODATA;
        }
    }

    public JSONObject traverseCoastDecision() {
        getRespectiveDirections();
        boolean scanDone = false;
        if (!scanDone) {
            scanDone = true;
            return commands.get("scan");
        } else {
            TileValue scanResult = analyzeScan();
            if (scanResult == TileValue.GROUND) {
                scanDone = false;
                return createCommand("heading", "left");
            } else if (scanResult == TileValue.OCEAN) {
                scanDone = false;
                return createCommand("heading", "right");
            } else {
                scanDone = false;
                return commands.get("fly");
            }
        }




    }

    public String traverseCoast(Position start) {
        boolean firstrun = true;
        if (firstrun) {
            return commands.get("fly").toString();
        } else if (drone.dronePosition == start) {
            return commands.get("stop").toString();
        } 
        else {
            JSONObject decision = traverseCoastDecision();
            return decision.toString();
        }


    }



    public void updateDrone(){
        //updates battery after a decision is made
        drone.decreaseBattery(cost);
    }
}