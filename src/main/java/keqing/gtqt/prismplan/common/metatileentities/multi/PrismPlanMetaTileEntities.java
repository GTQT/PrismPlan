package keqing.gtqt.prismplan.common.metatileentities.multi;

import keqing.gtqt.prismplan.api.utils.PrismPlanLog;

public class PrismPlanMetaTileEntities {

    private static int startId = 18900;

    public int getID()
    {
        return startId++;
    }
    public static void initialization() {
        PrismPlanLog.logger.info("Registering MetaTileEntities");

    }
}
