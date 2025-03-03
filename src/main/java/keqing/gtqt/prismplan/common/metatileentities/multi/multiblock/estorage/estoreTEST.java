package keqing.gtqt.prismplan.common.metatileentities.multi.multiblock.estorage;

import appeng.api.AEApi;
import appeng.api.implementations.tiles.IChestOrDrive;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkCellArrayUpdate;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.*;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.hooks.TickHandler;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.me.helpers.MachineSource;
import appeng.tile.inventory.AppEngCellInventory;
import appeng.util.Platform;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.InvOperation;
import appeng.util.inv.filter.IAEItemFilter;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
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
import keqing.gtqt.prismplan.api.capability.INetWorkProxy;
import keqing.gtqt.prismplan.api.multiblock.PrismPlanMultiblockAbility;
import keqing.gtqt.prismplan.api.utils.PrismPlanLog;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;

public class estoreTEST extends MultiblockWithDisplayBase implements IChestOrDrive, IAEAppEngInventory, ICellProvider {


    private final AppEngCellInventory inv = new AppEngCellInventory(this, 10);
    private final ICellHandler[] handlersBySlot = new ICellHandler[10];
    private final ECellDriveWatcher<IAEItemStack>[] invBySlot = new ECellDriveWatcher[10];
    private final Map<IStorageChannel<? extends IAEStack<?>>, List<IMEInventoryHandler>> inventoryHandlers;

    private boolean isCached = false;
    private int priority = 0;
    private final int cellState = 0;
    private int blinking;
    private boolean markDirtyQueued = false;

    public estoreTEST(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        this.inv.setFilter(new CellValidInventoryFilter());
        this.inventoryHandlers = new IdentityHashMap();
    }


    public IItemHandler getInternalInventory() {
        return this.inv;
    }

    @Override
    protected void updateFormedValid() {
        if (getNetWorkProxyHatch().couldSyncME()) {
            this.syncME();
        }
    }

    private void syncME() {
        AENetworkProxy proxy = this.getNetWorkProxyHatch().getProxy();
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
        if(!getNetWorkProxyHatch().couldUse())return false;
        return this.getNetWorkProxyHatch().getProxy().isActive();
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
        if(!getNetWorkProxyHatch().canBeUse()) return null;
        return this.getNetWorkProxyHatch().getProxy().getNode();
    }

    public AECableType getCableConnectionType(AEPartLocation dir) {
        return AECableType.SMART;
    }

    @Override
    public void securityBreak() {

    }

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
            this.getNetWorkProxyHatch().getProxy().getGrid().postEvent(new MENetworkCellArrayUpdate());
        } catch (GridAccessException ignored) {
        }
    }

    private void updateState() {
        if (!this.isCached) {
            inventoryHandlers.clear(); // 清除旧数据
            AEApi.instance().storage().storageChannels().forEach(channel ->
                    inventoryHandlers.put(channel, new ArrayList<>())
            );

            for (int x = 0; x < inv.getSlots(); x++) {
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
                                inventoryHandlers.get(channel).add(watcher);
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
        return this.inventoryHandlers.get(channel);
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
            this.getNetWorkProxyHatch().getProxy().getGrid().postEvent(new MENetworkCellArrayUpdate());
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
        return AEApi.instance().definitions().blocks().drive().maybeStack(1).orElse(ItemStack.EMPTY);
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

    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXX", "XXX", "XXX")
                .aisle("XXX", "XXX", "XXX")
                .aisle("XXX", "XSX", "XXX")
                .where('S', this.selfPredicate())
                .where('X', states(this.getCasingState())
                        .or(abilities(PrismPlanMultiblockAbility.NETWORK_PROXY).setExactLimit(1)))
                .build();
    }

    public INetWorkProxy getNetWorkProxyHatch() {
        List<INetWorkProxy> abilities = getAbilities(PrismPlanMultiblockAbility.NETWORK_PROXY);
        if (abilities.isEmpty())
            return null;
        return abilities.get(0);
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
        return this.getNetWorkProxyHatch().getProxy().getNode();
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
