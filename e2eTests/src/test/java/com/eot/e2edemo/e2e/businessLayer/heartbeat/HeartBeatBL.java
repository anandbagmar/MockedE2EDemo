package com.eot.e2edemo.e2e.businessLayer.heartbeat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HeartBeatBL {
    private static final Logger LOGGER = LogManager.getLogger(HeartBeatBL.class.getName());

    public void startHeatBeat(String userPersona) {
        LOGGER.info("Starting heartbeat for persona: " + userPersona);
    }
}
