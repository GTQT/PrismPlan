package keqing.gtqt.prismplan.common.metatileentities.multi;

import keqing.gtqt.prismplan.PrismPlan;
import keqing.gtqt.prismplan.api.utils.PrismPlanLog;
import keqing.gtqt.prismplan.common.metatileentities.multi.multiblock.estorage.estoreTEST;
import keqing.gtqt.prismplan.common.metatileentities.multi.multiblockpart.MetaTileEntityECPart;
import keqing.gtqt.prismplan.common.metatileentities.multi.multiblockpart.MetaTileEntityECPart111;
import keqing.gtqt.prismplan.common.metatileentities.single.MetaTileEntityWatcher;

import static gregtech.common.metatileentities.MetaTileEntities.registerMetaTileEntity;
import static keqing.gtqt.prismplan.api.utils.PrimsPlanUtility.prismPlanID;

public class PrismPlanMetaTileEntities {

    private static int startId = 18900;

    public static MetaTileEntityECPart ECTset;
    public static MetaTileEntityECPart111 ECTget;
    public static MetaTileEntityWatcher watcher;
    public static estoreTEST ESTEST;
    public int getID()
    {
        return startId++;
    }
    public static void initialization() {
        PrismPlanLog.logger.info("Registering MetaTileEntities");
        ECTset= registerMetaTileEntity(15519, new MetaTileEntityECPart(prismPlanID("ECTset")));
        ECTget= registerMetaTileEntity(15520, new MetaTileEntityECPart111(prismPlanID("ECTget")));
        watcher= registerMetaTileEntity(15521, new MetaTileEntityWatcher(prismPlanID("watcher")));
        ESTEST= registerMetaTileEntity(15522, new estoreTEST(prismPlanID("ESTEST")));

    }
}
