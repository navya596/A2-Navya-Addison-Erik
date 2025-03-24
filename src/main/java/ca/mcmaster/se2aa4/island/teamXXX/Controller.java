package ca.mcmaster.se2aa4.island.teamXXX;

import java.util.ArrayList;
import java.util.List;
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
    protected Map<String, JSONObject> commands; //Keys will be used to return command as a JSON object back to acknowledge results
    private String front;
    private String left;
    private String right;
    private Integer cost;
    private String status;
    private JSONObject extraInfo;
    private List<String> creeks = new ArrayList<>();
    private String site;

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
        getRespectiveDirections();
        //create put action in json oject
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

    public TileValue analyzeScan() {
        if (extraInfo.has("biomes")) {
            JSONArray biomesFound = extraInfo.getJSONArray("biomes");
            JSONArray creekFound = extraInfo.getJSONArray("creeks");
            JSONArray siteFound = extraInfo.getJSONArray("sites");
            boolean hasOcean = false;

            //checks if the tile is a creek if it is it adds it the value found to the creek list
            if(!creekFound.isEmpty()){
                creeks.add(creekFound.getString(0));
                return TileValue.CREEK;
            }

            //checks if the tile is a site if it is it sets the value found
            if(!siteFound.isEmpty()){
                site = siteFound.getString(0);
                return TileValue.SITE;
            }

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

    public boolean wasEchoCalled(){
        if(extraInfo.has("range") && extraInfo.has("found")){
            return true;
        } else{
            return false;
        }
    }

    public boolean wasScanCalled(){
        if(extraInfo.has("biomes")){
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
    private boolean isTraversingUp = true;
    private boolean echoCheck = false;
    private int x_len = 0;
    private int y_len = 0;
    private int xCoord;
    private int yCoord;
    


    public void bruteForceDecision(){
        if(!decisionQ.isEmpty()){
            return;
        } 

        if (!isXMappingStarted) {
            if (extraInfo.get("found").equals("GROUND")) {
                isXMappingStarted = true;
            } else {
                decisionQ.add(commands.get("fly"));
                decisionQ.add(createCommand("echo", "right"));
            }
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
        } else if (!isYMappingStarted) {
            if (extraInfo.get("found").equals("GROUND")) {
                isYMappingStarted = true;
            }   else {
                decisionQ.add(commands.get("fly"));
                decisionQ.add(createCommand("echo", "right"));
            }
        } else if (!isYMappingDone) {
            if (extraInfo.get("found").equals("OUT_OF_RANGE")) {
                isYMappingDone = true;
            }  else {
                decisionQ.add(commands.get("fly"));
                decisionQ.add(createCommand("echo", "right"));
                y_len++;
            }
        } else if (!isInitialCorrectionDone) {
            decisionQ.add(createCommand("heading", "right"));
            drone.changeHeading(false);
            
            decisionQ.add(createCommand("heading", "right"));
            drone.changeHeading(false);
            
            decisionQ.add(commands.get("fly"));
            decisionQ.add(createCommand("echo", "front"));
            
            //corrects the x and y lengths of island (algorithm counts one extra time)
            x_len--;
            y_len--;

            xCoord = x_len;
            yCoord = y_len;
            isInitialCorrectionDone = true;

        
        } else if (isTraversingUp && xCoord >= 0) {
            if (yCoord == 1) {
                decisionQ.add(createCommand("heading", "left"));
                drone.changeHeading(true);
                
                decisionQ.add(createCommand("heading", "left"));
                drone.changeHeading(true);
                
                decisionQ.add(createCommand("echo", "front"));
                isTraversingUp = !isTraversingUp;
                xCoord = xCoord - 2;
            } else if (wasEchoCalled()) {
                if (echoCheck && extraInfo.get("found").equals("OUT_OF_RANGE")) {
                    while (yCoord > 1) {
                        decisionQ.add(commands.get("fly"));
                        yCoord--;
                    } 
                    echoCheck = false;
                } else {
                    for (int i = 0; i < extraInfo.getInt("range")+1; i++) {
                        decisionQ.add(commands.get("fly"));
                        yCoord--;
                    }
                    decisionQ.add(commands.get("scan"));
                }
            } else if (wasScanCalled() && analyzeScan() == TileValue.OCEAN) {
                
                decisionQ.add(createCommand("echo", "front"));
                echoCheck = true;
                
            }
            
            //if the tile was marked as a Creek or a Site it captured
            //as long as the scan result is not an ocean we can continue to fly and scan the island
            else {
                decisionQ.add(commands.get("fly"));
                decisionQ.add(commands.get("scan"));
                
                yCoord--;
            }
        } else if (!isTraversingUp && xCoord >= 0) {
            if (yCoord == y_len) {
                decisionQ.add(createCommand("heading", "right"));
                drone.changeHeading(false);
                
                decisionQ.add(createCommand("heading", "right"));
                drone.changeHeading(false);

                decisionQ.add(createCommand("echo", "front"));
                isTraversingUp = !isTraversingUp;
                xCoord = xCoord - 2;
            } else if (wasEchoCalled()) {
                if (echoCheck && extraInfo.get("found").equals("OUT_OF_RANGE")) {
                    while (yCoord < y_len) {
                        decisionQ.add(commands.get("fly"));
                        yCoord++;
                    } 
                    echoCheck = false;
                } else {
                    for (int i = 0; i < extraInfo.getInt("range")+1; i++) {
                        decisionQ.add(commands.get("fly"));
                        yCoord++;
                    }
                    decisionQ.add(commands.get("scan"));
                }
                
            } else if (wasScanCalled() && analyzeScan() == TileValue.OCEAN) {
                decisionQ.add(createCommand("echo", "front"));
                echoCheck = true;
            }
            else {
                decisionQ.add(commands.get("fly"));
                decisionQ.add(commands.get("scan"));
                
                yCoord++;
            }
        } else if(!creeks.isEmpty() && site != null){
            decisionQ.add(commands.get("stop"));
        }        
        else {
            decisionQ.add(commands.get("stop"));
        }
    }
    
    public JSONObject bruteForceDecisionResult(){
        if (decisionQ.isEmpty()) {
            bruteForceDecision();
        } 
        logger.info(decisionQ.peek());
        return decisionQ.remove();
    }

    public String getSite(){
        return site;
    }

    public List<String> getCreeks(){
        return creeks;
    }

    public void updateDrone(){
        //updates battery after a decision is made
        drone.decreaseBattery(cost);
    }
}