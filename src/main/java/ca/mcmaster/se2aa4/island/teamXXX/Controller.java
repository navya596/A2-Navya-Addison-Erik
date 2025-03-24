package ca.mcmaster.se2aa4.island.teamXXX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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
    private boolean islandFound = false;
    
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

    public void findCreekOrSite(){
        if(extraInfo.has("biomes") && extraInfo.has("creeks") && extraInfo.has("sites")){
            JSONArray creekArray = extraInfo.getJSONArray("creeks");
            JSONArray siteArray = extraInfo.getJSONArray("sites");
            if(creekArray.length()>0){
                creeks.add(creekArray.getString(0));
            } else if (siteArray.length() > 0){
                site = siteArray.getString(0);
            }
        }
    }

    public boolean isIslandFound(){
        return islandFound;
    }

    public List<String> getCreek(){
        return creeks;
    }

    public String getSite(){
        //returns the creeks that we found
        return site;
    }

    public boolean wasEchoCalled(){
        if(extraInfo.has("range") && extraInfo.has("found")){
            return true;
        } else{
            return false;
        }
    }

    public void goToIsland(){
        if(!decisionQ.isEmpty()){
            return;
        }

        if(extraInfo.has("found") && extraInfo.get("found").equals("GROUND")){
            decisionQ.add(createCommand("heading", "right"));
            drone.setHeading("S");

            int range = (int) extraInfo.get("range");
            for(int i = 0; i<range; i++){
                decisionQ.add(commands.get("fly"));
            }

            decisionQ.add(commands.get("scan"));
            //since we found a ground tile that means we have found the island
            this.islandFound = true;

            return;
        } else {
            decisionQ.add(commands.get("fly"));
            decisionQ.add(createCommand("echo", "right"));
        }
    }


    public void bruteForceDecision(){

        //checks if there is another action queued, if so execute that actio before adding a new one
        if(!decisionQ.isEmpty()){
            return;
        } 

        //checks if echo is called and determines what it should do if it is still on ground or if it is now on the ocean
        else if(wasEchoCalled()){
            int range = (int) extraInfo.get("range");
            String found = (String) extraInfo.get("found");

            //since echo is going to be called after the u-turn
            //if the echo range is 0 that means the tile in front of us is still ground so we can call fly and scan
            if(range == 0 && found.equals("GROUND")){
                decisionQ.add(commands.get("fly"));
                decisionQ.add(commands.get("scan"));
            } 
            //if the range is greater than 0 that means we have to go back to the ground
            else if (found.equals("GROUND") && range > 0){
                for(int i = 0; i<range; i++){
                    decisionQ.add(commands.get("fly"));
                }
            }

            //if no ground is detected after the u-turn then something went wrong
            else{
                decisionQ.add(commands.get("stop"));
            }
        }
        
        //if the drone finds an Ocean tile while facing the South it will reposition itself to face the North
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
        //if there are creeks found and site is found that means that they have both been found and the mission is over
        else if (!creeks.isEmpty() && site != null){
            decisionQ.add(commands.get("stop"));
        }
        
        //queue has no commands in it then pass in a fly and echo command together
        else {

            findCreekOrSite();
            //force stops the mission if we unsuccesfully found both the creek and the emergency site
            if(drone.getBatteryLevel() < 50){
                decisionQ.add(commands.get("stop"));
            }

            decisionQ.add(commands.get("fly"));
            decisionQ.add(commands.get("scan"));
            logger.info("flew and scanned here");
        }
    }

    public JSONObject getActionMade(){
    
        logger.info(decisionQ.peek());
        return decisionQ.remove();
    }

    public void updateDrone(){
        //updates battery after a decision is made
        drone.decreaseBattery(cost);
    }
}