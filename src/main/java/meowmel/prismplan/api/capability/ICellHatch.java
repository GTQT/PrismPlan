package meowmel.prismplan.api.capability;

import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import meowmel.prismplan.common.metatileentities.multi.multiblock.estorage.ECellDriveWatcher;

public interface ICellHatch {
    default <T extends IAEStack<T>> IMEInventoryHandler<T> getHandler(final IStorageChannel<T> channel) {
        return null;
    }

    ECellDriveWatcher<IAEItemStack> getWatcher();

    ////////////////////////////////////////////
    //基本信息
    //信息
    EStorageCellData getData();
    //类型
    DriveStorageType getType();
    DriveStorageLevel getLevel();
    //容量
    long usedBytes();
    long maxBytes();
    //类型
    int usedTypes();
    int maxTypes();
}
