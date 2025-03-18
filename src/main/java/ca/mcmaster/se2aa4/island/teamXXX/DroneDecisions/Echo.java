package ca.mcmaster.se2aa4.island.teamXXX.DroneDecisions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import ca.mcmaster.se2aa4.island.teamXXX.Direction;

public class Echo implements Decision{
    private final Logger logger = LogManager.getLogger();
    private Direction direction; //direction used for radar

    private int cost;
    private int range;
    private String found;
    private String status;

    // Constructor to allow setting the direction 
    public Echo(Direction direction) {
        this.direction = direction;
    }
    //Write to Json
    @Override
    public JSONObject action(){
        JSONObject decision = new JSONObject();
        decision.put("action", "echo");
        JSONObject parameters = new JSONObject();
        parameters.put("direction", this.direction.name()); 
        decision.put("parameters", parameters);
        logger.info("** Decision: {}",decision.toString());
        return decision;
    }

    //Extract from Json
    @Override
    public void handle(JSONObject response) {
        if (response == null) {
            logger.error("Recieved null response");
            return;
        }

        try {
            this.cost = response.getInt("cost");
            this.status = response.getString("status");

            JSONObject extras = response.getJSONObject("extras");
            this.range = extras.getInt("range");
            this.found = extras.getString("found");

            // Log extracted values
            logger.info("** Response Handled: Cost={}, Status={}, Range={}, Found={}", cost, status, range, found);

        } catch (Exception e) {
            logger.error("Error parsing response JSON: {}", e.getMessage());
        }
    }

}
