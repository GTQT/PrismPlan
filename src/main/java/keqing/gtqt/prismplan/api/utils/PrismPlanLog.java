package keqing.gtqt.prismplan.api.utils;

import org.apache.logging.log4j.Logger;

public class PrismPlanLog {

    public static Logger logger;

    public PrismPlanLog() {
    }

    public static void init(Logger modLogger) {
        logger = modLogger;
    }

}
