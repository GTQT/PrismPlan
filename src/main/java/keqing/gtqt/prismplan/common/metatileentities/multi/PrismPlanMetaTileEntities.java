package keqing.gtqt.prismplan.common.metatileentities.multi;

import keqing.gtqt.prismplan.api.utils.PrismPlanLog;
import keqing.gtqt.prismplan.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockCPUMEChannel;
import net.minecraft.util.ResourceLocation;

import static gregtech.common.metatileentities.MetaTileEntities.registerMetaTileEntity;

public class PrismPlanMetaTileEntities {

    private static int startId = 18900;

    public static MetaTileEntityMultiblockCPUMEChannel MULTIBLOCK_CPU_ME_CHANNEL;

    public static int getID()
    {
        return startId++;
    }
    public static void initialization() {
        PrismPlanLog.logger.info("Registering MetaTileEntities");

        MULTIBLOCK_CPU_ME_CHANNEL = registerMetaTileEntity(getID(),new MetaTileEntityMultiblockCPUMEChannel(new ResourceLocation("prismplan","multiblock_cpume_channel")));

    }
}
