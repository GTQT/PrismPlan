package keqing.gtqt.prismplan.common.metatileentities.multi;

import keqing.gtqt.prismplan.PrismPlanConfig;
import keqing.gtqt.prismplan.api.utils.PrismPlanLog;
import keqing.gtqt.prismplan.common.metatileentities.multi.multiblock.ecalculator.*;
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

    private static final int startId = PrismPlanConfig.MachineSwitch.startId;

    //测试
    public static MetaTileEntityECPart ECTset;
    public static MetaTileEntityECPart111 ECTget;
    public static MetaTileEntityWatcher watcher;


    //正式注册
    public static MetaTileEntityStorageCellControl STORAGE_CELL_CONTROL;
    public static MetaTileEntityCalculatorControl CALCULATOR_CONTROL;

    public static MetaTileEntityNetWorkProxyHatch NETWORK_PROXY_HATCH;
    public static MetaTileEntityNetWorkStoreHatch NETWORK_STORE_HATCH;
    public static MetaTileEntityNetWorkCalculatorHatch NETWORK_CALCULATOR_HATCH;


    public static MetaTileEntityStorageCellHatch[] STORAGE_CELL_HATCH= new MetaTileEntityStorageCellHatch[5];
    public static MetaTileEntityStorageEnergyCell[] STORAGE_ENERGY_CELL= new MetaTileEntityStorageEnergyCell[5];
    public static MetaTileEntityCalculatorCellHatch[] CALCULATOR_CELL_HATCH= new MetaTileEntityCalculatorCellHatch[5];
    public static MetaTileEntityThreadHatch[] THREAD_HATCH= new MetaTileEntityThreadHatch[10];
    public static MetaTileEntityParallelHatch[] PARALLEL_HATCH= new MetaTileEntityParallelHatch[5];


    public static void initialization() {
        PrismPlanLog.logger.info("Registering MetaTileEntities");
        ECTset= registerMetaTileEntity(startId, new MetaTileEntityECPart(prismPlanID("ECTset")));
        ECTget= registerMetaTileEntity(startId+1, new MetaTileEntityECPart111(prismPlanID("ECTget")));
        watcher= registerMetaTileEntity(startId+2, new MetaTileEntityWatcher(prismPlanID("watcher")));

        //可以用的
        STORAGE_CELL_CONTROL = registerMetaTileEntity(startId+100, new MetaTileEntityStorageCellControl(prismPlanID("storage_cell_control")));
        CALCULATOR_CONTROL = registerMetaTileEntity(startId+101, new MetaTileEntityCalculatorControl(prismPlanID("calculator_control")));

        NETWORK_PROXY_HATCH= registerMetaTileEntity(startId+110, new MetaTileEntityNetWorkProxyHatch(prismPlanID("network_proxy_hatch")));
        NETWORK_STORE_HATCH= registerMetaTileEntity(startId+111, new MetaTileEntityNetWorkStoreHatch(prismPlanID("network_store_hatch")));
        NETWORK_CALCULATOR_HATCH= registerMetaTileEntity(startId+112, new MetaTileEntityNetWorkCalculatorHatch(prismPlanID("network_calculator_hatch")));

        STORAGE_CELL_HATCH[0]= registerMetaTileEntity(startId+120, new MetaTileEntityStorageCellHatch(prismPlanID("storage_cell_hatch.1"), 1));
        STORAGE_CELL_HATCH[1]= registerMetaTileEntity(startId+121, new MetaTileEntityStorageCellHatch(prismPlanID("storage_cell_hatch.2"), 2));
        STORAGE_CELL_HATCH[2]= registerMetaTileEntity(startId+122, new MetaTileEntityStorageCellHatch(prismPlanID("storage_cell_hatch.3"), 3));
        STORAGE_CELL_HATCH[3]= registerMetaTileEntity(startId+123, new MetaTileEntityStorageCellHatch(prismPlanID("storage_cell_hatch.4"), 4));
        STORAGE_CELL_HATCH[4]= registerMetaTileEntity(startId+124, new MetaTileEntityStorageCellHatch(prismPlanID("storage_cell_hatch.5"), 5));

        STORAGE_ENERGY_CELL[0] = registerMetaTileEntity(startId+125, new MetaTileEntityStorageEnergyCell(prismPlanID("storage_energy_cell.1"), 1, 1000000));
        STORAGE_ENERGY_CELL[1] = registerMetaTileEntity(startId+126, new MetaTileEntityStorageEnergyCell(prismPlanID("storage_energy_cell.2"), 2, 4000000));
        STORAGE_ENERGY_CELL[2] = registerMetaTileEntity(startId+127, new MetaTileEntityStorageEnergyCell(prismPlanID("storage_energy_cell.3"), 3, 16000000));
        STORAGE_ENERGY_CELL[3] = registerMetaTileEntity(startId+128, new MetaTileEntityStorageEnergyCell(prismPlanID("storage_energy_cell.4"), 4, 64000000));
        STORAGE_ENERGY_CELL[4] = registerMetaTileEntity(startId+129, new MetaTileEntityStorageEnergyCell(prismPlanID("storage_energy_cell.5"), 5, 256000000));

        CALCULATOR_CELL_HATCH[0]= registerMetaTileEntity(startId+130, new MetaTileEntityCalculatorCellHatch(prismPlanID("calculator_cell_hatch.1"), 1));
        CALCULATOR_CELL_HATCH[1]= registerMetaTileEntity(startId+131, new MetaTileEntityCalculatorCellHatch(prismPlanID("calculator_cell_hatch.2"), 2));
        CALCULATOR_CELL_HATCH[2]= registerMetaTileEntity(startId+132, new MetaTileEntityCalculatorCellHatch(prismPlanID("calculator_cell_hatch.3"), 3));
        CALCULATOR_CELL_HATCH[3]= registerMetaTileEntity(startId+133, new MetaTileEntityCalculatorCellHatch(prismPlanID("calculator_cell_hatch.4"), 4));
        CALCULATOR_CELL_HATCH[4]= registerMetaTileEntity(startId+134, new MetaTileEntityCalculatorCellHatch(prismPlanID("calculator_cell_hatch.5"), 5));

        PARALLEL_HATCH[0]= registerMetaTileEntity(startId+135, new MetaTileEntityParallelHatch(prismPlanID("parallel_hatch.1"), 32));
        PARALLEL_HATCH[1]= registerMetaTileEntity(startId+136, new MetaTileEntityParallelHatch(prismPlanID("parallel_hatch.2"), 256));
        PARALLEL_HATCH[2]= registerMetaTileEntity(startId+137, new MetaTileEntityParallelHatch(prismPlanID("parallel_hatch.3"), 2048));
        PARALLEL_HATCH[3]= registerMetaTileEntity(startId+138, new MetaTileEntityParallelHatch(prismPlanID("parallel_hatch.4"), 16384));
        PARALLEL_HATCH[4]= registerMetaTileEntity(startId+139, new MetaTileEntityParallelHatch(prismPlanID("parallel_hatch.5"), 131072));

        THREAD_HATCH[0]= registerMetaTileEntity(startId+140, new MetaTileEntityThreadHatch(prismPlanID("thread_hatch.1"), 1,0));
        THREAD_HATCH[1]= registerMetaTileEntity(startId+141, new MetaTileEntityThreadHatch(prismPlanID("thread_hatch.2"), 2,0));
        THREAD_HATCH[2]= registerMetaTileEntity(startId+142, new MetaTileEntityThreadHatch(prismPlanID("thread_hatch.3"), 4,0));
        THREAD_HATCH[3]= registerMetaTileEntity(startId+143, new MetaTileEntityThreadHatch(prismPlanID("thread_hatch.4"), 8,0));
        THREAD_HATCH[4]= registerMetaTileEntity(startId+144, new MetaTileEntityThreadHatch(prismPlanID("thread_hatch.5"), 16,0));

        THREAD_HATCH[5]= registerMetaTileEntity(startId+145, new MetaTileEntityThreadHatch(prismPlanID("thread_hatch.hyper.1"), 0,2));
        THREAD_HATCH[6]= registerMetaTileEntity(startId+146, new MetaTileEntityThreadHatch(prismPlanID("thread_hatch.hyper.2"), 0,4));
        THREAD_HATCH[7]= registerMetaTileEntity(startId+147, new MetaTileEntityThreadHatch(prismPlanID("thread_hatch.hyper.3"), 0,8));
        THREAD_HATCH[8]= registerMetaTileEntity(startId+148, new MetaTileEntityThreadHatch(prismPlanID("thread_hatch.hyper.4"), 0,16));
        THREAD_HATCH[9]= registerMetaTileEntity(startId+149, new MetaTileEntityThreadHatch(prismPlanID("thread_hatch.hyper.5"), 1,32));
    }
}
