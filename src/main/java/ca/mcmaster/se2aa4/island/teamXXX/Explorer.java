package ca.mcmaster.se2aa4.island.teamXXX;

import java.io.StringReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.json.JSONTokener;

import eu.ace_design.island.bot.IExplorerRaid;

public class Explorer implements IExplorerRaid {

    private final Logger logger = LogManager.getLogger();
    private int testCounter;
    private Controller controller;
    private String decision;
    private JSONObject response;
    private int cost;
    private String status;
    private JSONObject extraInfo;

    @Override
    public void initialize(String s) {
        logger.info("** Initializing the Exploration Command Center");
        JSONObject info = new JSONObject(new JSONTokener(new StringReader(s)));
        logger.info("** Initialization info:\n {}",info.toString(2));
        String direction = info.getString("heading");
        Integer batteryLevel = info.getInt("budget");
        logger.info("The drone is facing {}", direction);
        logger.info("Battery level is {}", batteryLevel);
    
        //Initialize the Controller object
        this.controller = new Controller(new Drone(batteryLevel, direction, 1, 1));
        

        //for now it will execute the steps provided from findGroundDecisions
        controller.findGroundDecisions();
    }

    @Override
    public String takeDecision() {
        decision = controller.executeFindGroundDecisions();
        if (decision.equals("foundGround")) { //means ground has been found and queue is empty
            //must enqueue decisions to go to ground
            controller.goToGround(extraInfo);
            return controller.goToGround();
        }
        else {
            return decision;
        }
    }

    @Override
    public void acknowledgeResults(String s) {
        response = new JSONObject(new JSONTokener(new StringReader(s)));
        logger.info("** Response received:\n"+response.toString(2));
        cost = response.getInt("cost");
        logger.info("The cost of the action was {}", cost);
        status = response.getString("status");
        logger.info("The status of the drone is {}", status);
        extraInfo = response.getJSONObject("extras");
        logger.info("Additional information received: {}", extraInfo);
        
        //Pass the result from the decision that was called in takeDecision()
        controller.resultOfDecision(cost, status, extraInfo);

        //updates the battery level of the drone
        controller.updateDrone();
    }

    @Override
    public String deliverFinalReport() {
        return "no creek found";
    }

}
