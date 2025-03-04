package keqing.gtqt.prismplan.common.metatileentities.multi;

import keqing.gtqt.prismplan.PrismPlan;
import keqing.gtqt.prismplan.api.utils.PrismPlanLog;
import keqing.gtqt.prismplan.common.metatileentities.multi.multiblock.estorage.MetaTileEntityNetWorkStoreHatch;
import keqing.gtqt.prismplan.common.metatileentities.multi.multiblock.estorage.MetaTileEntityStorageCellControl;
import keqing.gtqt.prismplan.common.metatileentities.multi.multiblock.estorage.MetaTileEntityStorageCellHatch;
import keqing.gtqt.prismplan.common.metatileentities.multi.multiblockpart.MetaTileEntityECPart;
import keqing.gtqt.prismplan.common.metatileentities.multi.multiblockpart.MetaTileEntityECPart111;
import keqing.gtqt.prismplan.common.metatileentities.multi.multiblockpart.MetaTileEntityNetWorkProxyHatch;
import keqing.gtqt.prismplan.common.metatileentities.single.MetaTileEntityWatcher;

import static gregtech.common.metatileentities.MetaTileEntities.registerMetaTileEntity;
import static keqing.gtqt.prismplan.api.utils.PrimsPlanUtility.prismPlanID;

public class PrismPlanMetaTileEntities {

    private static int startId = 18900;

    //测试
    public static MetaTileEntityECPart ECTset;
    public static MetaTileEntityECPart111 ECTget;
    public static MetaTileEntityWatcher watcher;
    public static MetaTileEntityStorageCellControl ESTEST;

    //正式注册
    public static MetaTileEntityNetWorkProxyHatch NETWORK_PROXY_HATCH;
    public static MetaTileEntityNetWorkStoreHatch NETWORK_STORE_HATCH;
    public static MetaTileEntityStorageCellHatch STORAGE_CELL_HATCH;

    public int getID()
    {
        return startId++;
    }
    public static void initialization() {
        PrismPlanLog.logger.info("Registering MetaTileEntities");
        ECTset= registerMetaTileEntity(15519, new MetaTileEntityECPart(prismPlanID("ECTset")));
        ECTget= registerMetaTileEntity(15520, new MetaTileEntityECPart111(prismPlanID("ECTget")));
        watcher= registerMetaTileEntity(15521, new MetaTileEntityWatcher(prismPlanID("watcher")));
        ESTEST= registerMetaTileEntity(15522, new MetaTileEntityStorageCellControl(prismPlanID("ESTEST")));

        NETWORK_PROXY_HATCH= registerMetaTileEntity(15540, new MetaTileEntityNetWorkProxyHatch(prismPlanID("network_proxy_hatch")));
        NETWORK_STORE_HATCH= registerMetaTileEntity(15541, new MetaTileEntityNetWorkStoreHatch(prismPlanID("network_store_hatch")));
        STORAGE_CELL_HATCH= registerMetaTileEntity(15542, new MetaTileEntityStorageCellHatch(prismPlanID("storage_cell_hatch"), 6));

    }
}
