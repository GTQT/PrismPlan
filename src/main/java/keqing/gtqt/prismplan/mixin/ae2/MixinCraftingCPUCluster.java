package keqing.gtqt.prismplan.mixin.ae2;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.IActionSource;
import appeng.crafting.MECraftingInventory;
import appeng.me.cache.CraftingGridCache;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.helpers.MachineSource;
import appeng.tile.crafting.TileCraftingTile;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import keqing.gtqt.prismplan.api.capability.INetWorkCalculator;
import keqing.gtqt.prismplan.api.capability.Levels;
import keqing.gtqt.prismplan.api.utils.TimeRecorder;
import keqing.gtqt.prismplan.common.metatileentities.multi.multiblock.ecalculator.ECPUCluster;
import keqing.gtqt.prismplan.common.metatileentities.multi.multiblock.ecalculator.MetaTileEntityCalculatorControl;
import keqing.gtqt.prismplan.common.metatileentities.multi.multiblock.ecalculator.MetaTileEntityThreadHatch;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(value = CraftingCPUCluster.class, remap = false)
public abstract class MixinCraftingCPUCluster implements ECPUCluster {

    @Unique
    private MetaTileEntityThreadHatch prismplan_ec$core = null;

    @Unique
    private MetaTileEntityCalculatorControl prismplan_ec$virtualCPUOwner = null;

    @Unique
    private long prismplan_ec$usedExtraStorage = 0;

    @Unique
    private final TimeRecorder prismplan_ec$timeRecorder = new TimeRecorder();

    @Unique
    private final TimeRecorder prismplan_ec$parallelismRecorder = new TimeRecorder();

    @Shadow
    private long availableStorage;

    @Shadow
    private boolean isDestroyed;

    @Shadow
    private int accelerator;

    @Shadow
    private MECraftingInventory inventory;

    @Shadow
    private boolean isComplete;

    @Shadow
    private ICraftingLink myLastLink;

    @Shadow
    private MachineSource machineSrc;

    @Shadow
    public abstract void destroy();

    @Shadow
    public abstract void cancel();

    @Final
    @Shadow
    private int[] usedOps;

    @Inject(method = "submitJob", at = @At(value = "INVOKE", target = "Lappeng/api/networking/crafting/ICraftingJob;getOutput()Lappeng/api/storage/data/IAEItemStack;"))
    private void injectSubmitJob(final IGrid g, final ICraftingJob job, final IActionSource src, final ICraftingRequester requestingMachine, final CallbackInfoReturnable<ICraftingLink> cir) {
        if (this.prismplan_ec$virtualCPUOwner == null) {
            return;
        }
        this.prismplan_ec$virtualCPUOwner.onVirtualCPUSubmitJob(job.getByteTotal());
    }

    @Inject(method = "cancel", at = @At("RETURN"))
    private void injectCancel(final CallbackInfo ci) {
        if (this.prismplan_ec$core == null) {
            return;
        }
        // Ensure inventory is empty
        if (this.inventory.getItemList().isEmpty()) {
            destroy();
        }
    }

    @Inject(method = "updateCraftingLogic", at = @At("HEAD"), cancellable = true)
    private void injectUpdateCraftingLogicStoreItems(final IGrid grid, final IEnergyGrid eg, final CraftingGridCache cgc, final CallbackInfo ci) {
        if (this.prismplan_ec$core == null) {
            return;
        }
        if (this.myLastLink != null) {
            if (this.myLastLink.isCanceled()) {
                this.myLastLink = null;
                this.cancel();
            }
        }
        if (this.isComplete) {
            // Ensure inventory is empty
            if (this.inventory.getItemList().isEmpty()) {
                destroy();
                ci.cancel();
            }
        }
    }

    @Inject(method = "updateCraftingLogic", at = @At("TAIL"))
    private void injectUpdateCraftingLogicTail(final IGrid grid, final IEnergyGrid eg, final CraftingGridCache cgc, final CallbackInfo ci) {
        int currentParallelism = this.usedOps[0];
        prismplan_ec$parallelismRecorder.addUsedTime(currentParallelism);
    }

    @WrapOperation(
            method = "updateCraftingLogic",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/tile/crafting/TileCraftingTile;isActive()Z"
            )
    )
    private boolean redirectUpdateCraftingLogicIsActive(final TileCraftingTile instance, final Operation<Boolean> original) {
        if (this.prismplan_ec$core != null) {
            MetaTileEntityCalculatorControl controller = (MetaTileEntityCalculatorControl) this.prismplan_ec$core.getController();
            return controller != null && controller.getNetWorkCalculatorHatch() != null && controller.getNetWorkCalculatorHatch().getProxy().isActive();
        }
        if (this.prismplan_ec$virtualCPUOwner != null) {
            MetaTileEntityCalculatorControl controller = prismplan_ec$virtualCPUOwner;
            return controller.getNetWorkCalculatorHatch() != null && controller.getNetWorkCalculatorHatch().getProxy().isActive();
        }
        return original.call(instance);
    }

    @Inject(method = "destroy", at = @At("HEAD"), cancellable = true)
    private void injectDestroy(final CallbackInfo ci) {
        if (this.prismplan_ec$core == null) {
            return;
        }
        if (this.isDestroyed) {
            ci.cancel();
            return;
        }
        this.prismplan_ec$core.onCPUDestroyed((CraftingCPUCluster) (Object) this);
    }

    @Inject(method = "isActive", at = @At("HEAD"), cancellable = true)
    private void injectIsActive(final CallbackInfoReturnable<Boolean> cir) {
        if (this.prismplan_ec$core == null && this.prismplan_ec$virtualCPUOwner == null) {
            return;
        }
        if (this.prismplan_ec$core != null) {
            MetaTileEntityCalculatorControl controller = (MetaTileEntityCalculatorControl) this.prismplan_ec$core.getController();
            cir.setReturnValue(controller != null && controller.getNetWorkCalculatorHatch() != null && controller.getNetWorkCalculatorHatch().getProxy().isActive());
        }
        if (this.prismplan_ec$virtualCPUOwner != null) {
            MetaTileEntityCalculatorControl controller = prismplan_ec$virtualCPUOwner;
            cir.setReturnValue(controller.getNetWorkCalculatorHatch() != null && controller.getNetWorkCalculatorHatch().getProxy().isActive());
        }
    }

    @Inject(method = "getGrid", at = @At("HEAD"), cancellable = true)
    private void injectGetGrid(final CallbackInfoReturnable<IGrid> cir) {
        if (this.prismplan_ec$core != null) {
            final MetaTileEntityCalculatorControl controller = (MetaTileEntityCalculatorControl) this.prismplan_ec$core.getController();
            if (controller == null) {
                return;
            }
            final INetWorkCalculator channel = controller.getNetWorkCalculatorHatch();
            if (channel == null) {
                return;
            }
            IGridNode node = channel.getProxy().getNode();
            cir.setReturnValue(node == null ? null : node.getGrid());
        }
        if (this.prismplan_ec$virtualCPUOwner != null) {
            final INetWorkCalculator channel = prismplan_ec$virtualCPUOwner.getNetWorkCalculatorHatch();
            if (channel == null) {
                return;
            }
            IGridNode node = channel.getProxy().getNode();
            cir.setReturnValue(node == null ? null : node.getGrid());
        }
    }

    @Inject(method = "getCore", at = @At("HEAD"), cancellable = true)
    private void injectGetCore(final CallbackInfoReturnable<TileCraftingTile> cir) {
        if (this.prismplan_ec$core != null || this.prismplan_ec$virtualCPUOwner != null) {
            cir.setReturnValue(null);
        }
    }

    @Inject(method = "getWorld", at = @At("HEAD"), cancellable = true)
    private void injectGetWorld(final CallbackInfoReturnable<World> cir) {
        if (this.prismplan_ec$core != null) {
            cir.setReturnValue(this.prismplan_ec$core.getWorld());
        }
        if (this.prismplan_ec$virtualCPUOwner != null) {
            cir.setReturnValue(prismplan_ec$virtualCPUOwner.getWorld());
        }
    }

    @Inject(method = "markDirty", at = @At("HEAD"), cancellable = true)
    private void injectMarkDirty(final CallbackInfo ci) {
        if (this.prismplan_ec$core != null) {
            this.prismplan_ec$core.markDirty();
            ci.cancel();
        }
        if (this.prismplan_ec$virtualCPUOwner != null) {
            this.prismplan_ec$virtualCPUOwner.markDirty();
            ci.cancel();
        }
    }

    @Unique
    @Override
    public void prismplan_ec$setAvailableStorage(final long availableStorage) {
        this.availableStorage = availableStorage;
    }

    @Unique
    @Override
    public void prismplan_ec$setAccelerators(final int accelerators) {
        this.accelerator = accelerators;
    }

    @Unique
    @Override
    public MetaTileEntityThreadHatch prismplan_ec$getController() {
        return prismplan_ec$core;
    }

    @Unique
    @Override
    public void prismplan_ec$setThreadCore(final MetaTileEntityThreadHatch threadCore) {
        this.prismplan_ec$core = threadCore;

        final MetaTileEntityCalculatorControl controller = (MetaTileEntityCalculatorControl) threadCore.getController();
        if (controller == null) {
            return;
        }
        final INetWorkCalculator channel = controller.getNetWorkCalculatorHatch();
        if (channel != null) {
            this.machineSrc = new MachineSource(channel);
        }
    }

    @Unique
    @Override
    public void prismplan_ec$setVirtualCPUOwner(@Nullable final MetaTileEntityCalculatorControl virtualCPUOwner) {
        this.prismplan_ec$virtualCPUOwner = virtualCPUOwner;
        if (virtualCPUOwner == null) {
            return;
        }

        final INetWorkCalculator channel = virtualCPUOwner.getNetWorkCalculatorHatch();
        if (channel != null) {
            this.machineSrc = new MachineSource(channel);
        }
    }

    @Unique
    @Override
    public Levels prismplan_ec$getControllerLevel() {
        final MetaTileEntityCalculatorControl controller;
        if (this.prismplan_ec$core != null) {
            controller = (MetaTileEntityCalculatorControl) this.prismplan_ec$core.getController();
        } else if (this.prismplan_ec$virtualCPUOwner != null) {
            controller = this.prismplan_ec$virtualCPUOwner;
        } else {
            return null;
        }

        if (controller != null) {
            return controller.getLevel();
        }
        return null;
    }

    @Unique
    @Override
    public void prismplan_ec$setUsedExtraStorage(final long usedExtraStorage) {
        this.prismplan_ec$usedExtraStorage = usedExtraStorage;
    }

    @Unique
    @Override
    public long prismplan_ec$getUsedExtraStorage() {
        return prismplan_ec$usedExtraStorage;
    }

    @Unique
    @Override
    public void prismplan_ec$markDestroyed() {
        this.isDestroyed = true;
        this.isComplete = true;
    }

    @Unique
    @Override
    public TimeRecorder prismplan_ec$getTimeRecorder() {
        return prismplan_ec$timeRecorder;
    }

    @Unique
    @Override
    public TimeRecorder prismplan_ec$getParallelismRecorder() {
        return prismplan_ec$parallelismRecorder;
    }

}
