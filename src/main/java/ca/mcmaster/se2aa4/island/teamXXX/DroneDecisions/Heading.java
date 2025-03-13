package ca.mcmaster.se2aa4.island.teamXXX.DroneDecisions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import ca.mcmaster.se2aa4.island.teamXXX.Direction;

public class Heading implements Decision{
    private final Logger logger = LogManager.getLogger();
    private Direction direction; //direction used for heading

    // Constructor to allow setting the direction 
    public Heading(Direction direction) {
        this.direction = direction;
    }
    @Override
    public JSONObject action(){
        JSONObject decision = new JSONObject();
        decision.put("action", "heading");
        JSONObject parameters = new JSONObject();
        parameters.put("direction", this.direction.name()); 
        decision.put("parameters", parameters);
        logger.info("** Decision: {}",decision.toString());
        return decision;
    }
}
