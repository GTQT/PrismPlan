package keqing.gtqt.prismplan.common.metatileentities.multi.multiblock.ecalculator;

import appeng.me.cluster.implementations.CraftingCPUCluster;
import keqing.gtqt.prismplan.api.capability.Levels;
import keqing.gtqt.prismplan.api.utils.TimeRecorder;

import javax.annotation.Nullable;

public interface ECPUCluster {

    static ECPUCluster from(final CraftingCPUCluster cluster) {
        return (ECPUCluster) (Object) cluster;
    }

    void prismplan_ec$setAvailableStorage(final long availableStorage);

    void prismplan_ec$setAccelerators(final int accelerators);

    MetaTileEntityThreadHatch prismplan_ec$getController();

    void prismplan_ec$setThreadCore(final MetaTileEntityThreadHatch threadCore);

    void prismplan_ec$setVirtualCPUOwner(@Nullable final MetaTileEntityCalculatorControl isVirtualCPUOwner);

    Levels prismplan_ec$getControllerLevel();

    long prismplan_ec$getUsedExtraStorage();

    void prismplan_ec$setUsedExtraStorage(final long usedExtraStorage);

    void prismplan_ec$markDestroyed();

    TimeRecorder prismplan_ec$getTimeRecorder();

    TimeRecorder prismplan_ec$getParallelismRecorder();

}
