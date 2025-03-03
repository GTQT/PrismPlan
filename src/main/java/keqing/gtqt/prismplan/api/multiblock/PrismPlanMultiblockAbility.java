package keqing.gtqt.prismplan.api.multiblock;

import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import keqing.gtqt.prismplan.api.capability.IItemAndFluidHandler;
import keqing.gtqt.prismplan.api.capability.INetWorkProxy;
import keqing.gtqt.prismplan.api.capability.IPrismPlanAE;

public class PrismPlanMultiblockAbility {
    public static final MultiblockAbility<IItemAndFluidHandler> PRISMPLAN_AE = new MultiblockAbility<>("ae_hatch");
    public static final MultiblockAbility<INetWorkProxy> NETWORK_PROXY = new MultiblockAbility<>("network_proxy");
}
