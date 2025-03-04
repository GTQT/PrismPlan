package keqing.gtqt.prismplan.mixin;

import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.helpers.MachineSource;
import keqing.gtqt.prismplan.common.metatileentities.multi.multiblockpart.IPrismPlanCPUCluster;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = CraftingCPUCluster.class, remap = false)
public class MixinCraftingCPUCluster implements IPrismPlanCPUCluster {

    @Shadow private MachineSource machineSrc;

    @Unique
    @Override
    public void prismPlan$setSource(MachineSource source) {
        this.machineSrc = source;
    }
}
