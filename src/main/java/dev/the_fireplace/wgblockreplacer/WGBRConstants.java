package dev.the_fireplace.wgblockreplacer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class WGBRConstants
{
    public static final String MODID = "wgblockreplacer";

    private static final Logger LOGGER = LogManager.getLogger(MODID);

    public static Logger getLogger() {
        return LOGGER;
    }
}
