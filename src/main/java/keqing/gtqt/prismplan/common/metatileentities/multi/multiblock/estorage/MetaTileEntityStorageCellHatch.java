package keqing.gtqt.prismplan.common.metatileentities.multi.multiblock.estorage;

import appeng.api.AEApi;
import appeng.api.networking.events.MENetworkCellArrayUpdate;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.*;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.tile.inventory.AppEngCellInventory;
import appeng.util.helpers.ItemHandlerUtil;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.InvOperation;
import appeng.util.inv.filter.IAEItemFilter;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import keqing.gtqt.prismplan.PrismPlan;
import keqing.gtqt.prismplan.api.capability.ICellHatch;
import keqing.gtqt.prismplan.api.capability.INetWorkProxy;
import keqing.gtqt.prismplan.api.multiblock.PrismPlanMultiblockAbility;
import keqing.gtqt.prismplan.common.item.ae2.estorage.EStorageCell;
import keqing.gtqt.prismplan.common.network.PktCellDriveStatusUpdate;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static appeng.helpers.ItemStackHelper.stackFromNBT;
import static appeng.helpers.ItemStackHelper.stackWriteToNBT;

public class MetaTileEntityStorageCellHatch extends MetaTileEntityMultiblockPart
        implements IMultiblockAbilityPart<ICellHatch>,ICellHatch,ISaveProvider, IAEAppEngInventory {

    //接口可能不全，使用到了请自行补全

    //这是网络仓，用于多方块访问网络的仓口
    //使用在多方块的文件内加入
    //    public ICellHatch getCellHatch() {
    //        List<ICellHatch> abilities = getAbilities(PrismPlanMultiblockAbility.CELL_HATCH);
    //        if (abilities.isEmpty())
    //            return null;
    //        return abilities.get(0);
    //    }

    protected final AppEngCellInventory driveInv = new AppEngCellInventory(this, 1);
    protected final Map<IStorageChannel<? extends IAEStack<?>>, IMEInventoryHandler<?>> inventoryHandlers = new Reference2ObjectOpenHashMap<>();

    protected EStorageCellHandler cellHandler = null;
    protected ECellDriveWatcher<IAEItemStack> watcher = null;

    protected boolean isCached = false;

    protected long lastWriteTick = 0;
    protected boolean writing = false;


    int tier;
    public MetaTileEntityStorageCellHatch(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
        this.driveInv.setFilter(CellInvFilter.INSTANCE);
    }

    public void update() {
        if (!this.getWorld().isRemote && isFirstTick()) {
            this.isCached = false;
        }
        if(this.getController()==null)return;
        long totalWorldTime = this.getWorld().getTotalWorldTime();
        if(this.getController().isStructureFormed()) {
            boolean changed = false;
            if (totalWorldTime - lastWriteTick >= 40) {
                if (writing) {
                    writing = false;
                    changed = true;
                }
            } else if (!writing) {
                writing = true;
                changed = true;
            }
            if (cellHandler == null) {
                return;
            }
            // Static update or changed update.
            if (this.getWorld().getTotalWorldTime() % 200 == 0) {
                BlockPos pos = getPos();
                PrismPlan.NET_CHANNEL.sendToAllTracking(
                        new PktCellDriveStatusUpdate(getPos(), writing),
                        new NetworkRegistry.TargetPoint(
                                this.getWorld().provider.getDimension(),
                                pos.getX(), pos.getY(), pos.getZ(),
                                -1)
                );
            } else if (changed) {
                BlockPos pos = getPos();
                PrismPlan.NET_CHANNEL.sendToAllAround(
                        new PktCellDriveStatusUpdate(getPos(), writing),
                        new NetworkRegistry.TargetPoint(
                                this.getWorld().provider.getDimension(),
                                pos.getX(), pos.getY(), pos.getZ(),
                                16)
                );
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends IAEStack<T>> IMEInventoryHandler<T> getHandler(final IStorageChannel<T> channel) {
        updateHandler(false);
        if (driveInv.getStackInSlot(0).getItem() instanceof EStorageCell<?>) {
            IMEInventoryHandler<?> handler = inventoryHandlers.get(channel);

            return handler == null ? null : (IMEInventoryHandler<T>) handler;
        }
//        if (driveInv.getStackInSlot(0).getItem() instanceof EStorageCell<?> cell && isCellSupported(cell.getLevel())) {
//            IMEInventoryHandler<?> handler = inventoryHandlers.get(channel);
//            return handler == null ? null : (IMEInventoryHandler<T>) handler;
//        }
        return null;
    }

    public boolean isCellSupported(int level) {
        return this.tier >= level;
    }
    @Override
    public void saveChanges(@Nullable final ICellInventory<?> cellInventory) {
        saveChanges();
    }

    @Override
    public void saveChanges() {
        markDirty();
    }

    @Override
    public void onChangeInventory(final IItemHandler inv, final int slot, final InvOperation mc, final ItemStack removed, final ItemStack added) {
        this.isCached = false; // recalculate the storage cell.
        this.updateHandler(true);
        this.markDirty();

        MultiblockControllerBase controller = getController();
        if (controller == null) {
            return;
        }

        if(controller instanceof MetaTileEntityStorageCellControl mte) {

            INetWorkProxy channel = mte.getNetWorkStoreHatch();
            AENetworkProxy proxy = channel.getProxy();
            IActionSource source = channel.getSource();

            try {
                if (proxy.isActive()) {
                    final IStorageGrid gs = proxy.getStorage();
                    postChanges(gs, removed, added, source);
                }
                proxy.getGrid().postEvent(new MENetworkCellArrayUpdate());
            } catch (final GridAccessException ignored) {
            }
        }

    }
    @SuppressWarnings("unchecked")
    public void postChanges(final IStorageGrid gs, final ItemStack removed, final ItemStack added, final IActionSource src) {
        if (cellHandler == null) {
            return;
        }
        for (final IStorageChannel<?> chan : AEApi.instance().storage().storageChannels()) {
            final IItemList<?> myChanges = chan.createList();

            if (!removed.isEmpty()) {
                final IMEInventory myInv = cellHandler.getCellInventory(removed, null, chan);
                if (myInv != null) {
                    myInv.getAvailableItems(myChanges);
                    for (final IAEStack is : myChanges) {
                        is.setStackSize(-is.getStackSize());
                    }
                }
            }
            if (!added.isEmpty()) {
                final IMEInventory myInv = cellHandler.getCellInventory(added, null, chan);
                if (myInv != null) {
                    myInv.getAvailableItems(myChanges);
                }
            }
            gs.postAlterationOfStoredItems(chan, myChanges, src);
        }
    }
    protected void updateHandler(final boolean refreshState) {
        if (isCached) {
            return;
        }
        watcher = null;
        cellHandler = null;
        inventoryHandlers.clear();
        isCached = true;
        ItemStack stack = driveInv.getStackInSlot(0);
        if (stack.isEmpty()) {
            updateDriveBlockState();
            return;
        }
        if ((cellHandler = EStorageCellHandler.getHandler(stack)) == null) {
            return;
        }

        MultiblockControllerBase controller = getController();
        if (controller == null) {
            return;
        }

        if(controller instanceof MetaTileEntityStorageCellControl mte) {
            ICellInventoryHandler cellInventory = null;
            final Collection<IStorageChannel<? extends IAEStack<?>>> storageChannels = AEApi.instance().storage().storageChannels();
            for (final IStorageChannel<? extends IAEStack<?>> channel : storageChannels) {
                cellInventory = cellHandler.getCellInventory(stack, this, channel);
                if (cellInventory == null) {
                    continue;
                }
                driveInv.setHandler(0, cellInventory);
                watcher = new ECellDriveWatcher<>(cellInventory, channel, this);
                watcher.setPriority(mte.getNetWorkStoreHatch().getPriority());
                inventoryHandlers.put(channel, watcher);
                break;
            }
            if (mte != null) {
                mte.recalculateEnergyUsage();
            }

            if (cellInventory == null || !refreshState) {
                return;
            }
            updateDriveBlockState();
        }
    }

    public void updateDriveBlockState() {
        if (this.getWorld() == null) {
            return;
        }
        this.markDirty();
    }


    public AppEngCellInventory getDriveInv() {
        return driveInv;
    }

    public ECellDriveWatcher<IAEItemStack> getWatcher() {
        return watcher;
    }

    public void onWriting() {
        this.lastWriteTick = getWorld().getTotalWorldTime();
    }

    public boolean isWriting() {
        return writing;
    }

    public void setWriting(final boolean writing) {
        this.writing = writing;
    }

    @Override
    public boolean hasCapability(@Nonnull final Capability<?> capability, @Nullable final EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull final Capability<T> capability, @Nullable final EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(driveInv);
        }
        return super.getCapability(capability, facing);
    }

    public NBTTagCompound writeToNBT(NBTTagCompound data) {

        final NBTTagCompound opt = new NBTTagCompound();
        for (int x = 0; x < driveInv.getSlots(); x++) {
            final NBTTagCompound itemNBT = new NBTTagCompound();
            final ItemStack is = driveInv.getStackInSlot(x);
            if (!is.isEmpty()) {
                stackWriteToNBT(is, itemNBT);
            }
            opt.setTag("item" + x, itemNBT);
        }
        data.setTag("driveInv", opt);

        return super.writeToNBT(data);
    }

    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);

        final NBTTagCompound opt = data.getCompoundTag("driveInv");
        for (int x = 0; x < driveInv.getSlots(); x++) {
            final NBTTagCompound item = opt.getCompoundTag("item" + x);
            ItemHandlerUtil.setStackInSlot(driveInv, x, stackFromNBT(item));
        }
    }


    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityStorageCellHatch(metaTileEntityId,tier);
    }

    @Override
    public MultiblockAbility<ICellHatch> getAbility() {
        return PrismPlanMultiblockAbility.CELL_HATCH;
    }

    @Override
    public void registerAbilities(List<ICellHatch> abilityList) {
        abilityList.add(this);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 180, 240);
        builder.dynamicLabel(28, 12, () -> "硬盘槽位", 0xFFFFFF);
        builder.widget(new SlotWidget(driveInv, 0, 8, 8, true, true)
                .setBackgroundTexture(GuiTextures.SLOT)
                .setTooltipText("输入硬盘"));

        builder.image(4, 28, 172, 128, GuiTextures.DISPLAY);

        builder.bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 8, 160);
        return builder.build(this.getHolder(), entityPlayer);
    }

    private static class CellInvFilter implements IAEItemFilter {
        private static final CellInvFilter INSTANCE = new CellInvFilter();
        @Override
        public boolean allowExtract(IItemHandler inv, int slot, int amount) {
            return true;
        }
        @Override
        public boolean allowInsert(IItemHandler inv, int slot, ItemStack stack) {
            return !stack.isEmpty() && EStorageCellHandler.getHandler(stack) != null;
        }
    }
}
