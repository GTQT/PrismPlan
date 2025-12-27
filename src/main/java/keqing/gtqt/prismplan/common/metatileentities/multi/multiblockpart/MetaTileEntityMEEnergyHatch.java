package keqing.gtqt.prismplan.common.metatileentities.multi.multiblockpart;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IAEPowerStorage;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.events.MENetworkPowerStorage;
import appeng.api.networking.security.IActionHost;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import codechicken.lib.raytracer.CuboidRayTraceResult;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.common.ConfigHolder;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityEnergyHatch;
import keqing.gtqt.prismplan.common.metatileentities.multi.PrismPlanMetaTileEntities;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

import static gregtech.api.GTValues.V;
import static gregtech.api.capability.GregtechDataCodes.UPDATE_ONLINE_STATUS;

public class MetaTileEntityMEEnergyHatch extends MetaTileEntityEnergyHatch implements IActionHost, IGridProxyable, IAEPowerStorage, IControllable {

    public final static String WORKING_TAG = "WorkingEnabled";
    // 能量单位换算
    private static final double EU_TO_AE_RATIO = 2.0; // 1 EU = 2 AE
    private static final double AE_TO_EU_RATIO = 0.5; // 1 AE = 0.5 EU
    protected boolean isOnline;
    protected boolean meStatusChanged = false;
    private boolean workingEnabled = true;
    private AENetworkProxy aeProxy;
    private int meUpdateTick;
    private boolean allowExtraConnections;

    public MetaTileEntityMEEnergyHatch(ResourceLocation metaTileEntityId, int tier, int amperage, boolean isExportHatch) {
        super(metaTileEntityId, tier, amperage, isExportHatch);
    }

    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityMEEnergyHatch(this.metaTileEntityId, this.getTier(), this.amperage, this.isExportHatch);
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
    public double injectAEPower(double amount, @Nonnull Actionable mode) {
        if (isExportHatch) return amount; // 动力仓（输出）拒绝注入

        double euAmount = amount * AE_TO_EU_RATIO;

        if (mode == Actionable.SIMULATE) {
            long canInsertEU = energyContainer.getEnergyCanBeInserted();
            double canInsertAE = canInsertEU * EU_TO_AE_RATIO;
            return Math.max(0, amount - canInsertAE);
        }

        long insertedEU = energyContainer.addEnergy((long) euAmount);
        double insertedAE = insertedEU * EU_TO_AE_RATIO;
        return amount - insertedAE;
    }

    @Override
    public double extractAEPower(double amount, @Nonnull Actionable mode, @Nonnull PowerMultiplier multiplier) {
        return 0;
    }

    @Override
    public double getAEMaxPower() {
        return energyContainer.getEnergyCapacity() * EU_TO_AE_RATIO;
    }

    @Override
    public double getAECurrentPower() {
        return energyContainer.getEnergyStored() * EU_TO_AE_RATIO;
    }

    @Override
    public boolean isAEPublicPowerStorage() {
        return !isExportHatch;
    }

    @Nonnull
    @Override
    public AccessRestriction getPowerFlow() {
        // 能源仓（输入）：只允许注入（WRITE）
        // 动力仓（输出）：特殊处理，不遵循标准储能权限
        return isExportHatch ? AccessRestriction.READ : AccessRestriction.WRITE;
    }

    @Override
    public void securityBreak() {
        getWorld().destroyBlock(getPos(), true);
    }

    @Override
    public void update() {
        super.update();
        World world = getWorld();

        // 仅服务端执行逻辑
        if (!world.isRemote) {
            this.meUpdateTick++;

            // ME网络状态同步
            if (this.workingEnabled && updateMEStatus() && shouldSyncME()) {
                syncME();
            }

            // 能量传输处理（仅当AE网络活动时）
            if (workingEnabled && aeProxy != null && aeProxy.isActive()) {
                if (isExportHatch) {
                    processPowerOutput(); // 动力仓：能量输出到AE网络
                } else {
                    processPowerInput();  // 能源仓：从AE网络请求能量
                }
            }
        }
    }

    /**
     * 同步ME网络状态
     */
    private void syncME() {
        // TODO: 实现ME网络状态同步逻辑
    }

    /**
     * 处理能量输出到AE网络
     * 将EU能量转换为AE能量注入网络
     */
    private void processPowerOutput() {
        // 检查是否有可输出的能量
        if (energyContainer.getEnergyStored() <= 0) {
            return;
        }

        try {
            IEnergyGrid energyGrid = getProxy().getEnergy();

            // 1. 计算可提取的EU量（受容器速率限制）
            long euToExtract = Math.min(
                    energyContainer.getEnergyStored(),
                    V[getTier()] * amperage
            );

            // 2. 转换为AE能量 (1 EU = 2 AE)
            double aeToInject = euToExtract * EU_TO_AE_RATIO;

            // 3. 注入AE网络
            double leftoverAE = energyGrid.injectPower(aeToInject, Actionable.MODULATE);

            // 4. 计算实际消耗的EU
            double usedAE = aeToInject - leftoverAE;
            long usedEU = (long) (usedAE * AE_TO_EU_RATIO);

            // 5. 从容器移除已使用的能量
            if (usedEU > 0) {
                energyContainer.removeEnergy(usedEU);
            }

        } catch (GridAccessException ignored) {
            // 网络不可用，不消耗能量
        }
    }

    /**
     * 处理从AE网络输入能量
     * 向AE网络发送能量请求事件
     */
    public void processPowerInput() {
        // 仅在能量容器未满时请求能量
        if (energyContainer.getEnergyStored() >= energyContainer.getEnergyCapacity()) {
            return;
        }

        try {
            getProxy().getGrid().postEvent(new MENetworkPowerStorage(
                    this,
                    MENetworkPowerStorage.PowerEventType.REQUEST_POWER
            ));
        } catch (GridAccessException ignored) {
            // 网络不可用，忽略错误
        }
    }

    @Nullable
    @Override
    public AENetworkProxy getProxy() {
        if (this.aeProxy == null) {
            return this.aeProxy = this.createProxy();
        }
        if (!this.aeProxy.isReady() && this.getWorld() != null) {
            this.aeProxy.onReady();
        }
        return this.aeProxy;
    }


    @Nullable
    @Override
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(this.getWorld(), this.getPos().getX(), this.getPos().getY(), this.getPos().getZ());
    }

    @Nullable
    public AENetworkProxy createProxy() {
        if (this.getHolder() instanceof IGridProxyable holder) {
            AENetworkProxy proxy = new AENetworkProxy(holder, "mte_proxy", this.getStackForm(), true);
            // ===== 关键：设置空闲功耗为0，解决动力仓死锁问题 =====
            proxy.setIdlePowerUsage(0.0);
            proxy.setFlags(GridFlags.REQUIRE_CHANNEL);
            proxy.setValidSides(getConnectableSides());
            return proxy;
        }
        return null;
    }

    public boolean updateMEStatus() {
        if (!getWorld().isRemote) {
            boolean isOnline = this.aeProxy != null && this.aeProxy.isActive() && this.aeProxy.isPowered();
            if (this.isOnline != isOnline) {
                writeCustomData(UPDATE_ONLINE_STATUS, buf -> buf.writeBoolean(isOnline));
                this.isOnline = isOnline;
                this.meStatusChanged = true;
            } else {
                this.meStatusChanged = false;
            }
        }
        return this.isOnline;
    }

    protected boolean shouldSyncME() {
        return this.meUpdateTick % ConfigHolder.compat.ae2.updateIntervals == 0;
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        if (this.aeProxy != null) {
            buf.writeBoolean(true);
            NBTTagCompound proxy = new NBTTagCompound();
            this.aeProxy.writeToNBT(proxy);
            buf.writeCompoundTag(proxy);
        } else {
            buf.writeBoolean(false);
        }
        buf.writeInt(this.meUpdateTick);
        buf.writeBoolean(this.isOnline);
        buf.writeBoolean(this.allowExtraConnections);
        buf.writeBoolean(workingEnabled);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        if (buf.readBoolean()) {
            NBTTagCompound nbtTagCompound;
            try {
                nbtTagCompound = buf.readCompoundTag();
            } catch (IOException ignored) {
                nbtTagCompound = null;
            }

            if (this.aeProxy != null && nbtTagCompound != null) {
                this.aeProxy.readFromNBT(nbtTagCompound);
            }
        }
        this.meUpdateTick = buf.readInt();
        this.isOnline = buf.readBoolean();
        this.allowExtraConnections = buf.readBoolean();
        this.workingEnabled = buf.readBoolean();
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

    @Override
    public void onRemoval() {
        super.onRemoval();
        if (this.aeProxy != null) {
            this.aeProxy.invalidate();
        }
    }

    @Override
    public AECableType getCableConnectionType(AEPartLocation part) {
        if (part.getFacing() != this.frontFacing && !this.allowExtraConnections) {
            return AECableType.NONE;
        }
        return AECableType.SMART;
    }

    @Override
    public void setFrontFacing(EnumFacing frontFacing) {
        super.setFrontFacing(frontFacing);
        updateConnectableSides();
    }

    public EnumSet<EnumFacing> getConnectableSides() {
        return this.allowExtraConnections ? EnumSet.allOf(EnumFacing.class) : EnumSet.of(getFrontFacing());
    }

    public void updateConnectableSides() {
        if (this.aeProxy != null) {
            this.aeProxy.setValidSides(getConnectableSides());
        }
    }

    @Override
    public boolean onWireCutterClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                     CuboidRayTraceResult hitResult) {
        this.allowExtraConnections = !this.allowExtraConnections;
        updateConnectableSides();

        if (!getWorld().isRemote) {
            playerIn.sendStatusMessage(new TextComponentTranslation(this.allowExtraConnections ?
                            "gregtech.machine.me.extra_connections.enabled" : "gregtech.machine.me.extra_connections.disabled"),
                    true);
        }

        return true;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("AllowExtraConnections", this.allowExtraConnections);
        data.setBoolean(WORKING_TAG, this.workingEnabled);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.allowExtraConnections = data.getBoolean("AllowExtraConnections");
        if (data.hasKey(WORKING_TAG)) {
            this.workingEnabled = data.getBoolean(WORKING_TAG);
        }
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        return super.getCapability(capability, side);
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.workingEnabled;
    }

    @Override
    public void setWorkingEnabled(boolean workingEnabled) {
        this.workingEnabled = workingEnabled;
        World world = this.getWorld();
        if (world != null && !world.isRemote) {
            writeCustomData(GregtechDataCodes.WORKING_ENABLED, buf -> buf.writeBoolean(workingEnabled));
        }
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return true;
    }

    @Override
    protected final ModularUI createUI(EntityPlayer player) {
        ModularUI.Builder builder = createUITemplate(player);
        return builder.build(this.getHolder(), player);
    }

    protected ModularUI.Builder createUITemplate(EntityPlayer player) {
        ModularUI.Builder builder = ModularUI
                .builder(GuiTextures.BACKGROUND, 176, 18 + 94)
                .label(10, 5, getMetaFullName());
        // ME Network status
        builder.dynamicLabel(10, 15, () -> this.isOnline ?
                        I18n.format("gregtech.gui.me_network.online") :
                        I18n.format("gregtech.gui.me_network.offline"),
                0x404040);

        builder.bindPlayerInventory(player.inventory, GuiTextures.SLOT, 7, 18 + 12);
        return builder;
    }

    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        String tierName = GTValues.VNF[this.getTier()];
        this.addDescriptorTooltip(stack, world, tooltip, advanced);
        tooltip.add(TextFormatting.GREEN + I18n.format("-网络参数："));
        if (!isExportHatch) {
            tooltip.add(I18n.format("允许直接从ME网络内抽取能量"));
            tooltip.add(I18n.format("功能上相当于 正常的AE耗能设备"));
            tooltip.add(I18n.format("在从ME网络抽取能量时IO无上限，每tick传输一次"));
        }
        if (isExportHatch) {
            tooltip.add(I18n.format("允许直接从ME网络内发送能量"));
            tooltip.add(I18n.format("功能上相当于 能源接收器"));
            tooltip.add(I18n.format("在向ME网络传输能量时IO无上限，每tick传输一次"));
        }
        tooltip.add(TextFormatting.GREEN + I18n.format("-电气参数："));
        if (this.isExportHatch) {
            tooltip.add(I18n.format("gregtech.universal.tooltip.voltage_out", this.energyContainer.getOutputVoltage(), tierName));
            tooltip.add(I18n.format("gregtech.universal.tooltip.amperage_out_till", this.energyContainer.getOutputAmperage()));
            tooltip.add(I18n.format("gregtech.universal.tooltip.throughput", this.energyContainer.getOutputVoltage() * this.energyContainer.getOutputAmperage()));
        } else {
            tooltip.add(I18n.format("gregtech.universal.tooltip.voltage_in", this.energyContainer.getInputVoltage(), tierName));
            tooltip.add(I18n.format("gregtech.universal.tooltip.amperage_in_till", this.energyContainer.getInputAmperage()));
            tooltip.add(I18n.format("gregtech.universal.tooltip.throughput", this.energyContainer.getInputVoltage() * this.energyContainer.getInputAmperage()));
        }

        tooltip.add(I18n.format("gregtech.universal.tooltip.energy_storage_capacity", this.energyContainer.getEnergyCapacity()));
        tooltip.add(I18n.format("gregtech.universal.enabled"));
    }

    public void getSubItems(CreativeTabs creativeTab, NonNullList<ItemStack> subItems) {
        if (this == PrismPlanMetaTileEntities.ME_ENERGY_INPUT_HATCH[0]) {
            for (MetaTileEntityMEEnergyHatch hatch : PrismPlanMetaTileEntities.ME_ENERGY_INPUT_HATCH) {
                if (hatch != null) {
                    subItems.add(hatch.getStackForm());
                }
            }

            for (MetaTileEntityMEEnergyHatch hatch : PrismPlanMetaTileEntities.ME_ENERGY_INPUT_HATCH_4A) {
                if (hatch != null) {
                    subItems.add(hatch.getStackForm());
                }
            }

            for (MetaTileEntityMEEnergyHatch hatch : PrismPlanMetaTileEntities.ME_ENERGY_INPUT_HATCH_16A) {
                if (hatch != null) {
                    subItems.add(hatch.getStackForm());
                }
            }

            for (MetaTileEntityMEEnergyHatch hatch : PrismPlanMetaTileEntities.ME_ENERGY_INPUT_HATCH_64A) {
                if (hatch != null) {
                    subItems.add(hatch.getStackForm());
                }
            }

            for (MetaTileEntityMEEnergyHatch hatch : PrismPlanMetaTileEntities.ME_ENERGY_OUTPUT_HATCH) {
                if (hatch != null) {
                    subItems.add(hatch.getStackForm());
                }
            }

            for (MetaTileEntityMEEnergyHatch hatch : PrismPlanMetaTileEntities.ME_ENERGY_OUTPUT_HATCH_4A) {
                if (hatch != null) {
                    subItems.add(hatch.getStackForm());
                }
            }

            for (MetaTileEntityMEEnergyHatch hatch : PrismPlanMetaTileEntities.ME_ENERGY_OUTPUT_HATCH_16A) {
                if (hatch != null) {
                    subItems.add(hatch.getStackForm());
                }
            }

            for (MetaTileEntityMEEnergyHatch hatch : PrismPlanMetaTileEntities.ME_ENERGY_OUTPUT_HATCH_64A) {
                if (hatch != null) {
                    subItems.add(hatch.getStackForm());
                }
            }
        } else if (this.getClass() != MetaTileEntityMEEnergyHatch.class) {
            super.getSubItems(creativeTab, subItems);
        }

    }
}