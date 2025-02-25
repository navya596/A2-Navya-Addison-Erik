package ca.mcmaster.se2aa4.island.teamXXX.DroneDecisions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

public class Fly implements Decision{
    private final Logger logger = LogManager.getLogger();

    @Override
    public JSONObject action(){
        JSONObject decision = new JSONObject();
        decision.put("action", "fly");
        logger.info("** Decision: {}",decision.toString());
        return decision;
    }
}
