package keqing.gtqt.prismplan.api.capability;

import appeng.me.cluster.implementations.CraftingCPUCluster;

import java.util.List;

public interface IThreadHatch {
    List<CraftingCPUCluster> getCpus();

    boolean addCPU(final CraftingCPUCluster cluster, final boolean hyperThread);
    boolean canAddCPU();
    int getThreads();

    int getMaxThreads();

    int getMaxHyperThreads();

    void refreshCPUSource();
    long getUsedStorage();
}
