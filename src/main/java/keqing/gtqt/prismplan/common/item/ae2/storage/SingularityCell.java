package keqing.gtqt.prismplan.common.item.ae2.storage;

import appeng.api.config.FuzzyMode;
import appeng.api.implementations.items.IItemGroup;
import appeng.api.storage.ICellHandler;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.core.localization.GuiText;
import appeng.items.AEBaseItem;
import appeng.items.contents.CellConfig;
import appeng.items.contents.CellUpgrades;
import appeng.me.storage.BasicCellInventoryHandler;
import appeng.util.Platform;
import keqing.gtqt.prismplan.common.item.ae2.inventory.SingularityCellInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import java.util.Set;

public abstract class SingularityCell<T extends IAEStack<T>> extends AEBaseItem implements ISingularityStorageCell<T>, IItemGroup {

    public SingularityCell() {
    }

    @Override
    public String getUnlocalizedGroupName(Set<ItemStack> others, ItemStack is) {
        return GuiText.StorageCells.getUnlocalized();
    }

    @Override
    public boolean isBlackListed( ItemStack var1,  T var2) {
        return false;
    }

    @Override
    public boolean storableInStorageCell() {
        return false;
    }

    @Override
    public boolean isStorageCell( ItemStack var1) {
        return true;
    }

    @Override
    public double getIdleDrain() {
        return 25;
    }

    @Override
    public boolean isEditable(ItemStack itemStack) {
        return true;
    }

    @Override
    public IItemHandler getUpgradesInventory(ItemStack itemStack) {
        return new CellUpgrades(itemStack, 2);
    }

    @Override
    public IItemHandler getConfigInventory(ItemStack itemStack) {
        return new CellConfig(itemStack);
    }

    @Override
    public FuzzyMode getFuzzyMode(ItemStack itemStack) {
        var fz = Platform.openNbtData(itemStack).getString("FuzzyMode");

        try {
            return FuzzyMode.valueOf(fz);
        } catch (Throwable var4) {
            return FuzzyMode.IGNORE_ALL;
        }
    }

    @Override
    public void setFuzzyMode(ItemStack itemStack, FuzzyMode fuzzyMode) {
        Platform.openNbtData(itemStack).setString("FuzzyMode", fuzzyMode.name());
    }

    public static class Handler implements ICellHandler {

        @Override
        public boolean isCell(ItemStack itemStack) {
            return itemStack.getItem() instanceof SingularityCell;
        }

        @Override
        public <T extends IAEStack<T>> ICellInventoryHandler<T> getCellInventory(ItemStack itemStack, ISaveProvider iSaveProvider, IStorageChannel<T> iStorageChannel) {
            if (!itemStack.isEmpty() && itemStack.getItem() instanceof SingularityCell<?> singularityCell) {
                if (singularityCell.getChannel().getClass().isInstance(iStorageChannel)) {
                    return new BasicCellInventoryHandler<>(SingularityCellInventory.createInventory(itemStack, iSaveProvider), iStorageChannel);
                }
            }
            return null;
        }
    }
}
