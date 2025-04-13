package keqing.gtqt.prismplan.common.metatileentities.multi.multiblock.ecalculator;

import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkCraftingCpuChange;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.IActionSource;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.me.GridAccessException;
import appeng.me.cluster.implementations.CraftingCPUCluster;
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
import gregtech.api.metatileentity.multiblock.AbilityInstances;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.common.ConfigHolder;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;
import keqing.gtqt.prismplan.api.capability.INetWorkCalculator;
import keqing.gtqt.prismplan.api.multiblock.PrismPlanMultiblockAbility;
import keqing.gtqt.prismplan.client.textures.PrismPlanTextures;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import static net.minecraft.init.Blocks.AIR;

public class MetaTileEntityNetWorkCalculatorHatch extends MetaTileEntityMultiblockPart implements
        IMultiblockAbilityPart<INetWorkCalculator>, INetWorkCalculator, IGridProxyable {

    protected final IActionSource source = new MachineSource(this);
    private final boolean isAttached = false;
    //未知作用 后期可能放置身份卡
    protected IItemHandlerModifiable targetItem;
    protected boolean isOnline;
    //保证本仓第一个创建网络 在使用本仓之前必须检查canBeUse
    boolean canBeUse;
    //接口实现getter：
    private AENetworkProxy networkProxy;
    //其他
    private int meUpdateTick = 0;
    private boolean wasActive = false;

    public MetaTileEntityNetWorkCalculatorHatch(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, 6);
        this.targetItem = new NotifiableItemStackHandler(this, 1, null, false);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityNetWorkCalculatorHatch(metaTileEntityId);
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


    public IBlockState getFrontBlock() {
        BlockPos pos = this.getPos();
        EnumFacing facing = this.getFrontFacing();
        return getWorld().getBlockState(pos.offset(facing));
    }

    @Override
    public boolean canBeUse() {
        return canBeUse;
    }

    @MENetworkEventSubscribe
    public void stateChange(final MENetworkPowerStatusChange c) {
        final boolean currentActive = this.getProxy().isActive();
        if (this.wasActive != currentActive) {
            this.wasActive = currentActive;
            postCPUClusterChangeEvent();
        }
    }

    @MENetworkEventSubscribe
    public void stateChange(final MENetworkChannelsChanged c) {
        final boolean currentActive = this.getProxy().isActive();
        if (this.wasActive != currentActive) {
            this.wasActive = currentActive;
            postCPUClusterChangeEvent();
        }
    }

    public void postCPUClusterChangeEvent() {
        if (this.getProxy().isActive()) {
            try {
                this.getProxy().getGrid().postEvent(new MENetworkCraftingCpuChange(this.getProxy().getNode()));
            } catch (final GridAccessException ignored) {
            }
        }
    }

    // Clusters
    public List<CraftingCPUCluster> getCPUs() {
        if (this.getController() != null && this.getController().isStructureFormed()) {

            final boolean currentActive = this.getProxy().isActive();
            if (!currentActive) {
                return Collections.emptyList();
            }

            MetaTileEntityCalculatorControl controller = (MetaTileEntityCalculatorControl) getController();
            if (controller == null) {
                return Collections.emptyList();
            }

            return controller.getClusterList();
        }
        return Collections.emptyList();
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
            textList.add(new TextComponentTranslation("正在访问：" + I18n.format(this.getController().getMetaFullName())));
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
    public MultiblockAbility<INetWorkCalculator> getAbility() {
        return PrismPlanMultiblockAbility.NETWORK_CALCULATOR;
    }

    @Override
    public void registerAbilities(AbilityInstances abilityInstances) {
        abilityInstances.add(this);
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

    @Override
    public void securityBreak() {
        getWorld().destroyBlock(getPos(), true);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        PrismPlanTextures.NETWORK_HATCH.renderSided(getFrontFacing(), renderState, translation, pipeline);
    }

    @Override
    public void addInformation(ItemStack stack, World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("prismplan.ecalculator_me_channel.info.0"));
        tooltip.add(I18n.format("prismplan.ecalculator_me_channel.info.1"));
        tooltip.add(I18n.format("prismplan.ecalculator_me_channel.info.2"));
        tooltip.add(I18n.format("prismplan.ecalculator_me_channel.info.3"));
    }
}
