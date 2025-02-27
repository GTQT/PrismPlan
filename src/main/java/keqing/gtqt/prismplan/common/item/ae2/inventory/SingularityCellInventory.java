package keqing.gtqt.prismplan.common.item.ae2.inventory;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.exceptions.AppEngException;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.ICellInventory;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.util.Platform;
import keqing.gtqt.prismplan.api.utils.AE2Values;
import keqing.gtqt.prismplan.api.utils.PrismPlanLog;
import keqing.gtqt.prismplan.common.item.ae2.storage.ISingularityStorageCell;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.IItemHandler;

import java.math.BigInteger;

public class SingularityCellInventory<T extends IAEStack<T>> implements ICellInventory<T> {

    private static final String KEY = "key";
    private static final String COUNT = "count";

    protected final ISingularityStorageCell<T> cellType;

    private final ItemStack itemStack;

    protected final ISaveProvider container;

    private final NBTTagCompound tagCompound;

    private T storedItem;

    private BigInteger count;

    private boolean isPersisted = true;

    public SingularityCellInventory(ISingularityStorageCell<T> cellType, ItemStack o,
                                    ISaveProvider container) {
        this.itemStack = o;
        this.container = container;
        this.cellType = cellType;
        this.tagCompound = Platform.openNbtData(o);
        this.loadCellItem();
    }

    public static ICellInventory<?> createInventory(ItemStack o, ISaveProvider container) {
        try {
            if (o == null) {
                throw new AppEngException("ItemStack was used as a cell, but was not a cell!");
            } else {
                Item type = o.getItem();
                if (type instanceof ISingularityStorageCell<?> cellType) {
                    if (!cellType.isStorageCell(o)) {
                        throw new AppEngException("ItemStack was used as a cell, but was not a cell!");
                    } else {
                        return new SingularityCellInventory<>(cellType, o, container);
                    }
                } else {
                    throw new AppEngException("ItemStack was used as a cell, but was not a cell!");
                }
            }
        } catch (AppEngException exception) {
            PrismPlanLog.logger.error(exception);
            return null;
        }
    }

    private void loadCellItem() {
        this.storedItem = tagCompound.hasKey(KEY) ? this.getChannel()
                .createFromNBT(tagCompound.getCompoundTag(KEY)) : null;
        this.count = tagCompound.hasKey(COUNT) ? new BigInteger(tagCompound.getByteArray(COUNT)): BigInteger.ZERO;

        if (this.storedItem != null) {
            this.storedItem.setCraftable(false);
        }
    }

    protected void saveChanges() {
        this.isPersisted = false;
        if (this.container != null) {
            this.container.saveChanges(this);
        } else {
            this.persist();
        }
    }

    @Override
    public void persist() {
        if (!this.isPersisted) {
            if (storedItem != null && count.signum() == 1) {
                var tag = new NBTTagCompound();
                storedItem.writeToNBT(tag);
                tagCompound.setTag(KEY, tag);
                tagCompound.setByteArray(COUNT, count.toByteArray());
            } else {
                tagCompound.removeTag(KEY);
                tagCompound.removeTag(COUNT);
            }

            this.isPersisted = true;
        }
    }

    @Override
    public T injectItems(T t, Actionable actionable, IActionSource iActionSource) {
        if (t == null) {
            return null;
        }

        if (t.getStackSize() == 0L) {
            return null;
        }

        if (this.cellType.isBlackListed(this.getItemStack(), t)) {
            return t;
        }

        if (this.storedItem != null && !t.equals(this.storedItem)){
            return t;
        }

        BigInteger insertAmount = BigInteger.valueOf(t.getStackSize());
        if (actionable == Actionable.MODULATE) {
            if (this.storedItem == null) {
                this.storedItem = t;
            }
            this.count = this.count.add(insertAmount);
            this.saveChanges();
        }
        return t.empty();
    }

    @Override
    public T extractItems(T t, Actionable actionable, IActionSource iActionSource) {
        if (t == null) {
            return null;
        }

        if (count.signum() < 1) {
            return t.empty();
        }

        if (storedItem == null) {
            return t.empty();
        }

        if (!t.equals(storedItem)) {
            return t.empty();
        }

        BigInteger extractAmount = BigInteger.valueOf(t.getStackSize());
        T results = t.copy();
        if (count.compareTo(extractAmount) <= 0) {
            results.setStackSize(count.longValue());
            if (actionable == Actionable.MODULATE) {
                storedItem = null;
                count = BigInteger.ZERO;
                saveChanges();
            }
        } else {
            if (actionable == Actionable.MODULATE) {
                count = count.subtract(extractAmount);
                saveChanges();
            }
        }
        return results;
    }

    @Override
    public IItemList<T> getAvailableItems(IItemList<T> iItemList) {
        if (storedItem != null) {
            iItemList.add(storedItem.setStackSize(getStoredItemCount()));
        }
        return iItemList;
    }

    @Override
    public IStorageChannel<T> getChannel() {
        return cellType.getChannel();
    }

    @Override
    public ItemStack getItemStack() {
        return itemStack;
    }

    @Override
    public double getIdleDrain() {
        return cellType.getIdleDrain();
    }

    @Override
    public FuzzyMode getFuzzyMode() {
        return cellType.getFuzzyMode(itemStack);
    }

    @Override
    public IItemHandler getConfigInventory() {
        return cellType.getConfigInventory(itemStack);
    }

    @Override
    public IItemHandler getUpgradesInventory() {
        return cellType.getUpgradesInventory(itemStack);
    }

    @Override
    public int getBytesPerType() {
        return 1;
    }

    @Override
    public boolean canHoldNewItem() {
        return storedItem == null;
    }

    @Override
    public long getTotalBytes() {
        return Long.MAX_VALUE;
    }

    @Override
    public long getFreeBytes() {
        return this.getTotalBytes() - this.getUsedBytes();
    }

    @Override
    public long getUsedBytes() {
        return this.storedItem != null ? 1 : 0;
    }

    @Override
    public long getTotalItemTypes() {
        return 1;
    }

    @Override
    public long getStoredItemCount() {
        return count.min(AE2Values.LONG_MAX).longValue();
    }

    @Override
    public long getStoredItemTypes() {
        return storedItem != null ? 1 : 0;
    }

    @Override
    public long getRemainingItemTypes() {
        return getTotalItemTypes() - getStoredItemTypes();
    }

    @Override
    public long getRemainingItemCount() {
        return Long.MAX_VALUE;
    }

    @Override
    public int getUnusedItemCount() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getStatusForCell() {
        if (this.getUsedBytes() == 0L) {
            return 4;
        } else if (this.canHoldNewItem()) {
            return 1;
        } else {
            return this.getRemainingItemCount() > 0L ? 2 : 3;
        }
    }
}
