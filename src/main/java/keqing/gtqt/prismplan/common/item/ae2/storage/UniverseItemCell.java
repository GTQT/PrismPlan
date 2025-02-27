package keqing.gtqt.prismplan.common.item.ae2.storage;

import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.Api;

public class UniverseItemCell extends UniverseCell<IAEItemStack> {

    public UniverseItemCell() {
        super();
    }

    @Override
    public IStorageChannel<IAEItemStack> getChannel() {
        return Api.INSTANCE.storage().getStorageChannel(IItemStorageChannel.class);
    }

}
