package keqing.gtqt.prismplan.common.item.ae2.storage;

import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.core.Api;

public class UniverseFluidCell extends UniverseCell<IAEFluidStack> {

    public UniverseFluidCell() {
        super();
    }

    @Override
    public IStorageChannel<IAEFluidStack> getChannel() {
        return Api.INSTANCE.storage().getStorageChannel(IFluidStorageChannel.class);
    }

}
