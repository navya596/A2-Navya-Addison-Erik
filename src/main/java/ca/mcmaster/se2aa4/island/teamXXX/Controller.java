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
    private String site;
    private JSONObject backLogEcho;
    private JSONObject backLogStop;
    
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
            decisionQ.add(createCommand("echo", "front"));
        } else if (scanResult == TileValue.COAST) {
            decisionQ.add(commands.get("fly"));
            decisionQ.add(commands.get("scan"));
        } else {
            decisionQ.add(commands.get("stop"));
            logger.info("STOP 1");
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

    public void findCreekOrSite(){
        if(extraInfo.has("biomes") && extraInfo.has("creeks") && extraInfo.has("sites")){
            JSONArray creekArray = extraInfo.getJSONArray("creeks");
            JSONArray siteArray = extraInfo.getJSONArray("sites");
            if(creekArray.length()>0){
                this.creek = creekArray.getString(0);
                logger.info("IN HERE");
                logger.info(creek);    
            } else if (siteArray.length() > 0){
                this.site = siteArray.getString(0);
                logger.info("SITES FOUND");
            }


            logger.info(creekArray.length() == 0);
            logger.info(creekArray.length() == 0);
        }
    }

    public String getCreek(){
        return creek;
    }

    public String getSite(){
        return site;
    }

    public boolean wasEchoCalled(){
        if(extraInfo.has("range") && extraInfo.has("found")){
            return true;
        } else{
            return false;
        }
    }



    public void bruteForceDecision(){
        //if two items are added to the Queue one after another we won't be able to properly depict which action happens when
        //the backLogAction ensures that the next decision made will be off of the correct action

        if(!decisionQ.isEmpty()){
            return;
            //enqueues the action from the backLog to the decision we want to happen next
            //decisionQ.add(backLogAction.remove());
        } 

        else if(wasEchoCalled()){
            int range = (int) extraInfo.get("range");
            String found = (String) extraInfo.get("found");

            //since echo is going to be called after the u-turn
            //if the echo range is 0 that means the tile in front of us is still ground so we can call fly and scan
            if(range == 0 && found.equals("GROUND")){
                decisionQ.add(commands.get("fly"));
                decisionQ.add(commands.get("scan"));
            } else if (found.equals("GROUND") && range > 0){

                
                for(int i = 0; i<range; i++){
                    decisionQ.add(commands.get("fly"));
                    //backLogAction.add(commands.get("fly"));
                }
            }

            //if no ground is detected after the u-turn then something went wrong
            else{
                decisionQ.add(commands.get("stop"));
            }
        }
        
        //if the drone finds an Ocean tile on while its facing east it will reposition itself to face west
        else if(drone.getHeading().equals("S") && analyzeScan().equals(TileValue.OCEAN)){
            
            decisionQ.add(createCommand("heading", "left"));
            drone.setHeading("E");
           
            decisionQ.add(createCommand("heading", "left"));
            
            drone.setHeading("N");
            decisionQ.add(createCommand("echo", "front"));

        } 
        //vice versa of the if statement above
        else if(drone.getHeading().equals("N") && analyzeScan().equals(TileValue.OCEAN)){
            
            decisionQ.add(createCommand("heading", "right"));
            drone.setHeading("E");

            decisionQ.add(createCommand("heading", "right"));
            drone.setHeading("S");

            decisionQ.add(createCommand("echo", "front"));
        }
        
        //queue has no commands in it then pass in a fly and echo command together
        else {

            findCreekOrSite();
            logger.info(this.creek);
            logger.info(this.site);

            decisionQ.add(commands.get("fly"));
            decisionQ.add(commands.get("scan"));
            logger.info("flew and scanned here");
        }
    }

    public void checkBatteryLife(){
        if(drone.getBatteryLevel()<30){
            decisionQ.add(commands.get("stop"));
        }
    }

    public JSONObject bruteForceDecisionResult(){
    
        logger.info(decisionQ.peek());
        return decisionQ.remove();
    }

    public void updateDrone(){
        //updates battery after a decision is made
        drone.decreaseBattery(cost);
    }
}