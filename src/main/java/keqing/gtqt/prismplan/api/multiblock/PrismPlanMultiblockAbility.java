package keqing.gtqt.prismplan.api.multiblock;

import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import keqing.gtqt.prismplan.api.capability.*;

public class PrismPlanMultiblockAbility {
    //public static final MultiblockAbility<IItemAndFluidHandler> PRISMPLAN_AE = new MultiblockAbility<>("ae_hatch");

    public static final MultiblockAbility<INetWorkProxy> NETWORK_PROXY = new MultiblockAbility<>("network_proxy", INetWorkProxy.class);
    public static final MultiblockAbility<INetWorkStore> NETWORK_STORE = new MultiblockAbility<>("network_store", INetWorkStore.class);
    public static final MultiblockAbility<INetWorkCalculator> NETWORK_CALCULATOR = new MultiblockAbility<>("network_calculator", INetWorkCalculator.class);

    public static final MultiblockAbility<ICellHatch> CELL_HATCH = new MultiblockAbility<>("cell_hatch", ICellHatch.class);
    public static final MultiblockAbility<ICalculatorHatch> CALCULATOR_HATCH = new MultiblockAbility<>("calculator_hatch", ICalculatorHatch.class);
    public static final MultiblockAbility<IEnergyHatch> ENERGY_HATCH = new MultiblockAbility<>("energy_hatch", IEnergyHatch.class);
    public static final MultiblockAbility<IParallelHatch> PARALLEL_HATCH = new MultiblockAbility<>("parallel_hatch",IParallelHatch.class);
    public static final MultiblockAbility<IThreadHatch> THREAD_HATCH = new MultiblockAbility<>("thread_hatch", IThreadHatch.class);
}
