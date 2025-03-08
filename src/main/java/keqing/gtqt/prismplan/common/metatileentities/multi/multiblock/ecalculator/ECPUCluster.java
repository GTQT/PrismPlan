package keqing.gtqt.prismplan.common.metatileentities.multi.multiblock.ecalculator;

import appeng.me.cluster.implementations.CraftingCPUCluster;
import keqing.gtqt.prismplan.api.capability.Levels;
import keqing.gtqt.prismplan.api.utils.TimeRecorder;

import javax.annotation.Nullable;

public interface ECPUCluster {

    static ECPUCluster from(final CraftingCPUCluster cluster) {
        return (ECPUCluster) (Object) cluster;
    }

    void novaeng_ec$setAvailableStorage(final long availableStorage);

    void novaeng_ec$setAccelerators(final int accelerators);

    MetaTileEntityThreadHatch novaeng_ec$getController();

    void novaeng_ec$setThreadCore(final MetaTileEntityThreadHatch threadCore);

    void novaeng_ec$setVirtualCPUOwner(@Nullable final MetaTileEntityCalculatorControl isVirtualCPUOwner);

    Levels novaeng_ec$getControllerLevel();

    long novaeng_ec$getUsedExtraStorage();

    void novaeng_ec$setUsedExtraStorage(final long usedExtraStorage);

    void novaeng_ec$markDestroyed();

    TimeRecorder novaeng_ec$getTimeRecorder();

    TimeRecorder novaeng_ec$getParallelismRecorder();

}
