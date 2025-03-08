package keqing.gtqt.prismplan.mixin.ae2;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.crafting.CraftingLink;
import appeng.me.cache.CraftingGridCache;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import keqing.gtqt.prismplan.PrismPlan;
import keqing.gtqt.prismplan.api.utils.TimeRecorder;
import keqing.gtqt.prismplan.common.metatileentities.multi.multiblock.ecalculator.ECPUCluster;
import keqing.gtqt.prismplan.common.metatileentities.multi.multiblock.ecalculator.MetaTileEntityNetWorkCalculatorHatch;
import keqing.gtqt.prismplan.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockCPUMEChannel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Set;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(value = CraftingGridCache.class, remap = false)
public abstract class MixinCraftingGridCache {

    @Shadow
    @Final
    private IGrid grid;

    @Shadow
    @Final
    private Set<CraftingCPUCluster> craftingCPUClusters;

    @Shadow
    public abstract void addLink(final CraftingLink link);

    @Inject(method = "updateCPUClusters()V", at = @At("RETURN"))
    private void injectUpdateCPUClusters(final CallbackInfo ci) {
        PrismPlan.LOGGER.info("updateCPUClusters");

        for (final IGridNode ecNode : grid.getMachines(MetaTileEntityHolder.class)) {

            final MetaTileEntityHolder ec = (MetaTileEntityHolder) ecNode.getMachine();
            if (!(ec.getMetaTileEntity() instanceof MetaTileEntityNetWorkCalculatorHatch)) {
                continue;
            }
            MetaTileEntityNetWorkCalculatorHatch mte = (MetaTileEntityNetWorkCalculatorHatch) ec.getMetaTileEntity();
            final List<CraftingCPUCluster> cpus = mte.getCPUs();

            for (CraftingCPUCluster cpu : cpus) {
                this.craftingCPUClusters.add(cpu);

                if (cpu.getLastCraftingLink() != null) {
                    this.addLink((CraftingLink) cpu.getLastCraftingLink());
                }
            }
        }
    }

    @WrapOperation(method = "onUpdateTick", at = @At(value = "INVOKE", target = "Lappeng/me/cluster/implementations/CraftingCPUCluster;updateCraftingLogic(Lappeng/api/networking/IGrid;Lappeng/api/networking/energy/IEnergyGrid;Lappeng/me/cache/CraftingGridCache;)V"))
    private void wrapOnUpdateTick(final CraftingCPUCluster instance, final IGrid grid, final IEnergyGrid eg, final CraftingGridCache cc, final Operation<Void> original) {
        ECPUCluster ec = ECPUCluster.from(instance);
        if (ec.novaeng_ec$getController() != null) {
            TimeRecorder recorder = ec.novaeng_ec$getTimeRecorder();
            final long start = System.nanoTime() / 1000;
            original.call(instance, grid, eg, cc);
            recorder.addUsedTime((int) (System.nanoTime() / 1000 - start));
        } else {
            original.call(instance, grid, eg, cc);
        }
    }

}
