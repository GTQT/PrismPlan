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
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.GhostCircuitItemStackHandler;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.GhostCircuitSlotWidget;
import gregtech.api.gui.widgets.ImageCycleButtonWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.gui.widgets.TankWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockNotifiablePart;
import groovyjarjarantlr4.v4.runtime.misc.NotNull;
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
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.IntStream;

public class MetaTileEntityMEInterface extends MetaTileEntityMultiblockNotifiablePart implements IMultiblockAbilityPart<IItemHandlerModifiable>, IControllable, IGhostSlotConfigurable {

    private final IItemHandlerModifiable targetItem;
    private final IItemHandlerModifiable outputItem;

    private final IItemHandlerModifiable extraItem;

    private final FluidTankList targetFluids;
    private final FluidTankList outputsFluids;
    protected @Nullable GhostCircuitItemStackHandler circuitInventory;
    protected boolean isOnline;
    protected boolean workingEnabled;
    private final IItemHandlerModifiable actualImportItems;
    private AENetworkProxy networkProxy;
    private int meUpdateTick = 0;

    public MetaTileEntityMEInterface(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, 4, false);

        this.targetItem = new NotifiableItemStackHandler(this, 9, null, false) {

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return !(stack.getItem() instanceof ICraftingPatternItem);
            }
        };
        this.outputItem = new NotifiableItemStackHandler(this, 9, null, false) {

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

        this.outputsFluids = new FluidTankList(false, IntStream.range(0, 9)
                .mapToObj(i -> new FluidTank(16000))
                .toArray(IFluidTank[]::new));

        this.circuitInventory = new GhostCircuitItemStackHandler(this);
        this.circuitInventory.addNotifiableMetaTileEntity(this);

        this.actualImportItems = new ItemHandlerList(
                Arrays.asList(this.outputItem, this.circuitInventory, this.extraItem));
    }

    // 修改为直接返回新实例
    protected FluidTankList createImportFluidHandler() {
        return new FluidTankList(false, IntStream.range(0, 9)
                .mapToObj(i -> new FluidTank(16000))
                .toArray(IFluidTank[]::new));
    }

    protected FluidTankList createExportFluidHandler() {
        return new FluidTankList(false, IntStream.range(0, 9)
                .mapToObj(i -> new FluidTank(16000))
                .toArray(IFluidTank[]::new));
    }

    @Override
    public void addToMultiBlock(MultiblockControllerBase controllerBase) {
        super.addToMultiBlock(controllerBase);

        for (IItemHandler handler : ((ItemHandlerList) this.actualImportItems).getBackingHandlers()) {
            if (handler instanceof INotifiableHandler notifiable) {
                notifiable.addNotifiableMetaTileEntity(controllerBase);
                notifiable.addToNotifiedList(this, handler, false);
            }
        }
        for (IFluidHandler handler : this.targetFluids) {
            if (handler instanceof INotifiableHandler notifiable) {
                notifiable.addNotifiableMetaTileEntity(controllerBase);
                notifiable.addToNotifiedList(this, handler, false);
            }
        }

    }

    @Override
    public void removeFromMultiBlock(MultiblockControllerBase controllerBase) {
        super.removeFromMultiBlock(controllerBase);

        for (IItemHandler handler : ((ItemHandlerList) this.actualImportItems).getBackingHandlers()) {
            if (handler instanceof INotifiableHandler notifiable) {
                notifiable.removeNotifiableMetaTileEntity(controllerBase);
            }
        }
    }

    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data.setBoolean("workingEnabled", this.workingEnabled);

        GTUtility.writeItems(this.targetItem, "targetItem", data);
        GTUtility.writeItems(this.outputItem, "outputItem", data);
        GTUtility.writeItems(this.extraItem, "extraItem", data);

        data.setTag("targetFluids", this.targetFluids.serializeNBT());
        data.setTag("outputsFluids", this.outputsFluids.serializeNBT());

        if (this.circuitInventory != null) {
            this.circuitInventory.write(data);
        }

        return super.writeToNBT(data);
    }

    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);

        this.workingEnabled = data.getBoolean("workingEnabled");

        GTUtility.readItems(this.targetItem, "targetItem", data);
        GTUtility.readItems(this.outputItem, "outputItem", data);
        GTUtility.readItems(this.extraItem, "extraItem", data);

        this.targetFluids.deserializeNBT(data.getCompoundTag("targetFluids"));
        this.outputsFluids.deserializeNBT(data.getCompoundTag("outputsFluids"));

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
        for (int i = 0; i < outputItem.getSlots(); i++) {
            var pos = getPos();
            if (!outputItem.getStackInSlot(i).isEmpty()) {
                getWorld().spawnEntity(new EntityItem(getWorld(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, outputItem.getStackInSlot(i)));
                outputItem.extractItem(i, 1, false);
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

            builder.widget(new SlotWidget(this.outputItem, i, 7 + 18 * i, 40, true, true, true)
                    .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.ARROW_OUTPUT_OVERLAY)
                    .setChangeListener(this::markDirty));

            builder.widget(new TankWidget(this.targetFluids.getTankAt(i), 7 + 18 * i, 60, 18, 18)
                    .setBackgroundTexture(GuiTextures.FLUID_SLOT)
                    .setContainerClicking(true, true)
                    .setAlwaysShowFull(true));

            builder.widget(new TankWidget(this.outputsFluids.getTankAt(i), 7 + 18 * i, 80, 18, 18)
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
        ItemStack outputStack = outputItem.getStackInSlot(i);
        if (!outputStack.isEmpty()) {
            injectItemsIntoNetwork(monitor, outputStack, i);
        }
    }

    private void handleNonEmptyTarget(int i, IMEMonitor<IAEItemStack> monitor, IAEItemStack itemStack) {
        ItemStack outputStack = outputItem.getStackInSlot(i);
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
                outputItem.extractItem(i, inserted, false);
            } else {
                outputItem.extractItem(i, outputStack.getCount(), false);
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
                    ItemStack remaining = outputItem.insertItem(i, physicalStack, false);

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
            IFluidTank exportTank = outputsFluids.getTankAt(i);

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
        return new MetaTileEntityMEInterface(metaTileEntityId);
    }


    @Override
    public MultiblockAbility<IItemHandlerModifiable> getAbility() {
        return MultiblockAbility.IMPORT_ITEMS;
    }

    @Override
    public void registerAbilities(List<IItemHandlerModifiable> abilityList) {
        abilityList.add(this.actualImportItems);
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
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(outputItem);
        else if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(outputsFluids);
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
}
