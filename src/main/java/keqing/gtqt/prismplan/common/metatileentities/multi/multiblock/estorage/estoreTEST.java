package keqing.gtqt.prismplan.common.metatileentities.multi.multiblock.estorage;

import appeng.api.AEApi;
import appeng.api.implementations.tiles.IChestOrDrive;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkCellArrayUpdate;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.*;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.core.AELog;
import appeng.hooks.TickHandler;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.me.helpers.MachineSource;
import appeng.me.storage.DriveWatcher;
import appeng.tile.inventory.AppEngCellInventory;
import appeng.tile.storage.TileDrive;
import appeng.util.Platform;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.InvOperation;
import appeng.util.inv.filter.IAEItemFilter;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.AdvancedTextWidget;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import groovyjarjarantlr4.v4.runtime.misc.NotNull;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import keqing.gtqt.prismplan.api.utils.PrismPlanLog;
import keqing.gtqt.prismplan.common.item.ae2.estorage.EStorageCell;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class estoreTEST extends MultiblockWithDisplayBase implements IChestOrDrive,IAEAppEngInventory,ICellProvider  {

    private final AppEngCellInventory inv = new AppEngCellInventory(this, 10);
    private final ICellHandler[] handlersBySlot = new ICellHandler[10];
    private final ECellDriveWatcher<IAEItemStack>[] invBySlot = new ECellDriveWatcher[10];
    private final IActionSource mySrc = new MachineSource(this);
    private boolean isCached = false;
    private final Map<IStorageChannel<? extends IAEStack<?>>, List<IMEInventoryHandler>> inventoryHandlers;
    private int priority = 0;
    private boolean wasActive = false;
    private int cellState = 0;
    private boolean powered;
    private int blinking;
    private int meUpdateTick = 0;
    protected boolean isOnline;

    protected final IActionSource source = new MachineSource(this);
    public IActionSource getSource() {
        return source;
    }

    private AENetworkProxy networkProxy;
    public IItemHandler getInternalInventory() {
        return this.inv;
    }


    public estoreTEST(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        this.inv.setFilter(new CellValidInventoryFilter());
        this.inventoryHandlers = new IdentityHashMap();
    }

    @Override
    protected void updateFormedValid() {
        if (!this.getWorld().isRemote) {
            ++this.meUpdateTick;
        }
        if (!this.getWorld().isRemote && this.updateMEStatus() && this.shouldSyncME()) {
            this.syncME();
        }
    }

    private void syncME() {
        AENetworkProxy proxy = this.getProxy();
        if (proxy == null) return;

        try {
            IStorageGrid storageGrid = proxy.getStorage();
            storageGrid.registerCellProvider(this); // 注册驱动器本身
        } catch (GridAccessException e) {
            PrismPlanLog.logger.warn("Grid access failed", e);
        }
    }

    public int getCellCount() {
        return 10;
    }

    public int getCellStatus(int slot) {
        if (Platform.isClient()) {
            return this.cellState >> slot * 3 & 7;
        } else {
            ECellDriveWatcher handler = this.invBySlot[slot];
            return handler == null ? 0 : handler.getStatus();
        }
    }

    public boolean isPowered() {
        return Platform.isClient() ? this.powered : this.getProxy().isActive();
    }

    public boolean isCellBlinking(int slot) {
        return (this.blinking & 1 << slot) == 1;
    }

    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.isCached = false;
        this.priority = data.getInteger("priority");
    }

    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("priority", this.priority);
        return data;
    }

    @Nullable
    @Override
    public IGridNode getGridNode(AEPartLocation dir) {
        return this.getProxy().getNode();
    }

    public AECableType getCableConnectionType(AEPartLocation dir) {
        return AECableType.SMART;
    }

    @Override
    public void securityBreak() {

    }

    private boolean markDirtyQueued = false;

    public void saveChanges() {
        if (this.getWorld() != null && !this.getWorld().isRemote) {
            this.markDirty();
            if (!this.markDirtyQueued) {
                TickHandler.INSTANCE.addCallable(null, this::markDirtyAtEndOfTick);
                this.markDirtyQueued = true;
            }

        }
    }
    private Object markDirtyAtEndOfTick(World w) {
        this.markDirty();
        this.markDirtyQueued = false;
        return null;
    }
    public void onChangeInventory(IItemHandler inv, int slot, InvOperation mc, ItemStack removed, ItemStack added) {
        this.isCached = false;
        this.updateState();
        try {
            this.getProxy().getGrid().postEvent(new MENetworkCellArrayUpdate());
        } catch (GridAccessException ignored) {}
    }

    private void updateState() {
        if (!this.isCached) {
            inventoryHandlers.clear(); // 清除旧数据
            AEApi.instance().storage().storageChannels().forEach(channel ->
                    inventoryHandlers.put(channel, new ArrayList<>())
            );

            for(int x = 0; x < inv.getSlots(); x++) {
                ItemStack is = inv.getStackInSlot(x);
                invBySlot[x] = null;
                handlersBySlot[x] = null;
                if (!is.isEmpty()) {
                    ICellHandler handler = AEApi.instance().registries().cell().getHandler(is);
                    if (handler != null) {
                        handlersBySlot[x] = handler;
                        for (IStorageChannel<?> channel : inventoryHandlers.keySet()) {
                            ICellInventoryHandler cell = handler.getCellInventory(is, this, channel);
                            if (cell != null) {
                                ECellDriveWatcher<IAEItemStack> watcher = new ECellDriveWatcher<>(cell, is, handler, this);
                                ((List) inventoryHandlers.get(channel)).add(watcher);
                                invBySlot[x] = watcher;
                                break;
                            }
                        }
                    }
                }
            }
            this.isCached = true;
        }
    }

    public void onReady() {
        this.updateState();
    }

    public List<IMEInventoryHandler> getCellArray(IStorageChannel channel) {
        this.updateState();
        return (List)this.inventoryHandlers.get(channel);
    }

    public int getPriority() {
        return this.priority;
    }

    public void setPriority(int newValue) {
        this.priority = newValue;
        this.saveChanges();
        this.isCached = false;
        this.updateState();

        try {
            this.getProxy().getGrid().postEvent(new MENetworkCellArrayUpdate());
        } catch (GridAccessException var3) {
        }

    }

    public void blinkCell(int slot) {
        this.blinking |= 1 << slot;
    }

    public void saveChanges(ICellInventory<?> cellInventory) {
        this.markDirty();
    }

    public ItemStack getItemStackRepresentation() {
        return (ItemStack)AEApi.instance().definitions().blocks().drive().maybeStack(1).orElse(ItemStack.EMPTY);
    }



    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 180, 240);
        builder.dynamicLabel(28, 12, () -> "物品监控器", 0xFFFFFF);
        builder.widget(new SlotWidget(inv, 0, 8, 8, true, true)
                .setBackgroundTexture(GuiTextures.SLOT)
                .setTooltipText("输入槽位"));

        builder.image(4, 28, 172, 128, GuiTextures.DISPLAY);

        builder.bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 8, 160);
        return builder.build(this.getHolder(), entityPlayer);
    }


    public @Nullable AENetworkProxy getProxy() {
        if (this.networkProxy == null) {
            return this.networkProxy = this.createProxy();
        } else {
            if (!this.networkProxy.isReady() && this.getWorld() != null) {
                this.networkProxy.onReady();
            }

            return this.networkProxy;
        }
    }

    private @Nullable AENetworkProxy createProxy() {
        IGregTechTileEntity mte = this.getHolder();
        if (mte instanceof IGridProxyable holder) {
            AENetworkProxy proxy = new AENetworkProxy(holder, "mte_proxy", this.getStackForm(), true);
            proxy.setFlags(GridFlags.REQUIRE_CHANNEL);
            proxy.setIdlePowerUsage(ConfigHolder.compat.ae2.meHatchEnergyUsage);
            proxy.setValidSides(EnumSet.of(this.getFrontFacing()));
            return proxy;
        } else {
            return null;
        }
    }

    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);

        if (this.networkProxy != null) {
            buf.writeBoolean(true);
            NBTTagCompound proxy = new NBTTagCompound();
            this.networkProxy.writeToNBT(proxy);
            buf.writeCompoundTag(proxy);
        } else {
            buf.writeBoolean(false);
        }

        buf.writeInt(this.meUpdateTick);
        buf.writeBoolean(this.isOnline);
    }

    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);


        if (buf.readBoolean()) {
            NBTTagCompound nbtTagCompound;
            try {
                nbtTagCompound = buf.readCompoundTag();
            } catch (IOException var4) {
                nbtTagCompound = null;
            }

            if (this.networkProxy != null && nbtTagCompound != null) {
                this.networkProxy.readFromNBT(nbtTagCompound);
            }
        }

        this.meUpdateTick = buf.readInt();
        this.isOnline = buf.readBoolean();
    }

    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.UPDATE_ONLINE_STATUS) {
            boolean isOnline = buf.readBoolean();
            if (this.isOnline != isOnline) {
                this.isOnline = isOnline;
                this.scheduleRenderUpdate();
            }
        }

    }


    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.networkProxy = createProxy();
        if (this.networkProxy != null) {
            this.networkProxy.setValidSides(EnumSet.of(this.getFrontFacing()));
            this.networkProxy.onReady();
        }
        this.updateState();
    }


    public boolean updateMEStatus() {
        if (!this.getWorld().isRemote) {
            boolean isOnline = this.networkProxy != null && this.networkProxy.isActive() && this.networkProxy.isPowered();
            if (this.isOnline != isOnline) {
                this.writeCustomData(GregtechDataCodes.UPDATE_ONLINE_STATUS, (buf) -> {
                    buf.writeBoolean(isOnline);
                });
                this.isOnline = isOnline;
            }
        }

        return this.isOnline;
    }

    protected boolean shouldSyncME() {
        return this.meUpdateTick % 200 == 0;
    }

    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXX", "XXX", "XXX")
                .aisle("XXX", "XXX", "XXX")
                .aisle("XXX", "XSX", "XXX")
                .where('S', this.selfPredicate())
                .where('X', states(this.getCasingState()))
                .build();
    }

    private IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new estoreTEST(metaTileEntityId);
    }

    @Override
    public boolean hasMaintenanceMechanics() {
        return false;
    }

    @Override
    public boolean hasMufflerMechanics() {
        return false;
    }

    @Nonnull
    @Override
    public IGridNode getActionableNode() {
        return this.getProxy().getNode();
    }

    @Override
    public boolean canBeRotated() {
        return false;
    }

    @Override
    public EnumFacing getForward() {
        return this.frontFacing;
    }

    @Override
    public EnumFacing getUp() {
        return EnumFacing.UP;
    }

    @Override
    public void setOrientation(EnumFacing enumFacing, EnumFacing enumFacing1) {

    }

    private class CellValidInventoryFilter implements IAEItemFilter {
        private CellValidInventoryFilter() {
        }

        public boolean allowExtract(IItemHandler inv, int slot, int amount) {
            return true;
        }

        @Override
        public boolean allowInsert(IItemHandler inv, int slot, ItemStack stack) {
            return AEApi.instance().registries().cell().isCellHandled(stack);
        }
    }
}
