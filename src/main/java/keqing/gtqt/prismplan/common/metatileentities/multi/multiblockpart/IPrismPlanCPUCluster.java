package keqing.gtqt.prismplan.common.metatileentities.multi.multiblockpart;

import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.helpers.MachineSource;

public interface IPrismPlanCPUCluster {

    static IPrismPlanCPUCluster from(final CraftingCPUCluster cluster) {
        return (IPrismPlanCPUCluster) (Object) cluster;
    }
    void prismPlan$setSource(MachineSource source);
}
