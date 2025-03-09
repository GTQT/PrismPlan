package keqing.gtqt.prismplan.common.metatileentities.multi.multiblock.ecalculator;

import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.WorldCoord;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.AdvancedTextWidget;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.util.TextComponentUtil;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.OrientedOverlayRenderer;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import keqing.gtqt.prismplan.api.capability.*;
import keqing.gtqt.prismplan.api.multiblock.PrismPlanMultiblockAbility;
import keqing.gtqt.prismplan.api.utils.PrimsPlanUtility;
import keqing.gtqt.prismplan.api.utils.PrismPlanLog;
import keqing.gtqt.prismplan.api.utils.TimeRecorder;
import keqing.gtqt.prismplan.client.textures.PrismPlanTextures;
import keqing.gtqt.prismplan.common.block.PrismPlanBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

import static gregtech.api.util.RelativeDirection.*;
import static keqing.gtqt.prismplan.common.block.prismPlan.BlockMultiblockCasing.CasingType.*;

public class MetaTileEntityCalculatorControl extends MultiblockWithDisplayBase {

    protected CraftingCPUCluster virtualCPU = null;
    protected int parallelism = 0;
    protected long totalBytes = 0;
    ///////////////////////////////////////////////////////////////////////////////////////////
    //ui
    int page = 0;

    public MetaTileEntityCalculatorControl(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        recalculateParallelism();
        recalculateTotalBytes();
        // Create / Update virtual cluster
        createVirtualCPU();
    }

    @SuppressWarnings("DataFlowIssue")
    protected void recalculateParallelism() {
        parallelism = 0;
        for (IParallelHatch parallelProc : getIParallelHatch())
            parallelism += parallelProc.getParallelism();

        // Update accelerators
        getIThreadHatch().forEach(threadCore -> threadCore.getCpus().stream()
                .map(ECPUCluster::from)
                .forEach(ecpuCluster -> ecpuCluster.prismplan_ec$setAccelerators(this.parallelism))
        );
    }

    protected void recalculateTotalBytes() {
        totalBytes = 0;
        for (ICalculatorHatch calculatorHatch : getICalculatorHatch())
            totalBytes += calculatorHatch.getSuppliedBytes();

    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public long getAvailableBytes() {
        long usedStorage = 0;
        for (IThreadHatch iThreadHatch : getIThreadHatch())
            usedStorage += iThreadHatch.getUsedStorage();

        return totalBytes - usedStorage;
    }

    public long getUsedBytes() {
        return totalBytes - getAvailableBytes();
    }

    @SuppressWarnings("DataFlowIssue")
    public void onVirtualCPUSubmitJob(final long usedBytes) {
        for (final IThreadHatch threadCore : getIThreadHatch()) {
            if (threadCore.addCPU(virtualCPU, false)) {
                ECPUCluster ecpuCluster = ECPUCluster.from(this.virtualCPU);
                ecpuCluster.prismplan_ec$setAvailableStorage(usedBytes);
                ecpuCluster.prismplan_ec$setVirtualCPUOwner(null);
                this.virtualCPU = null;
                createVirtualCPU();
                return;
            }
        }
        for (final IThreadHatch threadCore : getIThreadHatch()) {
            if (threadCore.addCPU(virtualCPU, true)) {
                ECPUCluster ecpuCluster = ECPUCluster.from(this.virtualCPU);
                final long usedExtraBytes = (long) (usedBytes * 0.1F);
                ecpuCluster.prismplan_ec$setAvailableStorage(usedBytes + usedExtraBytes);
                ecpuCluster.prismplan_ec$setUsedExtraStorage(usedExtraBytes);
                ecpuCluster.prismplan_ec$setVirtualCPUOwner(null);
                this.virtualCPU = null;
                createVirtualCPU();
                return;
            }
        }
        PrismPlanLog.logger.warn("Failed to submit virtual cluster to thread core, it may be invalid!");
    }

    public void createVirtualCPU() {
        final long availableBytes = getAvailableBytes();
        if (availableBytes < totalBytes * 0.1F) {
            if (this.virtualCPU != null) {
                this.virtualCPU.destroy();
                this.virtualCPU = null;
            }
            return;
        }

        if (this.virtualCPU != null) {
            ECPUCluster eCluster = ECPUCluster.from(this.virtualCPU);
            eCluster.prismplan_ec$setAvailableStorage(availableBytes);
            eCluster.prismplan_ec$setAccelerators(parallelism);
            return;
        }

        boolean canAddCluster = false;
        for (final IThreadHatch part : getIThreadHatch()) {
            if (part.canAddCPU()) {
                canAddCluster = true;
                break;
            }
        }

        if (!canAddCluster) {
            return;
        }

        WorldCoord pos = new WorldCoord(getPos());
        this.virtualCPU = new CraftingCPUCluster(pos, pos);
        ECPUCluster eCluster = ECPUCluster.from(this.virtualCPU);
        eCluster.prismplan_ec$setVirtualCPUOwner(this);
        eCluster.prismplan_ec$setAvailableStorage(availableBytes);
        eCluster.prismplan_ec$setAccelerators(parallelism);

        if (!isStructureFormed()) return;
        if (getNetWorkCalculatorHatch().getProxy() != null) {
            getNetWorkCalculatorHatch().postCPUClusterChangeEvent();
        }
    }

    public List<CraftingCPUCluster> getClusterList() {
        final List<CraftingCPUCluster> clusters = new ArrayList<>();
        for (IThreadHatch iThreadHatch : getIThreadHatch()) {
            iThreadHatch.refreshCPUSource();
            clusters.addAll(iThreadHatch.getCpus());
        }
        if (this.virtualCPU != null) {
            // Refresh machine source.
            ECPUCluster.from(this.virtualCPU).prismplan_ec$setVirtualCPUOwner(this);
            clusters.add(this.virtualCPU);
        }
        return clusters;
    }

    public void onClusterChanged() {
    }

    public int getSharedParallelism() {
        return parallelism;
    }

    @Override
    protected void updateFormedValid() {
        if (isStructureFormed() && this.getWorld().getTotalWorldTime() % 5 == 0) {
            this.getNetWorkCalculatorHatch().postCPUClusterChangeEvent();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    //仓口
    public INetWorkCalculator getNetWorkCalculatorHatch() {
        List<INetWorkCalculator> abilities = getAbilities(PrismPlanMultiblockAbility.NETWORK_CALCULATOR);
        if (abilities.isEmpty())
            return null;
        return abilities.get(0);
    }

    public List<ICalculatorHatch> getICalculatorHatch() {
        List<ICalculatorHatch> abilities = getAbilities(PrismPlanMultiblockAbility.CALCULATOR_HATCH);
        if (abilities.isEmpty())
            return null;
        return abilities;
    }

    public List<IThreadHatch> getIThreadHatch() {
        List<IThreadHatch> abilities = getAbilities(PrismPlanMultiblockAbility.THREAD_HATCH);
        if (abilities.isEmpty())
            return null;
        return abilities;
    }

    public List<IParallelHatch> getIParallelHatch() {
        List<IParallelHatch> abilities = getAbilities(PrismPlanMultiblockAbility.PARALLEL_HATCH);
        if (abilities.isEmpty())
            return null;
        return abilities;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    //多方块
    protected BlockPattern createStructurePattern() {
        FactoryBlockPattern pattern = FactoryBlockPattern.start(FRONT, UP, RIGHT)
                .aisle("XX", "XX", "XX")
                .aisle("NX", "SX", "XX")
                .aisle("TP", "UF", "TP").setRepeatable(1, 16)
                .aisle("HH", "HH", "HH")
                .where('S', this.selfPredicate())
                .where('N', abilities(PrismPlanMultiblockAbility.NETWORK_CALCULATOR))
                .where('T', abilities(PrismPlanMultiblockAbility.CALCULATOR_HATCH))
                .where('P', abilities(PrismPlanMultiblockAbility.PARALLEL_HATCH))
                .where('F', abilities(PrismPlanMultiblockAbility.THREAD_HATCH))
                .where('X', states(this.getCasingState()))
                .where('U', states(this.getConnectState()))
                .where('H', states(this.getHeatState()));
        return pattern.build();
    }

    protected IBlockState getCasingState() {
        return PrismPlanBlocks.blockMultiblockCasing.getState(MULTI_CASING);
    }
    protected IBlockState getHeatState() {
        return PrismPlanBlocks.blockMultiblockCasing.getState(MULTI_HEAT_VENT);
    }
    protected IBlockState getConnectState() {
        return PrismPlanBlocks.blockMultiblockCasing.getState(MULTI_CONNECT);
    }
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return PrismPlanTextures.MULTI_CASING;
    }

    @Override
    public boolean hasMaintenanceMechanics() {
        return false;
    }

    @Override
    public boolean hasMufflerMechanics() {
        return false;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityCalculatorControl(metaTileEntityId);
    }

    public Levels getLevel() {
        int level = 0;
        for (ICalculatorHatch iCalculatorHatch : getICalculatorHatch()) {
            if (iCalculatorHatch.getTier() > level) level = iCalculatorHatch.getTier();
        }
        return switch (level) {
            case 2 -> Levels.L2;
            case 3 -> Levels.L3;
            case 4 -> Levels.L4;
            case 5 -> Levels.L5;
            default -> Levels.L1;
        };
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        OrientedOverlayRenderer overlayRenderer = Textures.HPCA_OVERLAY;
        overlayRenderer.renderOrientedState(renderState, translation, pipeline, getFrontFacing(), true, isStructureFormed());
    }

    @Override
    public void addInformation(ItemStack stack, World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("prismplan.extendable_calculate_subsystem.info.0"));
        tooltip.add(I18n.format("prismplan.extendable_calculate_subsystem.info.1"));
        tooltip.add(I18n.format("prismplan.extendable_calculate_subsystem.info.2"));
        tooltip.add(I18n.format("prismplan.extendable_calculate_subsystem.info.3"));
        tooltip.add(I18n.format("prismplan.extendable_calculate_subsystem.info.4"));
        tooltip.add(I18n.format("prismplan.extendable_calculate_subsystem.info.5"));
        tooltip.add(I18n.format("prismplan.extendable_calculate_subsystem.info.6"));
        tooltip.add(I18n.format("prismplan.extendable_calculate_subsystem.info.7"));
        tooltip.add(I18n.format("prismplan.extendable_calculate_subsystem.info.8"));
    }

    private void incrementThreshold(Widget.ClickData clickData) {
        this.page = MathHelper.clamp(page + 1, 0, getIThreadHatch().size());
    }

    private void decrementThreshold(Widget.ClickData clickData) {
        this.page = MathHelper.clamp(page - 1, 0, getIThreadHatch().size());
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 300, 240);
        builder.dynamicLabel(8, 4, () -> I18n.format(getMetaFullName()), 0xFFFFFF);

        builder.image(4, 14, 172, 142, GuiTextures.DISPLAY);

        builder.widget((new AdvancedTextWidget(8, 18, this::addDisplayText1, 16777215)).setMaxWidthLimit(180));
        builder.widget((new ProgressWidget(this::getPercent, 6, 68, 168, 3, GuiTextures.PROGRESS_BAR_MULTI_ENERGY_YELLOW, ProgressWidget.MoveType.HORIZONTAL)).setHoverTextConsumer(this::addPercentText));

        builder.widget((new AdvancedTextWidget(8, 74, this::addDisplayText2, 16777215)).setMaxWidthLimit(180));
        builder.widget((new ProgressWidget(() -> getBytesPercent(0), 6, 114, 84, 3, GuiTextures.PROGRESS_BAR_MULTI_ENERGY_YELLOW, ProgressWidget.MoveType.HORIZONTAL)).setHoverTextConsumer((list) -> addBytesText(list, 0)));
        builder.widget((new ProgressWidget(() -> getBytesPercent(1), 90, 114, 84, 3, GuiTextures.PROGRESS_BAR_MULTI_ENERGY_YELLOW, ProgressWidget.MoveType.HORIZONTAL)).setHoverTextConsumer((list) -> addBytesText(list, 1)));

        builder.widget((new AdvancedTextWidget(8, 120, this::addDisplayText3, 16777215)).setMaxWidthLimit(180));


        builder.image(180, 4, 116, 215, GuiTextures.DISPLAY);
        builder.widget((new AdvancedTextWidget(184, 8, this::addCellWatchers, 16777215)).setMaxWidthLimit(120));

        builder.widget(new ClickButtonWidget(179, 218, 60, 18, "Page -1", this::decrementThreshold));
        builder.widget(new ClickButtonWidget(237, 218, 60, 18, "Page +1", this::incrementThreshold));

        builder.bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 8, 160);
        return builder.build(this.getHolder(), entityPlayer);
    }

    private void addDisplayText3(List<ITextComponent> textList) {
        final int totalParallelismPerSecond = getIThreadHatch().stream()
                .flatMap(core -> core.getCpus().stream())
                .map(ECPUCluster::from)
                .map(ECPUCluster::prismplan_ec$getParallelismRecorder)
                .mapToInt(TimeRecorder::usedTimeAvg)
                .sum();
        textList.add(new TextComponentTranslation("总并行度：" + PrimsPlanUtility.formatDecimal(totalParallelismPerSecond) + "/t"));

        final int totalCPUUsagePerSecond = getIThreadHatch().stream()
                .flatMap(core -> core.getCpus().stream())
                .map(ECPUCluster::from)
                .map(ECPUCluster::prismplan_ec$getTimeRecorder)
                .mapToInt(TimeRecorder::usedTimeAvg)
                .sum();
        textList.add(new TextComponentTranslation("性能损耗：" + PrimsPlanUtility.formatDecimal(totalCPUUsagePerSecond) + "µs/t"));

    }

    private void addDisplayText2(List<ITextComponent> textList) {
        textList.add(new TextComponentTranslation("总字节数：" + getTotalBytes()));
        textList.add(new TextComponentTranslation("可用字节：" + getAvailableBytes()));
        textList.add(new TextComponentTranslation("使用字节：" + getUsedBytes()));
    }

    public double getBytesPercent(int i) {
        if (getTotalBytes() == 0) return 0;
        if (i == 0) return (float) getUsedBytes() / getAvailableBytes();
        return (float) getUsedBytes() / getTotalBytes();
    }

    public void addBytesText(List<ITextComponent> hoverList, int i) {
        if (i == 0) {
            ITextComponent cwutInfo = TextComponentUtil.stringWithColor(
                    TextFormatting.AQUA,
                    PrimsPlanUtility.formatNumber(getUsedBytes()) + " / " + PrimsPlanUtility.formatNumber(getAvailableBytes()) + " Bytes");
            hoverList.add(TextComponentUtil.translationWithColor(
                    TextFormatting.GRAY,
                    "字节: %s",
                    cwutInfo));
        }
        if (i == 1) {
            ITextComponent cwutInfo = TextComponentUtil.stringWithColor(
                    TextFormatting.AQUA,
                    PrimsPlanUtility.formatNumber(getUsedBytes()) + " / " + PrimsPlanUtility.formatNumber(getTotalBytes()) + " Bytes");
            hoverList.add(TextComponentUtil.translationWithColor(
                    TextFormatting.GRAY,
                    "字节: %s",
                    cwutInfo));
        }
    }

    public double getPercent() {
        if ((getMaxThreads() + getMaxHyperThreads()) == 0) return 0;
        return (float) getAllCpu() / (getMaxThreads() + getMaxHyperThreads());
    }

    public void addPercentText(List<ITextComponent> hoverList) {
        ITextComponent cwutInfo = TextComponentUtil.stringWithColor(
                TextFormatting.AQUA,
                PrimsPlanUtility.formatNumber(getAllCpu()) + " / " + PrimsPlanUtility.formatNumber(getMaxThreads() + getMaxHyperThreads()) + " VCpus");
        hoverList.add(TextComponentUtil.translationWithColor(
                TextFormatting.GRAY,
                "核心: %s",
                cwutInfo));
    }

    private void addDisplayText1(List<ITextComponent> textList) {
        float percent = (float) getAllCpu() / (getMaxThreads() + getMaxHyperThreads());
        textList.add(new TextComponentTranslation(">>总核心数-" + getIThreadHatch().size() + " 占用率：" + percent));
        textList.add(new TextComponentTranslation("活跃线程：" + getAllCpu()));
        textList.add(new TextComponentTranslation("总数线程：" + getMaxThreads() + "+" + getMaxHyperThreads()));
        textList.add(new TextComponentTranslation("共享线程：" + getSharedParallelism()));
    }

    public int getAllCpu() {
        int cpus = 0;
        for (IThreadHatch iThreadHatch : getIThreadHatch())
            cpus += iThreadHatch.getCpus().size();
        return cpus;
    }

    public int getMaxThreads() {
        int maxThreads = 0;
        for (IThreadHatch iThreadHatch : getIThreadHatch())
            maxThreads += iThreadHatch.getMaxThreads();
        return maxThreads;
    }

    public int getMaxHyperThreads() {
        int getMaxHyperThreads = 0;
        for (IThreadHatch iThreadHatch : getIThreadHatch())
            getMaxHyperThreads += iThreadHatch.getMaxHyperThreads();
        return getMaxHyperThreads;
    }

    private void addCellWatchers(List<ITextComponent> textList) {
        List<IThreadHatch> threadDrives = getIThreadHatch();
        if (threadDrives == null) return;
        if (page >= threadDrives.size()) return;

        IThreadHatch hatch = threadDrives.get(page);

        List<CraftingCPUCluster> cpus = hatch.getCpus();
        int maxThreads = hatch.getMaxThreads();
        int maxHyperThreads = hatch.getMaxHyperThreads();
        float percent = (float) cpus.size() / (maxThreads + maxHyperThreads);

        textList.add(new TextComponentTranslation(">>核心-" + (page + 1) + " 占用率：" + percent));
        textList.add(new TextComponentTranslation("活跃线程：" + cpus.size()));
        textList.add(new TextComponentTranslation("总数线程：" + maxThreads + "+" + maxHyperThreads));
        for (final CraftingCPUCluster cpu : cpus) {
            final IAEItemStack output = cpu.getFinalOutput();
            if (output == null) {
                continue;
            }

            final long count = output.getStackSize();
            final ItemStack stack = output.getCachedItemStack(1);

            textList.add(new TextComponentTranslation("-正在制作：" + stack.getDisplayName() + " x " + count));
        }
    }
}
