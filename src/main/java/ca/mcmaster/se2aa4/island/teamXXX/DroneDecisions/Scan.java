package ca.mcmaster.se2aa4.island.teamXXX.DroneDecisions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class Scan implements Decision{
    private final Logger logger = LogManager.getLogger();

    private int cost;
    private String status;
    private String[] biomes;
    private String[] creeks;
    private String[] sites;

    @Override
    //Read from Json
    public JSONObject action(){
        JSONObject decision = new JSONObject();
        decision.put("action", "scan");  
        logger.info("** Decision: {}",decision.toString());
        return decision;
    }

    @Override
    //Write to Json
    public void handle(JSONObject response) {
        if (response == null) {
            logger.error("Recieved null response");
            return;
        }

        try {
            this.cost = response.getInt("cost");
            this.status = response.getString("status");

            JSONObject extras = response.getJSONObject("extras");
            this.biomes = jsonArrayToStringArray(extras.getJSONArray("biomes"));
            this.creeks = jsonArrayToStringArray(extras.getJSONArray("creeks"));
            this.sites = jsonArrayToStringArray(extras.getJSONArray("creeks"));

            // Log extracted values
            logger.info("** Response Handled: Cost={}, Status={}, Biomes={}, Creeks={}, Sites={}", cost, status, biomes, creeks, sites);

        } catch (Exception e) {
            logger.error("Error parsing response JSON: {}", e.getMessage());
        }
    
    }

    // Helper method to convert JSONArray to String array
    private String[] jsonArrayToStringArray(JSONArray jsonArray) {
        String[] array = new String[jsonArray.length()];
        for (int i = 0; i < jsonArray.length(); i++) {
            array[i] = jsonArray.getString(i);
        }
        return array;
    }
}
