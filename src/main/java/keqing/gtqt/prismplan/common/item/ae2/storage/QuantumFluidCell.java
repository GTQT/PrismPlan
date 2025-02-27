package keqing.gtqt.prismplan.common.item.ae2.storage;

import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.core.Api;
import net.minecraft.item.ItemStack;

public class QuantumFluidCell extends QuantumCell<IAEFluidStack> {

    public QuantumFluidCell(long bytes) {
        super(bytes);
    }


    @Override
    public IFluidStorageChannel getChannel() {
        return Api.INSTANCE.storage().getStorageChannel(IFluidStorageChannel.class);
    }

    @Override
    public int getTotalTypes( ItemStack cellItem) {
        return 1;
    }

}
