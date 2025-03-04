package keqing.gtqt.prismplan.api.capability;

import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import keqing.gtqt.prismplan.common.metatileentities.multi.multiblock.estorage.ECellDriveWatcher;

public interface ICellHatch {
    default <T extends IAEStack<T>> IMEInventoryHandler<T> getHandler(final IStorageChannel<T> channel) {
        return null;
    }

    ECellDriveWatcher<IAEItemStack> getWatcher();
}
