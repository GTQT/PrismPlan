package keqing.gtqt.prismplan.common.metatileentities.single;

import appeng.api.AEApi;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.GridFlags;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.util.item.AEItemStack;
import appeng.util.item.AEStack;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.AdvancedTextWidget;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.metatileentity.IFastRenderMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleOrientedCubeRenderer;
import gregtech.client.renderer.texture.custom.FireboxActiveRenderer;
import gregtech.common.ConfigHolder;
import groovyjarjarantlr4.v4.runtime.misc.NotNull;
import keqing.gtqt.prismplan.api.utils.PrismPlanLog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

public class MetaTileEntityWatcher extends MetaTileEntity implements IFastRenderMetaTileEntity {

    private final IItemHandlerModifiable targetItem;
    // 添加这两个字段声明
    private final long[] monitoringData = new long[3600];
    private final HologramConfig hologramConfig = new HologramConfig();
    protected boolean isOnline;
    long nowAmount;
    int waitTick = 200;
    private AENetworkProxy networkProxy;
    private int meUpdateTick = 0;
    private int dataIndex = 0;
    // 新增功能字段
    private long consumptionRate; // 单位：个/分钟
    private long estimatedDepletionTime = -1; // 单位：分钟（-1表示无效）
    // 新增字段
    private long growthAmount;    // 最近一小时增长总量
    private float growthRate;     // 增长率（百分比）

    public MetaTileEntityWatcher(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);

        this.targetItem = new NotifiableItemStackHandler(this, 1, null, false) {
            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return !(stack.getItem() instanceof ICraftingPatternItem);
            }
        };
    }

    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        GTUtility.writeItems(this.targetItem, "targetItem", data);
        return super.writeToNBT(data);
    }

    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        GTUtility.readItems(this.targetItem, "targetItem", data);
    }

    // 新增方法：清空监控数据
    public void resetMonitoringData() {
        Arrays.fill(monitoringData, 0L);
        consumptionRate = 0;
        estimatedDepletionTime = -1;
    }
    private static final int UPDATE_DATA_ID = 1001;
    // 服务端发送数据包
    private void sendDataUpdate() {
        writeCustomData(UPDATE_DATA_ID, buf -> {
            buf.writeLong(nowAmount);
            buf.writeLong(consumptionRate);
            buf.writeLong(estimatedDepletionTime);
            buf.writeLong(growthAmount);
            buf.writeFloat(growthRate); // 注意这里最后一个是float
        });
    }

    // 修改计算方法
    public void calculateConsumption() {
        if (monitoringData.length < 2) return;

        long totalConsumption = 0;
        int validConsumePoints = 0;
        int sampleCount = 360; // 360个点=1小时（360*10秒）

        // 获取一小时前和当前索引
        int startIndex = (dataIndex - sampleCount + 3600) % 3600;
        int endIndex = (dataIndex - 1 + 3600) % 3600;

        // 计算首尾差值
        long startAmount = monitoringData[startIndex];
        long endAmount = monitoringData[endIndex];
        this.growthAmount = endAmount - startAmount;

        // 计算增长率
        if (startAmount > 0) {
            this.growthRate = (float) (endAmount - startAmount) / startAmount * 100;
        } else {
            this.growthRate = 0;
        }

        // 原有消耗计算逻辑
        for (int i = 0; i < sampleCount; i++) {
            int current = (dataIndex - i - 1 + 3600) % 3600;
            int previous = (current - 1 + 3600) % 3600;

            long diff = monitoringData[previous] - monitoringData[current];
            if (diff > 0) { // 消耗
                totalConsumption += diff;
                validConsumePoints++;
            } else if (diff < 0) { // 增长
            }
        }

        // 更新消耗相关计算
        if (validConsumePoints > 0) {
            consumptionRate = (totalConsumption * 6) / validConsumePoints;
            long currentAmount = monitoringData[endIndex];
            estimatedDepletionTime = consumptionRate > 0 ?
                    currentAmount / consumptionRate : -1;
        } else {
            consumptionRate = 0;
            estimatedDepletionTime = -1;
        }

        // 在计算完成后同步
        sendDataUpdate();
    }

    public void update() {
        super.update();
        if (!this.getWorld().isRemote) {
            ++this.meUpdateTick;
        }
        if (!this.getWorld().isRemote && this.updateMEStatus() && this.shouldSyncME()) {
            this.syncME();
        }

    }

    protected AEStack<?> getAEStack(int solt) {
        return AEItemStack.fromItemStack(targetItem.getStackInSlot(solt));
    }

    public void syncME() {
        AENetworkProxy proxy = this.getProxy();
        if (proxy == null) return;

        try {
            IItemStorageChannel channel = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
            IMEMonitor<IAEItemStack> monitor = proxy.getStorage().getInventory(channel);

            if (targetItem.getStackInSlot(0).isEmpty()) {

            } else {
                IAEStack<?> stack = this.getAEStack(0);
                if (stack == null) return;

                if (stack instanceof IAEItemStack itemStack) {
                    long currentAmount = monitor.getStorageList().findPrecise(itemStack).getStackSize();
                    nowAmount = currentAmount;
                    // 更新数组（滚动存储）
                    monitoringData[dataIndex] = currentAmount;
                    dataIndex = (dataIndex + 1) % 3600; // 环形指针
                    // 每次同步都进行计算（每10秒）
                    calculateConsumption();
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

    protected boolean shouldSyncME() {
        return this.meUpdateTick % waitTick == 0;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityWatcher(metaTileEntityId);
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

        buf.writeInt(dataIndex);
        buf.writeLong(consumptionRate);
        buf.writeLong(estimatedDepletionTime);
        buf.writeLong(growthAmount);
        buf.writeFloat(growthRate);

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

        /*
        dataIndex = buf.readInt();
        nowAmount = buf.readLong();
        consumptionRate = buf.readLong();
        estimatedDepletionTime = buf.readLong();
        growthAmount = buf.readLong();
        growthRate = buf.readFloat();

         */

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
        if (dataId == UPDATE_DATA_ID) {
            nowAmount = buf.readLong();
            consumptionRate = buf.readLong();
            estimatedDepletionTime = buf.readLong();
            growthAmount = buf.readLong();
            growthRate = buf.readFloat(); // 最后读取float类型
        }

    }

    @Override
    public void onRemoval() {
        super.onRemoval();
        if (this.networkProxy != null) {
            this.networkProxy.invalidate();
        }

        var pos = getPos();
        if (!targetItem.getStackInSlot(0).isEmpty()) {
            getWorld().spawnEntity(new EntityItem(getWorld(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, targetItem.getStackInSlot(0)));
            targetItem.extractItem(0, 1, false);
        }
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 180, 240);
        builder.dynamicLabel(28, 12, () -> "物品监控器", 0xFFFFFF);
        builder.widget(new SlotWidget(targetItem, 0, 8, 8, false, true)
                .setBackgroundTexture(GuiTextures.SLOT)
                .setTooltipText("输入槽位"));

        builder.image(4, 28, 172, 128, GuiTextures.DISPLAY);
        builder.widget((new AdvancedTextWidget(8, 32, this::addDisplayText, 16777215)).setMaxWidthLimit(180));

        builder.widget(new ClickButtonWidget(70, 130, 40, 20, "重置", data -> resetMonitoringData()));
        builder.bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 8, 160);
        return builder.build(this.getHolder(), entityPlayer);
    }

    protected void addDisplayText(List<ITextComponent> textList) {
        textList.add(new TextComponentTranslation("prismplan.monitor.current_amount",
                formatAmount(nowAmount)));

        textList.add(new TextComponentTranslation("prismplan.monitor.consumption_rate",
                formatRate(consumptionRate)));

        textList.add(new TextComponentTranslation("prismplan.monitor.growth_amount",
                formatGrowthAmount(growthAmount)));

        textList.add(new TextComponentTranslation("prismplan.monitor.growth_rate",
                formatGrowthRate(growthRate)));

        textList.add(new TextComponentTranslation("prismplan.monitor.estimated_time",
                formatDepletionTime(estimatedDepletionTime)));
    }

    private Object formatAmount(long nowAmount) {
        return String.format("当前数量：%,d items", nowAmount);
    }

    // 格式化消耗速率
    private String formatRate(long rate) {
        return rate > 0 ?
                String.format("%,d items/min", rate) : I18n.translateToLocal("prismplan.monitor.no_data");
    }

    // 格式化耗空时间
    private String formatDepletionTime(long minutes) {
        if (minutes <= 0) {
            return I18n.translateToLocal("prismplan.monitor.infinite");
        }

        long hours = minutes / 60;
        long remainingMinutes = minutes % 60;

        String timeStr = "";
        if (hours > 0) {
            timeStr += hours + I18n.translateToLocal("prismplan.unit.hour");
        }
        if (remainingMinutes > 0) {
            timeStr += remainingMinutes + I18n.translateToLocal("prismplan.unit.minute");
        }
        return timeStr.isEmpty() ?
                I18n.translateToLocal("prismplan.monitor.soon") :
                timeStr;
    }

    // 新增格式化方法
    private String formatGrowthAmount(long amount) {
        return amount != 0 ?
                String.format("%s%,d", amount > 0 ? "+" : "-", Math.abs(amount)) :
                I18n.translateToLocal("prismplan.monitor.no_change");
    }

    private float formatGrowthRate(float rate) {
        return rate;
    }

    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        ICubeRenderer baseTexture = this.getBaseTexture();
        pipeline = ArrayUtils.add(pipeline, new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(this.getPaintingColorForRendering())));
        if (!(baseTexture instanceof FireboxActiveRenderer) && !(baseTexture instanceof SimpleOrientedCubeRenderer)) {
            baseTexture.render(renderState, translation, pipeline);
        } else {
            baseTexture.renderOriented(renderState, translation, pipeline, this.getFrontFacing());
        }
        Textures.ENERGY_OUT.renderSided(this.getFrontFacing(), renderState, translation, pipeline);

    }

    public ICubeRenderer getBaseTexture() {
        return Textures.VOLTAGE_CASINGS[1];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(double x, double y, double z, float partialTicks) {
        IFastRenderMetaTileEntity.super.renderMetaTileEntity(x, y, z, partialTicks);

        GlStateManager.pushMatrix();
        try {
            prepareTransform(x + 0.5, y, z + 0.5);
            renderBackground();
            renderText();
        } finally {
            GlStateManager.popMatrix();
        }
    }

    private void prepareTransform(double baseX, double baseY, double baseZ) {
        // 应用基础位移
        GlStateManager.translate(
                baseX + hologramConfig.posX,
                baseY + hologramConfig.posY,
                baseZ + hologramConfig.posZ
        );

        // 应用面向方向旋转
        applyFrontFacingRotation();

        // 应用自定义旋转（顺序：Yaw -> Pitch -> Roll）
        GlStateManager.rotate(hologramConfig.rotationYaw, 0, 1, 0);
        GlStateManager.rotate(hologramConfig.rotationPitch, 1, 0, 0);
        GlStateManager.rotate(hologramConfig.rotationRoll, 0, 0, 1);

        // 应用缩放
        GlStateManager.scale(hologramConfig.scale, hologramConfig.scale, hologramConfig.scale);
    }

    private void applyFrontFacingRotation() {
        EnumFacing frontFacing = getFrontFacing();
        switch (frontFacing) {
            case SOUTH:
                GlStateManager.rotate(180, 0, 1, 0);
                break;
            case WEST:
                GlStateManager.rotate(90, 0, 1, 0);
                break;
            case EAST:
                GlStateManager.rotate(-90, 0, 1, 0);
                break;
            case NORTH:
            default:
                break;
        }
    }

    private void renderBackground() {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(
                hologramConfig.bgRed,
                hologramConfig.bgGreen,
                hologramConfig.bgBlue,
                hologramConfig.bgAlpha
        );

        float halfWidth = hologramConfig.width / 2;
        float halfHeight = hologramConfig.height / 2;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        buffer.pos(-halfWidth, -halfHeight, 0.1).endVertex();
        buffer.pos(-halfWidth, halfHeight, 0.1).endVertex();
        buffer.pos(halfWidth, halfHeight, 0.1).endVertex();
        buffer.pos(halfWidth, -halfHeight, 0.1).endVertex();
        tessellator.draw();

        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
    }

    private void renderText() {

        List<ITextComponent> textList = Arrays.asList(
                new TextComponentTranslation("物品监控器"),

                new TextComponentTranslation("prismplan.monitor.current_amount",
                        formatAmount(nowAmount)),

                new TextComponentTranslation("prismplan.monitor.consumption_rate",
                        formatRate(consumptionRate)),

                new TextComponentTranslation("prismplan.monitor.growth_amount",
                        formatGrowthAmount(growthAmount)),

                new TextComponentTranslation("prismplan.monitor.growth_rate",
                        formatGrowthRate(growthRate)),

                new TextComponentTranslation("prismplan.monitor.estimated_time",
                        formatDepletionTime(estimatedDepletionTime))
        );

        GlStateManager.translate(0, 0, 0.1);
        GlStateManager.scale(hologramConfig.textScale, hologramConfig.textScale, hologramConfig.textScale);

        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();

        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        float scaleFactor = 1 / hologramConfig.textScale; // 计算实际缩放系数

        // 渲染标题
        List<String> titleLines = wrapText(fontRenderer, textList.get(0).getFormattedText(),
                (int) (hologramConfig.maxLineWidth * scaleFactor));
        for (int i = 0; i < titleLines.size(); i++) {
            renderTextLine(fontRenderer, titleLines.get(i), hologramConfig.width * 25 - 5, hologramConfig.height * 25 - 5 - i * 15, 0xFFFFFF, false);
        }

        // 渲染正文
        for(int j=1;j<=5;j++) {
            List<String> wrappedLines = wrapText(fontRenderer, textList.get(j).getFormattedText(),
                    (int) (hologramConfig.maxLineWidth * scaleFactor));
            for (int i = 0; i < wrappedLines.size(); i++) {
                renderTextLine(fontRenderer, wrappedLines.get(i), hologramConfig.width * 25 - 5, hologramConfig.height * 25 - 5 - (i + j) * 15, 0xFFFFFF, false);
            }
        }


        GlStateManager.enableDepth();
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
    }

    private List<String> wrapText(FontRenderer fr, String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        for (String s : text.split(" ")) {
            if (lines.isEmpty()) {
                lines.add(s);
                continue;
            }

            String last = lines.get(lines.size() - 1);
            if (fr.getStringWidth(last + " " + s) <= maxWidth) {
                lines.set(lines.size() - 1, last + " " + s);
            } else {
                lines.add(s);
            }
        }
        return lines;
    }

    private void renderTextLine(FontRenderer fr, String text, float x, float y, int color, boolean center) {
        GlStateManager.pushMatrix();
        try {
            if (center) {
                int textWidth = fr.getStringWidth(text);
                GlStateManager.translate(-textWidth / 2.0, 0, 0);
            }

            GlStateManager.translate(x, y, 0);
            GlStateManager.rotate(180, 0, 1, 0);
            GlStateManager.rotate(180, 1, 0, 0);

            fr.drawString(text, 0, 0, color);
        } finally {
            GlStateManager.popMatrix();
        }
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        //这个影响模型的可视范围，正常方块都是 1 1 1，长宽高各为1，当这个方块离线玩家视线后，obj模型渲染会停止，所以可以适当放大这个大小能让模型有更多角度的可视
        return new AxisAlignedBB(getPos(), getPos().add(10, 10, 10));
    }

    public class HologramConfig {
        // 位置偏移（以方块中心为原点）
        public float posX = 0.0f;
        public float posY = 4.5f;
        public float posZ = 0.0f;
        // 缩放比例
        public float scale = 1.0f;
        // 旋转参数（角度制）
        public float rotationYaw = 0.0f;   // Y轴旋转
        public float rotationPitch = 0.0f; // X轴旋转
        public float rotationRoll = 0.0f;   // Z轴旋转
        // 背景尺寸
        public float width = 4.0f;
        public float height = 3.0f;
        // 背景颜色
        public float bgRed = 0.0f;
        public float bgGreen = 0.0f;
        public float bgBlue = 1.0f;
        public float bgAlpha = 0.5f;
        // 文本配置
        public float textScale = 0.02f;
        public int maxLineWidth = 200; // 像素单位

    }

}
