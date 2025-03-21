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
    private JSONObject previousAction;
    private JSONObject previousResult;
    private Controller controller;

    @Override
    public void initialize(String s) {
        logger.info("** Initializing the Exploration Command Center");
        JSONObject info = new JSONObject(new JSONTokener(new StringReader(s)));
        logger.info("** Initialization info:\n {}",info.toString(2));
        String direction = info.getString("heading");
        Integer batteryLevel = info.getInt("budget");
        logger.info("The drone is facing {}", direction);
        logger.info("Battery level is {}", batteryLevel);
    }

    @Override
    public String takeDecision() {
        JSONObject decision = controller.getDecision(previousAction, previousResult);

        /*need to find a better way to determine when the drone should turn or scan
        Currently when the fly action is called in this takeDecision() method it will keep looping until stop action is called 
        For now we stop the mission when the drone has flown 5 times
        */
        /*if (this.testCounter == 5){
            Decision droneStop = new Stop();
            decision = droneStop.action();
        } else{
            Decision droneFly = new Fly();
            decision = droneFly.action();
        }*/
        this.testCounter += 1;

        return decision.toString();
    }

    @Override
    public void acknowledgeResults(String s) {
        JSONObject response = controller.getResult(s);
        previousResult = response;
    }

    @Override
    public String deliverFinalReport() {
        return "no creek found";
    }

}
