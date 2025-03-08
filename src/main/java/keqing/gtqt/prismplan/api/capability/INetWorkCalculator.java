package keqing.gtqt.prismplan.api.capability;

import appeng.api.networking.security.IActionHost;

public interface INetWorkCalculator extends INetWorkProxy, IActionHost {
    void postCPUClusterChangeEvent();
}
