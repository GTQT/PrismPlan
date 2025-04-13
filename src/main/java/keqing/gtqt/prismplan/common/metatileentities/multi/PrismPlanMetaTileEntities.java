package keqing.gtqt.prismplan.common.metatileentities.multi;

import keqing.gtqt.prismplan.api.utils.PrismPlanLog;
import keqing.gtqt.prismplan.common.metatileentities.multi.multiblock.ecalculator.*;
import keqing.gtqt.prismplan.common.metatileentities.multi.multiblock.estorage.MetaTileEntityNetWorkStoreHatch;
import keqing.gtqt.prismplan.common.metatileentities.multi.multiblock.estorage.MetaTileEntityStorageCellControl;
import keqing.gtqt.prismplan.common.metatileentities.multi.multiblock.estorage.MetaTileEntityStorageCellHatch;
import keqing.gtqt.prismplan.common.metatileentities.multi.multiblock.estorage.MetaTileEntityStorageEnergyCell;
import keqing.gtqt.prismplan.common.metatileentities.multi.multiblockpart.MetaTileEntityNetWorkProxyHatch;
import keqing.gtqt.prismplan.common.metatileentities.single.MetaTileEntityWatcher;

import static gregtech.common.metatileentities.MetaTileEntities.registerMetaTileEntity;
import static keqing.gtqt.prismplan.api.utils.PrimsPlanUtility.prismPlanID;

public class PrismPlanMetaTileEntities {

    //测试
    public static MetaTileEntityWatcher ME_WATCHER;

    //正式注册
    public static MetaTileEntityStorageCellControl STORAGE_CELL_CONTROL;
    public static MetaTileEntityCalculatorControl CALCULATOR_CONTROL;

    public static MetaTileEntityNetWorkProxyHatch NETWORK_PROXY_HATCH;
    public static MetaTileEntityNetWorkStoreHatch NETWORK_STORE_HATCH;
    public static MetaTileEntityNetWorkCalculatorHatch NETWORK_CALCULATOR_HATCH;

    public static MetaTileEntityStorageCellHatch[] STORAGE_CELL_HATCH = new MetaTileEntityStorageCellHatch[5];
    public static MetaTileEntityStorageEnergyCell[] STORAGE_ENERGY_CELL = new MetaTileEntityStorageEnergyCell[5];
    public static MetaTileEntityCalculatorCellHatch[] CALCULATOR_CELL_HATCH = new MetaTileEntityCalculatorCellHatch[5];
    public static MetaTileEntityThreadHatch[] THREAD_HATCH = new MetaTileEntityThreadHatch[10];
    public static MetaTileEntityParallelHatch[] PARALLEL_HATCH = new MetaTileEntityParallelHatch[5];


    public static void initialization() {
        PrismPlanLog.logger.info("Registering MetaTileEntities");
        ME_WATCHER = registerMetaTileEntity(1, new MetaTileEntityWatcher(prismPlanID("me_watcher")));
        //可以用的
        STORAGE_CELL_CONTROL = registerMetaTileEntity(10, new MetaTileEntityStorageCellControl(prismPlanID("storage_cell_control")));
        CALCULATOR_CONTROL = registerMetaTileEntity(11, new MetaTileEntityCalculatorControl(prismPlanID("calculator_control")));

        NETWORK_PROXY_HATCH = registerMetaTileEntity(12, new MetaTileEntityNetWorkProxyHatch(prismPlanID("network_proxy_hatch")));
        NETWORK_STORE_HATCH = registerMetaTileEntity(13, new MetaTileEntityNetWorkStoreHatch(prismPlanID("network_store_hatch")));
        NETWORK_CALCULATOR_HATCH = registerMetaTileEntity(14, new MetaTileEntityNetWorkCalculatorHatch(prismPlanID("network_calculator_hatch")));

        STORAGE_CELL_HATCH[0] = registerMetaTileEntity(20, new MetaTileEntityStorageCellHatch(prismPlanID("storage_cell_hatch.1"), 1));
        STORAGE_CELL_HATCH[1] = registerMetaTileEntity(21, new MetaTileEntityStorageCellHatch(prismPlanID("storage_cell_hatch.2"), 2));
        STORAGE_CELL_HATCH[2] = registerMetaTileEntity(22, new MetaTileEntityStorageCellHatch(prismPlanID("storage_cell_hatch.3"), 3));
        STORAGE_CELL_HATCH[3] = registerMetaTileEntity(23, new MetaTileEntityStorageCellHatch(prismPlanID("storage_cell_hatch.4"), 4));
        STORAGE_CELL_HATCH[4] = registerMetaTileEntity(24, new MetaTileEntityStorageCellHatch(prismPlanID("storage_cell_hatch.5"), 5));

        STORAGE_ENERGY_CELL[0] = registerMetaTileEntity(30, new MetaTileEntityStorageEnergyCell(prismPlanID("storage_energy_cell.1"), 1, 1000000));
        STORAGE_ENERGY_CELL[1] = registerMetaTileEntity(31, new MetaTileEntityStorageEnergyCell(prismPlanID("storage_energy_cell.2"), 2, 4000000));
        STORAGE_ENERGY_CELL[2] = registerMetaTileEntity(32, new MetaTileEntityStorageEnergyCell(prismPlanID("storage_energy_cell.3"), 3, 16000000));
        STORAGE_ENERGY_CELL[3] = registerMetaTileEntity(33, new MetaTileEntityStorageEnergyCell(prismPlanID("storage_energy_cell.4"), 4, 64000000));
        STORAGE_ENERGY_CELL[4] = registerMetaTileEntity(34, new MetaTileEntityStorageEnergyCell(prismPlanID("storage_energy_cell.5"), 5, 256000000));

        CALCULATOR_CELL_HATCH[0] = registerMetaTileEntity(40, new MetaTileEntityCalculatorCellHatch(prismPlanID("calculator_cell_hatch.1"), 1));
        CALCULATOR_CELL_HATCH[1] = registerMetaTileEntity(41, new MetaTileEntityCalculatorCellHatch(prismPlanID("calculator_cell_hatch.2"), 2));
        CALCULATOR_CELL_HATCH[2] = registerMetaTileEntity(42, new MetaTileEntityCalculatorCellHatch(prismPlanID("calculator_cell_hatch.3"), 3));
        CALCULATOR_CELL_HATCH[3] = registerMetaTileEntity(43, new MetaTileEntityCalculatorCellHatch(prismPlanID("calculator_cell_hatch.4"), 4));
        CALCULATOR_CELL_HATCH[4] = registerMetaTileEntity(44, new MetaTileEntityCalculatorCellHatch(prismPlanID("calculator_cell_hatch.5"), 5));

        PARALLEL_HATCH[0] = registerMetaTileEntity(50, new MetaTileEntityParallelHatch(prismPlanID("parallel_hatch.1"), 1, 32));
        PARALLEL_HATCH[1] = registerMetaTileEntity(51, new MetaTileEntityParallelHatch(prismPlanID("parallel_hatch.2"), 2, 256));
        PARALLEL_HATCH[2] = registerMetaTileEntity(52, new MetaTileEntityParallelHatch(prismPlanID("parallel_hatch.3"), 3, 2048));
        PARALLEL_HATCH[3] = registerMetaTileEntity(53, new MetaTileEntityParallelHatch(prismPlanID("parallel_hatch.4"), 4, 16384));
        PARALLEL_HATCH[4] = registerMetaTileEntity(54, new MetaTileEntityParallelHatch(prismPlanID("parallel_hatch.5"), 5, 131072));

        THREAD_HATCH[0] = registerMetaTileEntity(60, new MetaTileEntityThreadHatch(prismPlanID("thread_hatch.1"), 1, 1, 0));
        THREAD_HATCH[1] = registerMetaTileEntity(61, new MetaTileEntityThreadHatch(prismPlanID("thread_hatch.2"), 2, 2, 0));
        THREAD_HATCH[2] = registerMetaTileEntity(62, new MetaTileEntityThreadHatch(prismPlanID("thread_hatch.3"), 3, 4, 0));
        THREAD_HATCH[3] = registerMetaTileEntity(63, new MetaTileEntityThreadHatch(prismPlanID("thread_hatch.4"), 4, 8, 0));
        THREAD_HATCH[4] = registerMetaTileEntity(64, new MetaTileEntityThreadHatch(prismPlanID("thread_hatch.5"), 5, 16, 0));

        THREAD_HATCH[5] = registerMetaTileEntity(70, new MetaTileEntityThreadHatch(prismPlanID("thread_hatch.hyper.1"), 1, 0, 2));
        THREAD_HATCH[6] = registerMetaTileEntity(71, new MetaTileEntityThreadHatch(prismPlanID("thread_hatch.hyper.2"), 2, 0, 4));
        THREAD_HATCH[7] = registerMetaTileEntity(72, new MetaTileEntityThreadHatch(prismPlanID("thread_hatch.hyper.3"), 3, 0, 8));
        THREAD_HATCH[8] = registerMetaTileEntity(73, new MetaTileEntityThreadHatch(prismPlanID("thread_hatch.hyper.4"), 4, 0, 16));
        THREAD_HATCH[9] = registerMetaTileEntity(74, new MetaTileEntityThreadHatch(prismPlanID("thread_hatch.hyper.5"), 5, 1, 32));
    }
}
