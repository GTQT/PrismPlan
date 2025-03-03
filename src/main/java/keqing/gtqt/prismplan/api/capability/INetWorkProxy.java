package keqing.gtqt.prismplan.api.capability;

import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.util.AEPartLocation;
import appeng.me.helpers.AENetworkProxy;

public interface INetWorkProxy {
    AENetworkProxy getProxy();
    IActionSource getSource();
    IGridNode getGridNode(final AEPartLocation dir);
    boolean couldSyncME();
    boolean couldUse();
    boolean canBeUse();
}
