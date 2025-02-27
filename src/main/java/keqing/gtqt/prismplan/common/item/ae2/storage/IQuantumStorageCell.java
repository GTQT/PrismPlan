package keqing.gtqt.prismplan.common.item.ae2.storage;

import appeng.api.storage.ICellWorkbenchItem;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEStack;
import net.minecraft.item.ItemStack;

public interface IQuantumStorageCell<T extends IAEStack<T>> extends ICellWorkbenchItem {

    long getBytes( ItemStack var1);

    int getBytesPerType( ItemStack var1);

    int getTotalTypes( ItemStack var1);

    boolean isBlackListed( ItemStack var1,  T var2);

    boolean storableInStorageCell();

    boolean isStorageCell( ItemStack var1);

    double getIdleDrain();

    IStorageChannel<T> getChannel();

}
