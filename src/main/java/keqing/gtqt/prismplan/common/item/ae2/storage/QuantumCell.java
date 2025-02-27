package keqing.gtqt.prismplan.common.item.ae2.storage;

import appeng.api.AEApi;
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
import keqing.gtqt.prismplan.api.utils.AE2Values;
import keqing.gtqt.prismplan.common.item.ae2.inventory.QuantumCellInventory;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;

import java.util.List;
import java.util.Set;

public abstract class QuantumCell<T extends IAEStack<T>> extends AEBaseItem implements IQuantumStorageCell<T>, IItemGroup {

    protected final long totalBytes;

    public QuantumCell(long bytes) {
        this.setMaxStackSize(1);
        this.totalBytes = bytes;
    }

    @SideOnly(Side.CLIENT)
    public void addCheckedInformation(ItemStack stack, World world, List<String> lines,
                                      ITooltipFlag advancedTooltips) {
        AEApi.instance().client().addCellInformation(AEApi.instance().registries().cell().getCellInventory(stack,
                null, this.getChannel()), lines);
    }

    @Override
    public long getBytes( ItemStack cellItem) {
        return this.totalBytes;
    }

    @Override
    public int getTotalTypes( ItemStack cellItem) {
        return 63;
    }

    @Override
    public boolean isBlackListed( ItemStack cellItem,  T requestedAddition) {
        return false;
    }

    @Override
    public boolean storableInStorageCell() {
        return false;
    }

    @Override
    public boolean isStorageCell( ItemStack i) {
        return true;
    }

    @Override
    public String getUnlocalizedGroupName(Set<ItemStack> others, ItemStack is) {
        return GuiText.StorageCells.getUnlocalized();
    }

    @Override
    public boolean isEditable(ItemStack is) {
        return true;
    }

    @Override
    public IItemHandler getUpgradesInventory(ItemStack is) {
        return new CellUpgrades(is, 2);
    }

    @Override
    public IItemHandler getConfigInventory(ItemStack is) {
        return new CellConfig(is);
    }

    @Override
    public FuzzyMode getFuzzyMode(ItemStack is) {
        var fz = Platform.openNbtData(is).getString("FuzzyMode");

        try {
            return FuzzyMode.valueOf(fz);
        } catch (Throwable var4) {
            return FuzzyMode.IGNORE_ALL;
        }
    }

    @Override
    public void setFuzzyMode(ItemStack is, FuzzyMode fzMode) {
        Platform.openNbtData(is).setString("FuzzyMode", fzMode.name());
    }

    @Override
    public int getBytesPerType( ItemStack itemStack) {
        long value = this.totalBytes / 128;
        return value >= Integer.MAX_VALUE ? Integer.MAX_VALUE - 1 : (int) value;
    }

    @Override
    public double getIdleDrain() {
        return Math.round(Math.log(this.totalBytes) / AE2Values.LOG2);
    }

    public static class Handler implements ICellHandler {

        @Override
        public boolean isCell(ItemStack itemStack) {
            return itemStack.getItem() instanceof QuantumCell;
        }

        @Override
        public <T extends IAEStack<T>> ICellInventoryHandler<T> getCellInventory(ItemStack itemStack, ISaveProvider iSaveProvider, IStorageChannel<T> iStorageChannel) {
            if (!itemStack.isEmpty() && itemStack.getItem() instanceof QuantumCell<?> quantumCell) {
                if (quantumCell.getChannel().getClass().isInstance(iStorageChannel)) {
                    return new BasicCellInventoryHandler<>(QuantumCellInventory.createInventory(itemStack, iSaveProvider), iStorageChannel);
                }
            }
            return null;
        }
    }
}
