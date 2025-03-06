package keqing.gtqt.prismplan.common.metatileentities.multi.multiblock.estorage;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.implementations.tiles.IChestOrDrive;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IAEPowerStorage;
import appeng.api.networking.events.*;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.ICellContainer;
import appeng.api.storage.ICellInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IStorageChannel;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.me.helpers.MachineSource;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.AdvancedTextWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.OrientedOverlayRenderer;
import gregtech.common.ConfigHolder;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;
import keqing.gtqt.prismplan.api.capability.ICellHatch;
import keqing.gtqt.prismplan.api.capability.INetWorkStore;
import keqing.gtqt.prismplan.api.multiblock.PrismPlanMultiblockAbility;
import keqing.gtqt.prismplan.api.utils.PrismPlanLog;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static net.minecraft.init.Blocks.AIR;

public class MetaTileEntityNetWorkStoreHatch extends MetaTileEntityMultiblockPart implements
        IMultiblockAbilityPart<INetWorkStore>, INetWorkStore, IActionHost, IGridProxyable,
        ICellContainer, IAEPowerStorage {

    //接口可能不全，使用到了请自行补全

    //这是网络仓，用于多方块访问网络的仓口
    //使用在多方块的文件内加入
    //    public INetWorkStore getNetWorkStoreHatch() {
    //        List<INetWorkStore> abilities = getAbilities(PrismPlanMultiblockAbility.NETWORK_STORE);
    //        if (abilities.isEmpty())
    //            return null;
    //        return abilities.get(0);
    //    }
    //通过本方法访问仓的接口来访问仓所链接的网络的基本信息
    //当然这要保证多方块拥有本仓，建议在多方块成型后使用

    //控制器需要@Override的方法：
    //    public @Nullable AENetworkProxy getProxy() {
    //        return getNetWorkProxyHatch().getProxy();
    //    }


    protected final IActionSource source = new MachineSource(this);
    //未知作用 后期可能放置身份卡
    protected IItemHandlerModifiable targetItem;
    protected boolean isOnline;
    protected int priority = 0;
    //保证本仓第一个创建网络 在使用本仓之前必须检查canBeUse
    boolean canBeUse;
    //接口实现getter：
    private AENetworkProxy networkProxy;
    //其他
    private int meUpdateTick = 0;
    private boolean wasActive = false;

    public MetaTileEntityNetWorkStoreHatch(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, 6);
        this.targetItem = new NotifiableItemStackHandler(this, 1, null, false);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityNetWorkStoreHatch(metaTileEntityId);
    }

    //测试用 检查网络是否畅通
    public void update() {
        super.update();

        if (!this.getWorld().isRemote) {
            if (networkProxy != null && networkProxy.isReady() && getFrontBlock() == AIR)
                this.networkProxy.invalidate();

            ++this.meUpdateTick;
        }
        if (!this.getWorld().isRemote && this.updateMEStatus() && this.shouldSyncME()) {
            this.syncME();
        }

    }

    public IBlockState getFrontBlock() {
        BlockPos pos = this.getPos();
        EnumFacing facing = this.getFrontFacing();
        return getWorld().getBlockState(pos.offset(facing));
    }

    //触发定时同步器
    public boolean couldSyncME() {
        if (this.getController() == null) return false;
        if (!this.getController().isStructureFormed()) return false;
        return !this.getWorld().isRemote && this.updateMEStatus() && this.shouldSyncME() && canBeUse;
    }

    //检查代理是否创建 阻止抢占
    public boolean couldUse() {
        if (this.getController() == null) return false;
        if (!this.getController().isStructureFormed()) return false;
        return !this.getWorld().isRemote && canBeUse;
    }

    //检查代理是否创建 阻止抢占
    @Override
    public boolean canBeUse() {
        return canBeUse;
    }

    public void syncME() {
        AENetworkProxy proxy = this.getProxy();
        /*
        if (proxy == null) return;

        try {
            IItemStorageChannel channel = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
            IMEMonitor<IAEItemStack> monitor = proxy.getStorage().getInventory(channel);
            PrismPlanLog.logger.info("Network proxy update:" + monitor.getStorageList());

        } catch (GridAccessException e) {
            PrismPlanLog.logger.warn("Grid access failed", e);
        }

         */
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 180, 240);
        builder.dynamicLabel(28, 12, () -> "ME网络仓", 0xFFFFFF);
        builder.widget(new SlotWidget(targetItem, 0, 8, 8, false, true)
                .setBackgroundTexture(GuiTextures.SLOT)
                .setTooltipText("输入槽位"));

        builder.image(4, 28, 172, 128, GuiTextures.DISPLAY);
        builder.dynamicLabel(8, 32, () -> this.isOnline ? I18n.format("gregtech.gui.me_network.online") : I18n.format("gregtech.gui.me_network.offline"), 4210752);
        builder.widget((new AdvancedTextWidget(8, 42, this::addDisplayText, 16777215)).setMaxWidthLimit(180));

        builder.bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 8, 160);
        return builder.build(this.getHolder(), entityPlayer);
    }

    protected void addDisplayText(List<ITextComponent> textList) {
        if (this.getController() != null)
            textList.add(new TextComponentTranslation("正在访问："+I18n.format(this.getController().getMetaFullName())));
    }

    public @Nullable AENetworkProxy getProxy() {
        if (this.networkProxy == null) {
            canBeUse = false;
            return this.networkProxy = this.createProxy();
        } else {
            if (!this.networkProxy.isReady() && this.getWorld() != null) {
                this.networkProxy.onReady();
            }
            canBeUse = true;
            return this.networkProxy;
        }
    }

    @Override
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(this.getWorld(), this.getPos().getX(), this.getPos().getY(), this.getPos().getZ());
    }

    private @Nullable AENetworkProxy createProxy() {
        IGregTechTileEntity mte = this.getHolder();
        if (mte instanceof IGridProxyable holder) {
            AENetworkProxy proxy = new AENetworkProxy(holder, "mte_proxy", this.getStackForm(), true);
            proxy.setFlags(GridFlags.REQUIRE_CHANNEL, GridFlags.DENSE_CAPACITY);
            proxy.setIdlePowerUsage(1.0D);
            proxy.setValidSides(EnumSet.of(this.getFrontFacing()));
            canBeUse = true;

            return proxy;
        } else {
            return null;
        }
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
        return this.meUpdateTick % ConfigHolder.compat.ae2.updateIntervals == 0;
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
        buf.writeBoolean(this.canBeUse);
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
        this.canBeUse = buf.readBoolean();
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

    public void refresh() {
        getProxy();
        getControl().recalculateEnergyUsage();
        List<ICellHatch> cellDrives = getControl().getCellDrives();
        if (!cellDrives.isEmpty()) {
            try {
                networkProxy.getGrid().postEvent(new MENetworkCellArrayUpdate());
            } catch (GridAccessException ignored) {
            }
        }
    }

    public IActionSource getSource() {
        return source;
    }

    @Nonnull
    @Override
    public AECableType getCableConnectionType(@Nonnull final AEPartLocation dir) {
        return AECableType.DENSE_SMART;
    }

    @Override
    public void onRemoval() {
        super.onRemoval();
        if (this.networkProxy != null) {
            this.networkProxy.invalidate();
        }
    }

    @Override
    public MultiblockAbility<INetWorkStore> getAbility() {
        return PrismPlanMultiblockAbility.NETWORK_STORE;
    }

    @Override
    public void registerAbilities(List<INetWorkStore> abilityList) {
        abilityList.add(this);
    }

    @Nonnull
    @Override
    public IGridNode getActionableNode() {
        return this.getProxy().getNode();
    }

    @Nullable
    @Override
    public IGridNode getGridNode(@Nonnull final AEPartLocation dir) {
        return this.getProxy().getNode();
    }

    @Override
    public void securityBreak() {
        getWorld().destroyBlock(getPos(), true);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        OrientedOverlayRenderer overlayRenderer = Textures.FUSION_REACTOR_OVERLAY;
        overlayRenderer.renderOrientedState(renderState, translation, pipeline, getFrontFacing(), this.getController() != null, isOnline);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public List<IMEInventoryHandler> getCellArray(final IStorageChannel<?> channel) {
        if (this.getController() != null) {
            return ((MetaTileEntityStorageCellControl) this.getController()).getCellDrives().stream()
                    .map(a -> a.getHandler(channel))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @MENetworkEventSubscribe
    public void stateChange(final MENetworkPowerStatusChange c) {
        postCellArrayUpdateEvent();
    }

    @MENetworkEventSubscribe
    public void stateChange(final MENetworkChannelsChanged c) {
        postCellArrayUpdateEvent();
    }

    protected void postCellArrayUpdateEvent() {
        final boolean currentActive = this.networkProxy.isActive();
        if (this.wasActive != currentActive) {
            this.wasActive = currentActive;
            try {
                this.networkProxy.getGrid().postEvent(new MENetworkCellArrayUpdate());
            } catch (final GridAccessException e) {
            }
        }
    }

    @Override
    public int getPriority() {
        return priority;
    }


    //none
    @Override
    public void blinkCell(final int slot) {
    }

    @Override
    public void saveChanges(@Nullable final ICellInventory<?> cellInventory) {
    }


    public MetaTileEntityStorageCellControl getControl()
    {
        if(this.getController() instanceof MetaTileEntityStorageCellControl mte) return mte;
        return null;
    }

    @Override
    public double injectAEPower(final double amt, @Nonnull final Actionable mode) {
        if (this.getControl() == null) {
            return 0;
        }
        if (amt < 0.000001) {
            return 0;
        }
        if (mode == Actionable.MODULATE && this.getAECurrentPower() < 0.01 && amt > 0) {
            try {
                networkProxy.getGrid().postEvent(new MENetworkPowerStorage(this, MENetworkPowerStorage.PowerEventType.PROVIDE_POWER));
            } catch (final GridAccessException ignored) {
            }
        }
        return getControl().injectPower(amt, mode);
    }

    @Override
    public double extractAEPower(final double amt, @Nonnull final Actionable mode, @Nonnull final PowerMultiplier multiplier) {
        if (this.getControl() == null) {
            return 0;
        }
        if (mode == Actionable.MODULATE) {
            final boolean wasFull = this.getAECurrentPower() >= this.getAEMaxPower() - 0.001;
            if (wasFull && amt > 0) {
                try {
                    networkProxy.getGrid().postEvent(new MENetworkPowerStorage(this, MENetworkPowerStorage.PowerEventType.REQUEST_POWER));
                } catch (final GridAccessException ignored) {
                }
            }
        }
        return multiplier.divide(getControl().extractPower(multiplier.multiply(amt), mode));
    }

    @Override
    public double getAEMaxPower() {
        if (this.getControl() == null) {
            return 0;
        }
        return this.getControl().getMaxEnergyStore();
    }

    @Override
    public double getAECurrentPower() {
        if (this.getControl() == null) {
            return 0;
        }
        return this.getControl().getEnergyStored();
    }
    @Override
    public boolean isAEPublicPowerStorage() {
        return true;
    }

    @Nonnull
    @Override
    public AccessRestriction getPowerFlow() {
        return AccessRestriction.READ_WRITE;
    }

}
