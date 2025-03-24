package ca.mcmaster.se2aa4.island.teamXXX;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class Controller {
    //attributes
    
    //Just added a logger to verify logic
    private final Logger logger = LogManager.getLogger();

    protected Drone drone; 
    private String pastState; //need a past state verifying to get to an island
    private Navigator navigator; 
    private String[] currentState = new String[2];
    protected Map<String, JSONObject> commands; //Keys will be used to return command as a JSON object back to acknowledge results
    private String front;
    private String left;
    private String right;
    private Integer cost;
    private String status;
    private JSONObject extraInfo;
    private int runs;
    private String creek;
    private JSONObject backLogEcho;
    private JSONObject backLogStop;
    private boolean goToGroundStarted = false;
    private boolean adjustingAtGround = false;

    //changed backLogAction into a Queue so that we can store multiple actions at once
    private Queue<JSONObject> backLogAction = new LinkedList<>();
    private Queue<JSONObject> decisionQ = new LinkedList<>();

    //Constructor
    public Controller(Drone drone) {
        this.drone = drone;
        this.runs = 0;
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
    protected JSONObject createCommand(String action, String directionType) {
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

            logger.info("DIRECTION OF FRONT");
            logger.info(front);
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

    /*public void getPastEcho(String previousDecision){

        JSONObject jsonObject = new JSONObject(previousDecision);
        String pastState = jsonObject.getString("action");

        if ("echo".equals(pastState)) { 
            JSONObject parameters = jsonObject.getJSONObject("parameters");
            String previousDirection = parameters.getString("direction");            
            // Update current respective directions
            getRespectiveDirections(); 
            
            //If ground was found for the previous echo command
            if ("GROUND".equals(extraInfo.getString("found"))) {
                //if current direction's left is same as previous echo direction (want to make sure ground is always on the left)
                if (left.equals(parameters.getString("direction")) ) {
                    if (extraInfo.has("range") && extraInfo.get("range") instanceof Integer) {
                        if (extraInfo.getInt("range") == 0) {
                            decisionQ.add(commands.get("fly"));
                        }
                    }
                    else { //go to ground if range is not close enough
                        goToGroundDecisions();
                    }   
                } else if (front.equals(parameters.getString("direction")) ) { 

                    if (extraInfo.has("range") && extraInfo.get("range") instanceof Integer) {
                        if (extraInfo.getInt("range") == 0) {
                            decisionQ.add(createCommand("heading", "right"));
                        }
                    }
                    else { //go to ground if range is not close enough
                        goToGroundDecisions();
                    } 
                } else if (right.equals(parameters.getString("direction")) ) {
                    if (extraInfo.has("range") && extraInfo.get("range") instanceof Integer) {
                        if (extraInfo.getInt("range") == 0) {
                            decisionQ.add(createCommand("heading", "left"));
                        }
                    }
                    else { //go to ground if range is not close enough
                        goToGroundDecisions();
                    } 
    
                }
            } else if ("OUT_OF_RANGE".equals(extraInfo.getString("found"))) {
                //echo in next direction (front -> left -> right by convention)
                // Determine if previous echo was done on respective left, right, or front
                if (previousDirection.equals(left)) {
                    decisionQ.add(createCommand("echo", "right"));
                } else if (previousDirection.equals(front)) {
                    decisionQ.add(createCommand("echo", "left"));
                }
                else if (previousDirection.equals(right)) {
                    drone.changeHeading(false); //try changing heading if all echo directions were out of range
                    decisionQ.add(createCommand("heading", "left"));
    
                }
            }
        }                   
                
        
            
            
    } */

    //Following methods below is just to find a ground tile
    //it uses a Queue to store the predetermined decisions to make
    public boolean findGroundDecisions(){
        getRespectiveDirections();
        //If past result was echo, and ground was found, return true to explorer
        if (extraInfo != null && extraInfo.has("range") && extraInfo.has("found") && extraInfo.get("found").equals("GROUND")) {
            logger.info("ADDED HEADING HERE");
            decisionQ.add(createCommand("heading", "right"));
            //decisionQ.add(createCommand("echo", "front"));
            return true;
        }
        //Else fly twice and echo right to look for ground
        else {
            
            //fly east 2 times
            for(int i = 0; i < 2; i++){
                decisionQ.add(commands.get("fly"));
            }

            logger.info("CHECKING HERE {}", right);

            //echo south for a ground tile
            decisionQ.add(createCommand("echo", "right"));
            return false; //return false as ground was not found
        }
    }

    //empties queue for ground decisions
    public JSONObject executeFindGroundDecisions(){
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

            return decision;
        }
        
        return null;
    }



    //empties queue for ground decisions
    public JSONObject executeGoToGroundDecisions(){
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

            return decision;
        }
        else { //means queue is empty, all steps have been performed
            //go back to explore class
            

            return null;
        }
        
    }

    public void analyzeEcho() {
        
        int range = extraInfo.getInt("range");
        String found = (String) extraInfo.get("found");

        if (found.equals("GROUND") && range != 0 && analyzeScan() != TileValue.GROUND) { // no ocean means only ground
            
            backLogStop = commands.get("stop");
        } 
            
        
    } 

    public TileValue analyzeScan() {
        if (extraInfo.has("biomes")) {
            JSONArray biomesFound = extraInfo.getJSONArray("biomes");
            boolean hasOcean = false;
            for (int i = 0; i < biomesFound.length(); i++) {
                String biome = biomesFound.getString(i).toUpperCase();
                if (biome.contains("OCEAN")) {
                    hasOcean = true;
                    break;
                }
            }
            if (!hasOcean) { // no ocean means only ground
                return TileValue.GROUND;
            } else if (biomesFound.length() > 1) { // if more than one biome, we assume coastline
                return TileValue.COAST;
            } else { // otherwise, only ocean is present
                return TileValue.OCEAN;
            }
        } else {
            return TileValue.NODATA;
        }
    }


    public void findCreek(){
        if(extraInfo != null && extraInfo.has("creeks")){
            JSONArray creekKey = extraInfo.getJSONArray("creeks");
            logger.info(creekKey.toString());
            this.creek = creekKey.toString();
        }
    }

    public boolean wasEchoCalled(){
        if(extraInfo.has("range") && extraInfo.has("found")){
            return true;
        } else{
            return false;
        }
    }
    private boolean isXMappingStarted = false;
    private boolean isXMappingDone = false;
    private boolean isYMappingStarted = false;
    private boolean isYMappingDone = false;
    private boolean isInitialCorrectionDone = false;
    private int x_len = 0;
    private int y_len = 0;


    public boolean bruteForceDecision(){
        logger.info("CHECK BEFORE ANY LOGIC");
        logger.info(drone.getHeading());
        //if(backLogAction != null){
        if(!decisionQ.isEmpty()){
            return false;
            //enqueues the action from the backLog to the decision we want to happen next
            //decisionQ.add(backLogAction.remove());
        } 

        if (!isXMappingStarted) {
            if (extraInfo.get("found").equals("GROUND")) {
                isXMappingStarted = true;
            } else {
                decisionQ.add(commands.get("fly"));
                decisionQ.add(createCommand("echo", "right"));
            }
            return false;
        } else if (!isXMappingDone) {
            
            if (extraInfo.get("found").equals("OUT_OF_RANGE")) {
                decisionQ.add(createCommand("heading", "right"));
                drone.changeHeading(false);
                getRespectiveDirections();
                decisionQ.add(createCommand("echo", "right"));
                isXMappingDone = true;
            }  else {
                decisionQ.add(commands.get("fly"));
                decisionQ.add(createCommand("echo", "right"));
                x_len++;
            }
            return false;
        } else if (!isYMappingStarted) {
            if (extraInfo.get("found").equals("GROUND")) {
                isYMappingStarted = true;
            }   else {
                decisionQ.add(commands.get("fly"));
                decisionQ.add(createCommand("echo", "right"));
            }
            return false;
        } else if (!isYMappingDone) {
            if (extraInfo.get("found").equals("OUT_OF_RANGE")) {
                isYMappingDone = true;
            }  else {
                decisionQ.add(commands.get("fly"));
                decisionQ.add(createCommand("echo", "right"));
                y_len++;
            }
            return false;
        } else if (!isInitialCorrectionDone) {
            decisionQ.add(createCommand("heading", "right"));
            drone.changeHeading(false);
            getRespectiveDirections();
            decisionQ.add(createCommand("heading", "right"));
            drone.changeHeading(false);
            getRespectiveDirections();
            decisionQ.add(commands.get("fly"));
            decisionQ.add(commands.get("scan"));
            
            //corrects the x and y lengths of island (algorithm counts one extra time)
            x_len--;
            y_len--;
            isInitialCorrectionDone = true;
            return false;
        } else {
            //decisionQ.add(commands.get("stop"));
            return true;

        }
    }
    public void goToGroundDecisions() {
        
        
        if (y_len != 0) {
            for(int i = 0; i < y_len/2; i++){
                decisionQ.add(commands.get("fly"));
            }
        
            logger.info("GOING TO GROUND IN RANGE: " + y_len);
        
            decisionQ.add(commands.get("scan"));
            
            

            
            goToGroundStarted = true;
        }
        
        //decisionQ.add(commands.get("scan"));
            
        //decisionQ.add(commands.get("stop"));
        
        
            
        
    
        
        
    }

    public void zigZagAlgorithm() {
        //addisons code here

        
    }
    
    public JSONObject bruteForceDecisionResult(){
        if (decisionQ.isEmpty()) {
            bruteForceDecision();
        } 
        else if (goToGroundStarted && extraInfo != null) {
            logger.info("BIOME: " + (analyzeScan()).toString());
            if (analyzeScan() == TileValue.OCEAN ) {
                decisionQ.add(createCommand("heading", "left"));
                drone.changeHeading(true);
                getRespectiveDirections();
                adjustingAtGround = true;
            }
            else {
                
                decisionQ.add(commands.get("scan"));
                decisionQ.add(commands.get("stop"));

            } 
            
            

        }
        logger.info(decisionQ.peek());
        return decisionQ.remove();
    }

    public void updateDrone(){
        //updates battery after a decision is made
        drone.decreaseBattery(cost);
    }
}