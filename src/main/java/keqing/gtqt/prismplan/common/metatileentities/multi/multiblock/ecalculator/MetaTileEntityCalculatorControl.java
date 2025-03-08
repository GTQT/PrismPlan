package keqing.gtqt.prismplan.common.metatileentities.multi.multiblock.ecalculator;

import appeng.api.util.WorldCoord;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.OrientedOverlayRenderer;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import keqing.gtqt.prismplan.api.capability.*;
import keqing.gtqt.prismplan.api.multiblock.PrismPlanMultiblockAbility;
import keqing.gtqt.prismplan.api.utils.PrismPlanLog;
import keqing.gtqt.prismplan.common.metatileentities.multi.multiblock.estorage.MetaTileEntityStorageCellControl;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

import static gregtech.api.util.RelativeDirection.*;

public class MetaTileEntityCalculatorControl extends MultiblockWithDisplayBase {

    protected CraftingCPUCluster virtualCPU = null;
    protected int parallelism = 0;
    protected long totalBytes = 0;


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
        parallelism=0;
        for(IParallelHatch parallelProc : getIParallelHatch())
            parallelism+=parallelProc.getParallelism();

        // Update accelerators
        getIThreadHatch().forEach(threadCore -> threadCore.getCpus().stream()
                .map(ECPUCluster::from)
                .forEach(ecpuCluster -> ecpuCluster.novaeng_ec$setAccelerators(this.parallelism))
        );
    }

    protected void recalculateTotalBytes() {
        totalBytes=0;
        for(ICalculatorHatch calculatorHatch : getICalculatorHatch())
            totalBytes+=calculatorHatch.getSuppliedBytes();

    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public long getAvailableBytes() {
        long usedStorage=0;
        for(IThreadHatch iThreadHatch : getIThreadHatch())
            usedStorage+=iThreadHatch.getUsedStorage();

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
                ecpuCluster.novaeng_ec$setAvailableStorage(usedBytes);
                ecpuCluster.novaeng_ec$setVirtualCPUOwner(null);
                this.virtualCPU = null;
                createVirtualCPU();
                return;
            }
        }
        for (final IThreadHatch threadCore : getIThreadHatch()) {
            if (threadCore.addCPU(virtualCPU, true)) {
                ECPUCluster ecpuCluster = ECPUCluster.from(this.virtualCPU);
                final long usedExtraBytes = (long) (usedBytes * 0.1F);
                ecpuCluster.novaeng_ec$setAvailableStorage(usedBytes + usedExtraBytes);
                ecpuCluster.novaeng_ec$setUsedExtraStorage(usedExtraBytes);
                ecpuCluster.novaeng_ec$setVirtualCPUOwner(null);
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
            eCluster.novaeng_ec$setAvailableStorage(availableBytes);
            eCluster.novaeng_ec$setAccelerators(parallelism);
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
        eCluster.novaeng_ec$setVirtualCPUOwner(this);
        eCluster.novaeng_ec$setAvailableStorage(availableBytes);
        eCluster.novaeng_ec$setAccelerators(parallelism);

        if (getNetWorkCalculatorHatch() != null) {
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
            ECPUCluster.from(this.virtualCPU).novaeng_ec$setVirtualCPUOwner(this);
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
                .aisle("TP", "XF", "TP").setRepeatable(1, 16)
                .aisle("XX", "XX", "XX")
                .where('S', this.selfPredicate())
                .where('N', abilities(PrismPlanMultiblockAbility.NETWORK_CALCULATOR))
                .where('T', abilities(PrismPlanMultiblockAbility.CALCULATOR_HATCH))
                .where('P', abilities(PrismPlanMultiblockAbility.PARALLEL_HATCH))
                .where('F', abilities(PrismPlanMultiblockAbility.THREAD_HATCH))
                .where('X', states(this.getCasingState()));
        return pattern.build();
    }

    private IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
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
        for(ICalculatorHatch iCalculatorHatch : getICalculatorHatch())
        {
            if(iCalculatorHatch.getTier()>level)level=iCalculatorHatch.getTier();
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
}
