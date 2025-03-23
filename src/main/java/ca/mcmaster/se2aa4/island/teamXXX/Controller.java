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

    public void getPastEcho(JSONObject previousDecision){
        pastState = (String) previousDecision.get("action");

        if ("echo".equals(pastState)) { 
            JSONObject parameters = previousDecision.getJSONObject("parameters");
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
        decisionQ.add(commands.get("scan"));

        //turn towards the south
        decisionQ.add(createCommand("heading", "right"));
        decisionQ.add(commands.get("scan"));

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
            

            return "queue empty";
        }
        
    }

    public void goToGroundDecisions() {

        //add a null check for extra info before accessing it 
        //be consistent with using opt safely to access json values and prevent exceptions
        //add null check for creeks array???
        //logic for out of range??
        if (extraInfo.has("range") && extraInfo.has("found") && extraInfo.get("found").equals("GROUND")) {
            getRespectiveDirections();
            int range = (int) extraInfo.get("range");
            if (range != 0) {
                //enqueue fly to ground based on range
                for(int i = 0; i < range; i++){
                    decisionQ.add(commands.get("fly"));
                }

                logger.info("GOING TO GROUND IN RANGE: " + range);

                
                
                decisionQ.add(commands.get("scan"));
                decisionQ.add(createCommand("heading", "right"));
                decisionQ.add(commands.get("scan"));
            }
            else {
                decisionQ.add(createCommand("heading", "left"));
                decisionQ.add(commands.get("scan"));
            }
            
            
            
        } else if (extraInfo.has("range") && extraInfo.has("found") && extraInfo.get("found").equals("OUT_OF_RANGE")) {
            //*********if out of range we might wanna echo left or right of current heading

            //echo left
            //echo right
        }
        else if (extraInfo != null && extraInfo.has("biomes") && extraInfo.has("creeks")) { //else if extra info has a key called biomes and creeks 
            Object creeksObj = extraInfo.opt("creeks");  // Use opt() to avoid exceptions
            Object biomesObj = extraInfo.opt("biomes");

            JSONArray creeksArray = null;
            JSONArray biomesArray = null;

            if (creeksObj instanceof JSONArray) {
                creeksArray = (JSONArray) creeksObj;
            }
            if (biomesObj instanceof JSONArray) {
                biomesArray = (JSONArray) biomesObj;
            }

            
            if (biomesArray != null) {  

                // Check if biomes contains "OCEAN"
                boolean containsOcean = false;
                for (int i = 0; i < biomesArray.length(); i++) {  
                    if ("OCEAN".equals(biomesArray.getString(i))) {  
                        containsOcean = true;  
                        break;  
                    }  
                }
        
                if (creeksArray.length() > 0) {  
                    decisionQ.add(commands.get("stop"));  
                }  
                else if (biomesArray.length() == 1 && containsOcean) {  
                    decisionQ.add(createCommand("echo", "left"));   //handle out of range in above if state ment
                }  
                else if (biomesArray.length() != 1 && containsOcean) {  
                    decisionQ.add(commands.get("fly"));  
                    decisionQ.add(commands.get("scan"));
                    
                    
                }  
                else {  
                    decisionQ.add(createCommand("heading", "right"));  
                    decisionQ.add(commands.get("fly"));  
                    decisionQ.add(commands.get("scan"));

                    
                } 

                
                
            }  

            //decisionQ.add(commands.get("scan"));
            //decisionQ.add(commands.get("stop"));
        }

    }

    public String goToGround() {
        if (extraInfo != null) {
            logger.info("Extra info: " + extraInfo);
        } else {
            logger.info("Extra info is null");
        }
        
        if (!decisionQ.isEmpty()) {
            logger.info("BATTERY LEVEL {}", drone.getBatteryLevel());
            JSONObject groundDecision = decisionQ.remove();
            getRespectiveDirections();
            return groundDecision.toString();
        }
        else {     
            return "reachedGround";
        }
    }

    public void patrolGroundDecisions() {
        


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

    public void traverseCoastDecision() {
        TileValue scanResult = analyzeScan();
        logger.info("SCAN RESULT: {}", scanResult);
        
        if (scanResult == TileValue.GROUND) {
            logger.info(drone.getHeading());
            decisionQ.add(createCommand("heading", "right"));
            decisionQ.add(commands.get("scan"));
        } else if (scanResult == TileValue.OCEAN) {
            decisionQ.add(createCommand("heading", "left"));
            decisionQ.add(commands.get("scan"));
        } else if (scanResult == TileValue.COAST) {
            decisionQ.add(commands.get("fly"));
            decisionQ.add(commands.get("scan"));
        } else {
            decisionQ.add(commands.get("stop"));
        }
        logger.info("DECISION QUEUE: {}", decisionQ);
        logger.info("Added coast traversal decisions");
    }

    public String traverseCoast() {
        JSONObject decision;
        logger.info("traversing coast");
        logger.info("DECISION QUEUE: {}", decisionQ);
        if (decisionQ.isEmpty()) {
            logger.info("decision queue empty. Update decisions");
            traverseCoastDecision();
            decision = decisionQ.remove();
            
        } else {
            logger.info("scanning");
            decision = decisionQ.remove();
        }
        
        getRespectiveDirections();
        if((decision.get("action")).equals("heading")){
        
            String newDirection = decision.getJSONObject("parameters").getString("direction");
            drone.setHeading(newDirection);
        }

        return decision.toString();
    }

    


    
    

    //if creek is NOT found
        //if ocean only
            //turn left
            //go in
            //turn right
            //
    //else creek is found
        //stop mission

    public void updateDrone(){
        //updates battery after a decision is made
        drone.decreaseBattery(cost);
    }
}