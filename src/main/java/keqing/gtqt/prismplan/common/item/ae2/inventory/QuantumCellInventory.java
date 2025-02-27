package keqing.gtqt.prismplan.common.item.ae2.inventory;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.exceptions.AppEngException;
import appeng.api.implementations.items.IStorageCell;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.ICellInventory;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.core.AEConfig;
import appeng.me.storage.AbstractCellInventory;
import appeng.me.storage.BasicCellInventory;
import keqing.gtqt.prismplan.PrismPlan;
import keqing.gtqt.prismplan.api.utils.PrismPlanLog;
import keqing.gtqt.prismplan.common.item.ae2.storage.IQuantumStorageCell;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.IItemHandler;

public class QuantumCellInventory<T extends IAEStack<T>> extends AbstractCellInventory<T> {

    private final IStorageChannel<T> channel;

    private final IQuantumStorageCell<T> type;

    private QuantumCellInventory(IQuantumStorageCell<T> type, ItemStack o, ISaveProvider container) {
        super(new Adapter<>(type), o, container);
        this.type = type;
        this.channel = cellType.getChannel();
    }

    public static ICellInventory<?> createInventory(ItemStack o, ISaveProvider container) {
        try {
            if (o == null) {
                throw new AppEngException("ItemStack was used as a cell, but was not a cell!");
            } else {
                Item type = o.getItem();
                if (type instanceof IQuantumStorageCell<?> cellType) {
                    if (!cellType.isStorageCell(o)) {
                        throw new AppEngException("ItemStack was used as a cell, but was not a cell!");
                    } else {
                        return new QuantumCellInventory<>(cellType, o, container);
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

    private boolean isQuantumStorageCell(T input) {
        if (!(input instanceof IAEItemStack stack)) {
            return false;
        } else {
            IQuantumStorageCell<?> type = getStorageCell(stack.getDefinition());
            return type != null && !type.storableInStorageCell();
        }
    }

    private boolean isStorageCell(T input) {
        if (!(input instanceof IAEItemStack stack)) {
            return false;
        } else {
            return BasicCellInventory.isCell(stack.createItemStack());
        }
    }

    private static IQuantumStorageCell<?> getStorageCell(ItemStack input) {
        if (input != null) {
            Item type = input.getItem();
            if (type instanceof IQuantumStorageCell) {
                return (IQuantumStorageCell<?>) type;
            }
        }

        return null;
    }

    private static <T extends IAEStack<T>> boolean isCellEmpty(ICellInventory<T> inv) {
        return inv == null || inv.getAvailableItems(inv.getChannel().createList()).isEmpty();
    }

    @Override
    public T injectItems(T input, Actionable mode, IActionSource src) {
        if (input == null) {
            return null;
        } else if (input.getStackSize() == 0L) {
            return null;
        } else if (cellType.isBlackListed(getItemStack(), input)) {
            return input;
        } else {
            if (isStorageCell(input)) {
                return input;
            }

            if (isQuantumStorageCell(input)) {
                ICellInventory<?> meInventory = createInventory(((IAEItemStack) input).createItemStack(), null);
                if (!isCellEmpty(meInventory)) {
                    return input;
                }
            }

            T l = getCellItems().findPrecise(input);
            long remainingItemCount;
            T toReturn;
            if (l != null) {
                remainingItemCount = getRemainingItemCount();
                if (remainingItemCount <= 0L) {
                    return input;
                } else if (input.getStackSize() > remainingItemCount) {
                    toReturn = input.copy();
                    toReturn.setStackSize(toReturn.getStackSize() - remainingItemCount);
                    if (mode == Actionable.MODULATE) {
                        l.setStackSize(l.getStackSize() + remainingItemCount);
                        saveChanges();
                    }

                    return toReturn;
                } else {
                    if (mode == Actionable.MODULATE) {
                        l.setStackSize(l.getStackSize() + input.getStackSize());
                        saveChanges();
                    }

                    return null;
                }
            } else {
                if (canHoldNewItem()) {
                    remainingItemCount = getRemainingItemCount() - (long) getBytesPerType() * (long) itemsPerByte;
                    if (remainingItemCount > 0L) {
                        if (input.getStackSize() > remainingItemCount) {
                            toReturn = input.copy();
                            toReturn.setStackSize(input.getStackSize() - remainingItemCount);
                            if (mode == Actionable.MODULATE) {
                                T toWrite = input.copy();
                                toWrite.setStackSize(remainingItemCount);
                                cellItems.add(toWrite);
                                saveChanges();
                            }

                            return toReturn;
                        }

                        if (mode == Actionable.MODULATE) {
                            cellItems.add(input);
                            saveChanges();
                        }

                        return null;
                    }
                }

                return input;
            }
        }
    }

    @Override
    public T extractItems(T request, Actionable mode, IActionSource src) {
        if (request == null) {
            return null;
        } else {
            long size = Math.min(2147483647L, request.getStackSize());
            T Results = null;
            T l = getCellItems().findPrecise(request);
            if (l != null) {
                Results = l.copy();
                if (l.getStackSize() <= size) {
                    Results.setStackSize(l.getStackSize());
                    if (mode == Actionable.MODULATE) {
                        l.setStackSize(0L);
                        saveChanges();
                    }
                } else {
                    Results.setStackSize(size);
                    if (mode == Actionable.MODULATE) {
                        l.setStackSize(l.getStackSize() - size);
                        saveChanges();
                    }
                }
            }

            return Results;
        }
    }

    @Override
    public long getTotalBytes() {
        return type.getBytes(getItemStack());
    }

    @Override
    public IStorageChannel<T> getChannel() {
        return channel;
    }

    @Override
    protected boolean loadCellItem(NBTTagCompound compoundTag, long stackSize) {
        T t;
        try {
            t = getChannel().createFromNBT(compoundTag);
            if (t == null) {
                PrismPlanLog.logger.warn("Removing item " + compoundTag + " from storage cell because the associated item type couldn't be found.", new Object[0]);
                return false;
            }
        } catch (Throwable exception) {
            if (AEConfig.instance().isRemoveCrashingItemsOnLoad()) {
                PrismPlanLog.logger.warn("Removing item " + compoundTag + " from storage cell because loading the ItemStack crashed.", exception);
                return false;
            }

            throw exception;
        }

        t.setStackSize(stackSize);
        t.setCraftable(false);
        if (stackSize > 0L) {
            cellItems.add(t);
        }

        return true;
    }

    private static class Adapter<T extends IAEStack<T>> implements IStorageCell<T> {
        private final IQuantumStorageCell<T> cellType;

        public Adapter(IQuantumStorageCell<T> type) {
            cellType = type;
        }

        @Override
        public int getBytes( ItemStack itemStack) {
            PrismPlanLog.logger.warn("A method that should not have been called was called.");
            return (int) cellType.getBytes(itemStack);
        }

        @Override
        public int getBytesPerType( ItemStack itemStack) {
            return cellType.getBytesPerType(itemStack);
        }

        @Override
        public int getTotalTypes( ItemStack itemStack) {
            return cellType.getTotalTypes(itemStack);
        }

        @Override
        public boolean isBlackListed( ItemStack itemStack,  T t) {
            return cellType.isBlackListed(itemStack, t);
        }

        @Override
        public boolean storableInStorageCell() {
            return cellType.storableInStorageCell();
        }

        @Override
        public boolean isStorageCell( ItemStack itemStack) {
            return cellType.isStorageCell(itemStack);
        }

        @Override
        public double getIdleDrain() {
            return cellType.getIdleDrain();
        }


        @Override
        public IStorageChannel<T> getChannel() {
            return cellType.getChannel();
        }

        @Override
        public boolean isEditable(ItemStack itemStack) {
            return cellType.isEditable(itemStack);
        }

        @Override
        public IItemHandler getUpgradesInventory(ItemStack itemStack) {
            return cellType.getUpgradesInventory(itemStack);
        }

        @Override
        public IItemHandler getConfigInventory(ItemStack itemStack) {
            return cellType.getConfigInventory(itemStack);
        }

        @Override
        public FuzzyMode getFuzzyMode(ItemStack itemStack) {
            return cellType.getFuzzyMode(itemStack);
        }

        @Override
        public void setFuzzyMode(ItemStack itemStack, FuzzyMode fuzzyMode) {
            cellType.setFuzzyMode(itemStack, fuzzyMode);
        }
    }
}
