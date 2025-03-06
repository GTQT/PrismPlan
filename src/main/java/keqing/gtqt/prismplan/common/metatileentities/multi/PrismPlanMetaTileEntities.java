package keqing.gtqt.prismplan.common.metatileentities.multi;

import keqing.gtqt.prismplan.api.utils.PrismPlanLog;
import keqing.gtqt.prismplan.common.metatileentities.multi.multiblock.estorage.MetaTileEntityNetWorkStoreHatch;
import keqing.gtqt.prismplan.common.metatileentities.multi.multiblock.estorage.MetaTileEntityStorageCellControl;
import keqing.gtqt.prismplan.common.metatileentities.multi.multiblock.estorage.MetaTileEntityStorageCellHatch;
import keqing.gtqt.prismplan.common.metatileentities.multi.multiblock.estorage.MetaTileEntityStorageEnergyCell;
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


    //正式注册
    public static MetaTileEntityStorageCellControl STORAGE_CELL_CONTROL;
    public static MetaTileEntityNetWorkProxyHatch NETWORK_PROXY_HATCH;
    public static MetaTileEntityNetWorkStoreHatch NETWORK_STORE_HATCH;
    public static MetaTileEntityStorageCellHatch[] STORAGE_CELL_HATCH= new MetaTileEntityStorageCellHatch[5];
    public static MetaTileEntityStorageEnergyCell[] STORAGE_ENERGY_CELL= new MetaTileEntityStorageEnergyCell[5];

    public int getID()
    {
        return startId++;
    }
    public static void initialization() {
        PrismPlanLog.logger.info("Registering MetaTileEntities");
        ECTset= registerMetaTileEntity(15519, new MetaTileEntityECPart(prismPlanID("ECTset")));
        ECTget= registerMetaTileEntity(15520, new MetaTileEntityECPart111(prismPlanID("ECTget")));
        watcher= registerMetaTileEntity(15521, new MetaTileEntityWatcher(prismPlanID("watcher")));

        //可以用的
        STORAGE_CELL_CONTROL = registerMetaTileEntity(15530, new MetaTileEntityStorageCellControl(prismPlanID("storage_cell_control")));

        NETWORK_PROXY_HATCH= registerMetaTileEntity(15540, new MetaTileEntityNetWorkProxyHatch(prismPlanID("network_proxy_hatch")));
        NETWORK_STORE_HATCH= registerMetaTileEntity(15541, new MetaTileEntityNetWorkStoreHatch(prismPlanID("network_store_hatch")));

        STORAGE_CELL_HATCH[0]= registerMetaTileEntity(15550, new MetaTileEntityStorageCellHatch(prismPlanID("storage_cell_hatch.1"), 1));
        STORAGE_CELL_HATCH[1]= registerMetaTileEntity(15551, new MetaTileEntityStorageCellHatch(prismPlanID("storage_cell_hatch.2"), 2));
        STORAGE_CELL_HATCH[2]= registerMetaTileEntity(15552, new MetaTileEntityStorageCellHatch(prismPlanID("storage_cell_hatch.3"), 3));
        STORAGE_CELL_HATCH[3]= registerMetaTileEntity(15553, new MetaTileEntityStorageCellHatch(prismPlanID("storage_cell_hatch.4"), 4));
        STORAGE_CELL_HATCH[4]= registerMetaTileEntity(15554, new MetaTileEntityStorageCellHatch(prismPlanID("storage_cell_hatch.5"), 5));

        STORAGE_ENERGY_CELL[0] = registerMetaTileEntity(15570, new MetaTileEntityStorageEnergyCell(prismPlanID("storage_energy_cell.1"), 1, 1000000));
        STORAGE_ENERGY_CELL[1] = registerMetaTileEntity(15571, new MetaTileEntityStorageEnergyCell(prismPlanID("storage_energy_cell.2"), 2, 4000000));
        STORAGE_ENERGY_CELL[2] = registerMetaTileEntity(15572, new MetaTileEntityStorageEnergyCell(prismPlanID("storage_energy_cell.3"), 3, 16000000));
        STORAGE_ENERGY_CELL[3] = registerMetaTileEntity(15573, new MetaTileEntityStorageEnergyCell(prismPlanID("storage_energy_cell.4"), 4, 64000000));
        STORAGE_ENERGY_CELL[4] = registerMetaTileEntity(15574, new MetaTileEntityStorageEnergyCell(prismPlanID("storage_energy_cell.5"), 5, 256000000));

    }
}
