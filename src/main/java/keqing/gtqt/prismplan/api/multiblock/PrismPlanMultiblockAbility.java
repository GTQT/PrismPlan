package keqing.gtqt.prismplan.api.multiblock;

import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import keqing.gtqt.prismplan.api.capability.*;

public class PrismPlanMultiblockAbility {
    public static final MultiblockAbility<IItemAndFluidHandler> PRISMPLAN_AE = new MultiblockAbility<>("ae_hatch");
    public static final MultiblockAbility<INetWorkProxy> NETWORK_PROXY = new MultiblockAbility<>("network_proxy");
    public static final MultiblockAbility<INetWorkStore> NETWORK_STORE = new MultiblockAbility<>("network_store");

    public static final MultiblockAbility<ICellHatch> CELL_HATCH = new MultiblockAbility<>("cell_hatch");
}
