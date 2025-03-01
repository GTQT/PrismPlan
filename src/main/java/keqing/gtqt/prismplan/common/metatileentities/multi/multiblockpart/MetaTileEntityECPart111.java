package keqing.gtqt.prismplan.common.metatileentities.multi.multiblockpart;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.GridFlags;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.BaseActionSource;
import appeng.me.helpers.IGridProxyable;
import appeng.me.helpers.MachineSource;
import appeng.util.item.AEItemStack;
import appeng.util.item.AEStack;
import codechicken.lib.raytracer.CuboidRayTraceResult;
import gregtech.api.capability.*;
import gregtech.api.capability.impl.*;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.GhostCircuitSlotWidget;
import gregtech.api.gui.widgets.ImageCycleButtonWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.gui.widgets.TankWidget;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.util.GTHashMaps;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockNotifiablePart;
import groovyjarjarantlr4.v4.runtime.misc.NotNull;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import keqing.gtqt.prismplan.api.capability.IItemAndFluidHandler;
import keqing.gtqt.prismplan.api.multiblock.PrismPlanMultiblockAbility;
import keqing.gtqt.prismplan.api.utils.PrismPlanLog;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;

public class MetaTileEntityECPart111 extends MetaTileEntityMultiblockNotifiablePart implements IMultiblockAbilityPart<IItemAndFluidHandler>, IControllable, IGhostSlotConfigurable {

    protected IItemHandlerModifiable targetItem;
    protected FluidTankList targetFluids;
    protected IItemHandlerModifiable extraItem;


    private final int tankSize;
    protected @Nullable GhostCircuitItemStackHandler circuitInventory;
    protected boolean isOnline;
    private IItemAndFluidHandler FluidAndItemStore;
    private boolean workingEnabled;
    private IItemHandlerModifiable actualImportItems;
    private AENetworkProxy networkProxy;
    private int meUpdateTick = 0;

    public MetaTileEntityECPart111(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, 4, false);
        this.tankSize = 16000;




        this.initializeInventory();
    }

    protected void initializeInventory() {

        FluidTank[] fluidsHandlers = new FluidTank[9];
        List<FluidTank> tankslist = new ArrayList<>();
        for (int i = 0; i < 9; ++i) {
            fluidsHandlers[i] = new NotifiableFluidTank(this.tankSize, this, false);
            tankslist.add(fluidsHandlers[i]);
        }

        this.targetItem = new NotifiableItemStackHandler(this, 9, null, false) {

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return !(stack.getItem() instanceof ICraftingPatternItem);
            }
        };
        this.extraItem = new NotifiableItemStackHandler(this, 7, null, false) {
            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return !(stack.getItem() instanceof ICraftingPatternItem);
            }
        };
        this.targetFluids = new FluidTankList(false, IntStream.range(0, 9)
                .mapToObj(i -> new FluidTank(16000))
                .toArray(IFluidTank[]::new));

        this.circuitInventory = new GhostCircuitItemStackHandler(this);
        this.circuitInventory.addNotifiableMetaTileEntity(this);

        var list = Arrays.asList(this.createNewImportItemHandler(), this.circuitInventory, this.extraItem);
        this.FluidAndItemStore = new ItemAndFluidHandler(false, tankslist, list);
        this.actualImportItems = this.FluidAndItemStore;

        this.importFluids = this.createImportFluidHandler();
        this.exportFluids = this.createExportFluidHandler();
        this.fluidInventory = new FluidHandlerProxy(this.importFluids, this.exportFluids);
        this.importItems = this.createImportItemHandler();
        this.exportItems = this.createExportItemHandler();
        this.itemInventory = new ItemHandlerProxy(this.importItems, this.exportItems);
    }

    public IItemHandlerModifiable getImportItems() {
        return this.actualImportItems == null ? super.getImportItems() : this.FluidAndItemStore;
    }

    public void addToMultiBlock(MultiblockControllerBase controllerBase) {
        super.addToMultiBlock(controllerBase);
        if (this.hasGhostCircuitInventory() && this.actualImportItems instanceof ItemHandlerList) {
            Iterator var2 = ((ItemHandlerList) this.actualImportItems).getBackingHandlers().iterator();

            while (var2.hasNext()) {
                IItemHandler handler = (IItemHandler) var2.next();
                if (handler instanceof INotifiableHandler notifiable) {
                    notifiable.addNotifiableMetaTileEntity(controllerBase);
                    notifiable.addToNotifiedList(this, handler, this.isExportHatch);
                }
            }
        }

    }

    public void removeFromMultiBlock(MultiblockControllerBase controllerBase) {
        super.removeFromMultiBlock(controllerBase);
        if (this.hasGhostCircuitInventory() && this.actualImportItems instanceof ItemHandlerList) {
            Iterator var2 = ((ItemHandlerList) this.actualImportItems).getBackingHandlers().iterator();

            while (var2.hasNext()) {
                IItemHandler handler = (IItemHandler) var2.next();
                if (handler instanceof INotifiableHandler notifiable) {
                    notifiable.removeNotifiableMetaTileEntity(controllerBase);
                }
            }
        }
    }

    protected FluidTankList createImportFluidHandler() {
        return this.isExportHatch ? new FluidTankList(false) : (FluidTankList) this.FluidAndItemStore;
    }

    protected FluidTankList createExportFluidHandler() {
        return this.isExportHatch ? (FluidTankList) this.FluidAndItemStore : new FluidTankList(false);
    }

    private int getInventorySize() {
        return 9;
    }

    protected IItemHandlerModifiable createExportItemHandler() {
        return this.isExportHatch ?
                this.FluidAndItemStore
                :
                new GTItemStackHandler(this, 0);
    }

    protected IItemHandlerModifiable createImportItemHandler() {
        return this.isExportHatch ?
                new GTItemStackHandler(this, 0)
                :
                this.FluidAndItemStore;
    }

    protected IItemHandlerModifiable createNewExportItemHandler() {
        return this.isExportHatch ?
                new NotifiableItemStackHandler(this, this.getInventorySize(), this.getController(), true)
                :
                new GTItemStackHandler(this, 0);
    }

    protected IItemHandlerModifiable createNewImportItemHandler() {
        return this.isExportHatch ?
                new GTItemStackHandler(this, 0)
                :
                new NotifiableItemStackHandler(this, this.getInventorySize(), this.getController(), false);
    }

    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data.setBoolean("workingEnabled", this.workingEnabled);

        GTUtility.writeItems(this.targetItem, "targetItem", data);
        //GTUtility.writeItems(this.outputItem, "outputItem", data);
        GTUtility.writeItems(this.extraItem, "extraItem", data);

        data.setTag("targetFluids", this.targetFluids.serializeNBT());
        //data.setTag("outputsFluids", this.outputsFluids.serializeNBT());

        if (this.circuitInventory != null) {
            this.circuitInventory.write(data);
        }

        return super.writeToNBT(data);
    }

    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);

        this.workingEnabled = data.getBoolean("workingEnabled");

        GTUtility.readItems(this.targetItem, "targetItem", data);
        //GTUtility.readItems(this.outputItem, "outputItem", data);
        GTUtility.readItems(this.extraItem, "extraItem", data);

        this.targetFluids.deserializeNBT(data.getCompoundTag("targetFluids"));
        //this.outputsFluids.deserializeNBT(data.getCompoundTag("outputsFluids"));

        if (this.circuitInventory != null) {
            this.circuitInventory.read(data);
        }
    }

    @Override
    public void onRemoval() {
        super.onRemoval();
        if (this.networkProxy != null) {
            this.networkProxy.invalidate();
        }
        for (int i = 0; i < targetItem.getSlots(); i++) {
            var pos = getPos();
            if (!targetItem.getStackInSlot(i).isEmpty()) {
                getWorld().spawnEntity(new EntityItem(getWorld(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, targetItem.getStackInSlot(i)));
                targetItem.extractItem(i, 1, false);
            }
        }
        for (int i = 0; i < FluidAndItemStore.getSlots(); i++) {
            var pos = getPos();
            if (!FluidAndItemStore.getStackInSlot(i).isEmpty()) {
                getWorld().spawnEntity(new EntityItem(getWorld(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, FluidAndItemStore.getStackInSlot(i)));
                FluidAndItemStore.extractItem(i, 1, false);
            }
        }
        for (int i = 0; i < extraItem.getSlots(); i++) {
            var pos = getPos();
            if (!extraItem.getStackInSlot(i).isEmpty()) {
                getWorld().spawnEntity(new EntityItem(getWorld(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, extraItem.getStackInSlot(i)));
                extraItem.extractItem(i, 1, false);
            }
        }
    }


    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176, 209)
                .label(10, 5, this.getMetaFullName())
                .bindPlayerInventory(entityPlayer.inventory, 126);
        builder.dynamicLabel(10, 15, () -> this.isOnline ? I18n.format("gregtech.gui.me_network.online") : I18n.format("gregtech.gui.me_network.offline"), 4210752);

        for (int i = 0; i < 9; i++) {
            builder.widget(new SlotWidget(this.targetItem, i, 7 + 18 * i, 20, true, true, true)
                    .setBackgroundTexture(GuiTextures.SLOT)
                    .setChangeListener(this::markDirty));

            builder.widget(new SlotWidget(this.FluidAndItemStore, i, 7 + 18 * i, 40, true, true, true)
                    .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.ARROW_OUTPUT_OVERLAY)
                    .setChangeListener(this::markDirty));

            builder.widget(new TankWidget(this.targetFluids.getTankAt(i), 7 + 18 * i, 60, 18, 18)
                    .setBackgroundTexture(GuiTextures.FLUID_SLOT)
                    .setContainerClicking(true, true)
                    .setAlwaysShowFull(true));

            builder.widget(new TankWidget(this.FluidAndItemStore.getTankAt(i), 7 + 18 * i, 80, 18, 18)
                    .setBackgroundTexture(GuiTextures.FLUID_SLOT)
                    .setOverlayTexture(GuiTextures.ARROW_OUTPUT_OVERLAY)
                    .setContainerClicking(true, true)
                    .setAlwaysShowFull(true));
        }
        for (int i = 0; i < 7; i++) {
            builder.widget(new SlotWidget(this.extraItem, i, 7 + 18 * i, 100, true, true, true)
                    .setBackgroundTexture(GuiTextures.SLOT)
                    .setChangeListener(this::markDirty));
        }

        SlotWidget circuitSlot = (new GhostCircuitSlotWidget(this.circuitInventory, 0, 7 + 18 * 7, 100)).setBackgroundTexture(GuiTextures.SLOT, this.getCircuitSlotOverlay());
        builder.widget(circuitSlot.setConsumer(this::getCircuitSlotTooltip));

        builder.widget(new ImageCycleButtonWidget(7 + 18 * 8, 100, 18, 18, GuiTextures.BUTTON_POWER, this::isWorkingEnabled, this::setWorkingEnabled));


        return builder.build(this.getHolder(), entityPlayer);
    }

    protected AEStack<?> getAEStack(int solt) {
        return AEItemStack.fromItemStack(targetItem.getStackInSlot(solt));
    }

    public void update() {
        super.update();

        if (!this.getWorld().isRemote && this.workingEnabled) {
            if (this.isExportHatch) {
                this.pushFluidsIntoNearbyHandlers(this.getFrontFacing());
                this.pushItemsIntoNearbyHandlers(this.getFrontFacing());
            } else {
                this.pullFluidsFromNearbyHandlers(this.getFrontFacing());
                this.pullItemsFromNearbyHandlers(this.getFrontFacing());
            }
        }

        if (!this.getWorld().isRemote) {
            ++this.meUpdateTick;
        }
        if (!this.getWorld().isRemote && workingEnabled && this.updateMEStatus() && this.shouldSyncME()) {
            for (int i = 0; i < 9; i++) {
                this.syncME(i);
                this.syncFluidME(i); // 新增流体同步
            }

        }

    }

    protected boolean shouldSyncME() {
        return this.meUpdateTick % ConfigHolder.compat.ae2.updateIntervals == 0;
    }

    public void syncME(int i) {
        AENetworkProxy proxy = this.getProxy();
        if (proxy == null) return;

        try {
            IItemStorageChannel channel = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
            IMEMonitor<IAEItemStack> monitor = proxy.getStorage().getInventory(channel);

            if (targetItem.getStackInSlot(i) == ItemStack.EMPTY) {
                handleEmptyTarget(i, monitor);
            } else {
                IAEStack<?> stack = this.getAEStack(i);
                if (stack == null) return;

                if (stack instanceof IAEItemStack itemStack) {
                    handleNonEmptyTarget(i, monitor, itemStack);
                }
            }

        } catch (GridAccessException e) {
            // 网络不可用，记录日志
            PrismPlanLog.logger.warn("Grid access failed", e);
        } catch (Exception e) {
            // 其他异常，记录日志
            PrismPlanLog.logger.warn("Unexpected error occurred", e);
        }
    }

    private void handleEmptyTarget(int i, IMEMonitor<IAEItemStack> monitor) {
        ItemStack outputStack = FluidAndItemStore.getStackInSlot(i);
        if (!outputStack.isEmpty()) {
            injectItemsIntoNetwork(monitor, outputStack, i);
        }
    }

    private void handleNonEmptyTarget(int i, IMEMonitor<IAEItemStack> monitor, IAEItemStack itemStack) {
        ItemStack outputStack = FluidAndItemStore.getStackInSlot(i);
        if (!outputStack.isEmpty() && !ItemStack.areItemStacksEqual(outputStack, targetItem.getStackInSlot(i))) {
            injectItemsIntoNetwork(monitor, outputStack, i);
        }

        extractItemsFromNetwork(monitor, itemStack, i);
    }

    private void injectItemsIntoNetwork(IMEMonitor<IAEItemStack> monitor, ItemStack outputStack, int i) {
        IAEItemStack aeOutput = AEItemStack.fromItemStack(outputStack);
        if (aeOutput != null) {
            IAEItemStack remaining = monitor.injectItems(aeOutput, Actionable.MODULATE, getActionSource());

            if (remaining != null && remaining.getStackSize() > 0) {
                int inserted = Math.max(0, outputStack.getCount() - (int) remaining.getStackSize());
                FluidAndItemStore.extractItem(i, inserted, false);
            } else {
                FluidAndItemStore.extractItem(i, outputStack.getCount(), false);
            }
        }
    }

    private void extractItemsFromNetwork(IMEMonitor<IAEItemStack> monitor, IAEItemStack itemStack, int i) {
        long available = monitor.getStorageList().findPrecise(itemStack) != null ?
                monitor.getStorageList().findPrecise(itemStack).getStackSize() : 0;
        if (available > 0) {
            long toExtract = Math.min(64, available);
            IAEItemStack request = itemStack.copy().setStackSize(toExtract);

            IAEItemStack simulated = monitor.extractItems(request, Actionable.SIMULATE, getActionSource());
            if (simulated != null && simulated.getStackSize() >= toExtract) {
                IAEItemStack extracted = monitor.extractItems(request, Actionable.MODULATE, getActionSource());
                if (extracted != null) {
                    ItemStack physicalStack = extracted.createItemStack();
                    ItemStack remaining = FluidAndItemStore.insertItem(i, physicalStack, false);

                    if (!remaining.isEmpty()) {
                        IAEItemStack leftOver = AEItemStack.fromItemStack(remaining);
                        monitor.injectItems(leftOver, Actionable.MODULATE, getActionSource());
                    }
                }
            }
        }
    }


    private void syncFluidME(int i) {
        AENetworkProxy proxy = this.getProxy();
        if (proxy == null) return;

        try {
            IFluidStorageChannel channel = AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class);
            IMEMonitor<IAEFluidStack> monitor = proxy.getStorage().getInventory(channel);

            IFluidTank targetTank = targetFluids.getTankAt(i);
            IFluidTank exportTank = FluidAndItemStore.getTankAt(i);

            // 处理目标流体为空的情况
            if (targetTank.getFluid() == null) {
                handleEmptyFluidTarget(monitor, exportTank);
            } else {
                handleNonEmptyFluidTarget(monitor, targetTank, exportTank);
            }

        } catch (GridAccessException e) {
            PrismPlanLog.logger.warn("Fluid grid access failed", e);
        } catch (Exception e) {
            PrismPlanLog.logger.warn("Unexpected fluid error", e);
        }
    }

    // 处理空目标流体情况
    private void handleEmptyFluidTarget(IMEMonitor<IAEFluidStack> monitor, IFluidTank exportTank) {
        FluidStack exportFluid = exportTank.getFluid();
        if (exportFluid != null) {
            IAEFluidStack aeFluid = AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class)
                    .createStack(exportFluid);
            if (aeFluid != null) {
                IAEFluidStack remaining = monitor.injectItems(aeFluid, Actionable.MODULATE, getActionSource());
                if (remaining != null) {
                    exportTank.drain((int) (aeFluid.getStackSize() - remaining.getStackSize()), true);
                } else {
                    exportTank.drain(exportFluid.amount, true);
                }
            }
        }
    }

    // 处理非空目标流体情况
    private void handleNonEmptyFluidTarget(IMEMonitor<IAEFluidStack> monitor,
                                           IFluidTank targetTank, IFluidTank exportTank) {
        // 注入输出储罐的流体
        FluidStack exportFluid = exportTank.getFluid();
        if (exportFluid != null && !exportFluid.isFluidEqual(targetTank.getFluid())) {
            IAEFluidStack aeFluid = AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class)
                    .createStack(exportFluid);
            if (aeFluid != null) {
                IAEFluidStack remaining = monitor.injectItems(aeFluid, Actionable.MODULATE, getActionSource());
                if (remaining != null) {
                    exportTank.drain((int) (aeFluid.getStackSize() - remaining.getStackSize()), true);
                } else {
                    exportTank.drain(exportFluid.amount, true);
                }
            }
        }

        // 从网络提取流体
        IAEFluidStack request = AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class)
                .createStack(targetTank.getFluid());


        if (request != null) {
            request.setStackSize(Math.min(64000, exportTank.getCapacity() - exportTank.getFluidAmount()));

            IAEFluidStack simulated = monitor.extractItems(request, Actionable.SIMULATE, getActionSource());
            if (simulated != null) {
                IAEFluidStack extracted = monitor.extractItems(request, Actionable.MODULATE, getActionSource());
                if (extracted != null) {
                    int filled = exportTank.fill(extracted.getFluidStack(), true);
                    if (filled < extracted.getStackSize()) {
                        IAEFluidStack leftOver = extracted.copy().setStackSize(extracted.getStackSize() - filled);
                        monitor.injectItems(leftOver, Actionable.MODULATE, getActionSource());
                    }
                }
            }
        }
    }

    protected IActionSource getActionSource() {
        IGregTechTileEntity var2 = this.getHolder();
        if (var2 instanceof IActionHost holder) {
            return new MachineSource(holder);
        } else {
            return new BaseActionSource();
        }
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

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityECPart111(metaTileEntityId);
    }

    @Override
    public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return true;
        else if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
            return true;

        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(FluidAndItemStore);
        else if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(FluidAndItemStore);
        return super.getCapability(capability, side);
    }

    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(this.workingEnabled);

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
        this.workingEnabled = buf.readBoolean();

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

    public @NotNull AECableType getCableConnectionType(@NotNull AEPartLocation part) {
        return part.getFacing() != this.frontFacing ? AECableType.NONE : AECableType.SMART;
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

    public boolean isWorkingEnabled() {
        return this.workingEnabled;
    }

    public void setWorkingEnabled(boolean workingEnabled) {
        this.workingEnabled = workingEnabled;
        World world = this.getWorld();
        if (world != null && !world.isRemote) {
            this.writeCustomData(GregtechDataCodes.WORKING_ENABLED, (buf) -> {
                buf.writeBoolean(workingEnabled);
            });
        }

    }

    public boolean onSoftMalletClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        IControllable controllable = this.getCapability(GregtechTileCapabilities.CAPABILITY_CONTROLLABLE, null);
        if (controllable != null) {
            controllable.setWorkingEnabled(!controllable.isWorkingEnabled());
            if (!this.getWorld().isRemote) {
                playerIn.sendStatusMessage(new TextComponentTranslation(controllable.isWorkingEnabled() ? "behaviour.soft_hammer.enabled" : "behaviour.soft_hammer.disabled"), true);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean hasGhostCircuitInventory() {
        return true;
    }

    protected TextureArea getCircuitSlotOverlay() {
        return GuiTextures.INT_CIRCUIT_OVERLAY;
    }

    protected void getCircuitSlotTooltip(@NotNull SlotWidget widget) {
        String configString;
        if (this.circuitInventory != null && this.circuitInventory.getCircuitValue() != -1) {
            configString = String.valueOf(this.circuitInventory.getCircuitValue());
        } else {
            configString = (new TextComponentTranslation("gregtech.gui.configurator_slot.no_value")).getFormattedText();
        }
        widget.setTooltipText("gregtech.gui.configurator_slot.tooltip", configString);
    }

    @Override
    public void setGhostCircuitConfig(int config) {
        if (this.circuitInventory != null && this.circuitInventory.getCircuitValue() != config) {
            this.circuitInventory.setCircuitValue(config);
            if (!this.getWorld().isRemote) {
                this.markDirty();
            }

        }
    }

    @Override
    public MultiblockAbility<IItemAndFluidHandler> getAbility() {
        return PrismPlanMultiblockAbility.PRISMPLAN_AE;
    }

    @Override
    public void registerAbilities(List<IItemAndFluidHandler> list) {
        list.add(this.FluidAndItemStore);
    }

    private class ItemAndFluidHandler extends FluidTankList implements IItemAndFluidHandler, IItemHandlerModifiable {
        private final Int2ObjectMap<IItemHandler> handlerBySlotIndex = new Int2ObjectOpenHashMap();
        private final Map<IItemHandler, Integer> baseIndexOffset = new IdentityHashMap();

        public ItemAndFluidHandler(boolean allowSameFluidFill, @NotNull List<? extends IFluidTank> fluidTanks, List<? extends IItemHandler> itemHandlerList) {
            super(allowSameFluidFill, fluidTanks);
            int currentSlotIndex = 0;

            int slotsCount;
            for (Iterator var3 = itemHandlerList.iterator(); var3.hasNext(); currentSlotIndex += slotsCount) {
                IItemHandler itemHandler = (IItemHandler) var3.next();
                if (this.baseIndexOffset.containsKey(itemHandler)) {
                    throw new IllegalArgumentException("Attempted to add item handler " + itemHandler + " twice");
                }

                this.baseIndexOffset.put(itemHandler, currentSlotIndex);
                slotsCount = itemHandler.getSlots();

                for (int slotIndex = 0; slotIndex < slotsCount; ++slotIndex) {
                    this.handlerBySlotIndex.put(currentSlotIndex + slotIndex, itemHandler);
                }
            }
        }

        public int getSlots() {
            return this.handlerBySlotIndex.size();
        }

        public void setStackInSlot(int slot, @NotNull ItemStack stack) {
            IItemHandler itemHandler = this.handlerBySlotIndex.get(slot);
            if (!(itemHandler instanceof IItemHandlerModifiable)) {
                throw new UnsupportedOperationException("Handler " + itemHandler + " does not support this method");
            } else {
                ((IItemHandlerModifiable) itemHandler).setStackInSlot(slot - this.baseIndexOffset.get(itemHandler), stack);
            }
        }

        public @NotNull ItemStack getStackInSlot(int slot) {
            IItemHandler itemHandler = this.handlerBySlotIndex.get(slot);
            int var10000 = slot - this.baseIndexOffset.get(itemHandler);
            return itemHandler.getStackInSlot(slot - this.baseIndexOffset.get(itemHandler));
        }

        public int getSlotLimit(int slot) {
            IItemHandler itemHandler = this.handlerBySlotIndex.get(slot);
            return itemHandler.getSlotLimit(slot - this.baseIndexOffset.get(itemHandler));
        }

        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            IItemHandler itemHandler = this.handlerBySlotIndex.get(slot);
            return itemHandler.insertItem(slot - this.baseIndexOffset.get(itemHandler), stack, simulate);
        }

        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            IItemHandler itemHandler = this.handlerBySlotIndex.get(slot);
            return itemHandler.extractItem(slot - this.baseIndexOffset.get(itemHandler), amount, simulate);
        }

        public @NotNull Collection<IItemHandler> getBackingHandlers() {
            return Collections.unmodifiableCollection(this.handlerBySlotIndex.values());
        }
    }
}
