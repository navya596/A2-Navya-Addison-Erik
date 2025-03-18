package ca.mcmaster.se2aa4.island.teamXXX.DroneDecisions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

public class Stop implements Decision {
    private final Logger logger = LogManager.getLogger();
    private int cost;
    private String status;
    
    @Override
    //Write to Json
    public JSONObject action(){
        JSONObject decision = new JSONObject();
        decision.put("action", "stop");
        logger.info("** Decision: {}",decision.toString());
        return decision;
    }

    @Override
    //Read from Json
    public void handle(JSONObject response) {
        if (response == null) {
            logger.error("Recieved null response");
            return;
        }

        
        try {
            this.cost = response.getInt("cost");
            this.status = response.getString("status");

            // Log extracted values
            logger.info("** Response Handled: Cost={}, Status={}", cost, status);

        } catch (Exception e) {
            logger.error("Error parsing response JSON: {}", e.getMessage());
        }

    }
}
