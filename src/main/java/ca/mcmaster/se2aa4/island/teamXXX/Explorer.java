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
    private JSONObject decision;
    private JSONObject response;
    private int cost;
    private String status;
    private JSONObject extraInfo;
    private int i = 0;

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
        this.controller = new Controller(new Drone(batteryLevel, direction));
        

        //for now it will execute the steps provided from findGroundDecisions
        //controller.findGroundDecisions();
    }

    @Override
    public String takeDecision() {

        if (i < 20) {
            decision = controller.commands.get("fly");
            i++;
        }
        
        else if (i == 20) {
            decision = controller.createCommand("echo", "right");
            i++;
        }

        else if (i == 21){
            decision = controller.createCommand("heading", "right");
            controller.drone.setHeading("S");
            i++;
        }

        else if (i < 42) {
            decision = controller.commands.get("fly");
            i++;
        }

        else if(i==42){
            decision = controller.commands.get("scan");
            logger.info("JUST scanned here");
            i++;
        }


        else if(i<700){
            controller.bruteForceDecision();
            decision = controller.bruteForceDecisionResult();
            
            i++;
        }

        else{
            decision = controller.commands.get("stop");
            logger.info("STOP in explore ");
        }

        // else if (i == 38) {
        //     decision = controller.commands.get("scan").toString();
        //     i++;
        // }

        // else if (i > 37 && i != 200) {
        //     decision = controller.traverseCoast().toString();
        //     i++;
        // } else if (i == 200) {
        //     decision = controller.commands.get("stop").toString();
        // }
        
        
        // controller.executeFindGroundDecisions();
        // if (decision.equals("queue empty")) { //means ground has been found and queue is empty
        //     //must enqueue decisions to go to ground
        //     controller.goToGroundDecisions();
        //     decision = controller.executeFindGroundDecisions();
        // }
        

        
        return decision.toString();

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
        
        //Pass the result from the decision that was called 
        controller.resultOfDecision(cost, status, extraInfo);
        logger.info("Current decision: {}", decision);
        /*if (i > 42 && extraInfo.has("range") && extraInfo.has("found") && decision.get("action").equals("echo")) {
            controller.analyzeEcho();
            logger.info("going in here");
        }*/
        //updates the battery level of the drone
        controller.updateDrone();
    }

    @Override
    public String deliverFinalReport() {
        return "no creek found";
    }

}
