package keqing.gtqt.prismplan.mixin;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.crafting.CraftingLink;
import appeng.me.cache.CraftingGridCache;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import keqing.gtqt.prismplan.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockCPUMEChannel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Set;

@Mixin(value = CraftingGridCache.class, remap = false)
public abstract class MixinCraftingGridCache {

    @Shadow @Final private IGrid grid;

    @Shadow @Final private Set<CraftingCPUCluster> craftingCPUClusters;

    @Shadow public abstract void addLink(CraftingLink link);

    @Inject(method = "updateCPUClusters()V", at = @At("RETURN"))
    private void injectUpdateCPUClusters(final CallbackInfo ci) {

        for (final IGridNode ecNode : grid.getMachines(MetaTileEntityHolder.class)) {

            final MetaTileEntityHolder ec = (MetaTileEntityHolder) ecNode.getMachine();
            MetaTileEntityMultiblockCPUMEChannel mte = (MetaTileEntityMultiblockCPUMEChannel) ec.getMetaTileEntity();
            final List<CraftingCPUCluster> cpus = mte.getCPUs();


            for (CraftingCPUCluster cpu : cpus) {
                this.craftingCPUClusters.add(cpu);

                if (cpu.getLastCraftingLink() != null) {
                    this.addLink((CraftingLink) cpu.getLastCraftingLink());
                }
            }
        }
    }

}
