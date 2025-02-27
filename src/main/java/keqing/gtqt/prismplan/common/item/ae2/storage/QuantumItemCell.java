package keqing.gtqt.prismplan.common.item.ae2.storage;

import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.Api;
import net.minecraft.item.ItemStack;

public class QuantumItemCell extends QuantumCell<IAEItemStack> {

    public QuantumItemCell(long bytes) {
        super(bytes);
    }


    @Override
    public IItemStorageChannel getChannel() {
        return Api.INSTANCE.storage().getStorageChannel(IItemStorageChannel.class);
    }

    @Override
    public int getTotalTypes( ItemStack cellItem) {
        return 1;
    }

}
