package keqing.gtqt.prismplan.common.item.ae2.storage;

import appeng.api.storage.ICellWorkbenchItem;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEStack;
import net.minecraft.item.ItemStack;

public interface ISingularityStorageCell<T extends IAEStack<T>> extends ICellWorkbenchItem {

    boolean isBlackListed( ItemStack var1,  T var2);

    boolean storableInStorageCell();

    boolean isStorageCell( ItemStack var1);

    double getIdleDrain();


    IStorageChannel<T> getChannel();
}
