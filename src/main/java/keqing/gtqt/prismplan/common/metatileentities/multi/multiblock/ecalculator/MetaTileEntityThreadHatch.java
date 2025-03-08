package keqing.gtqt.prismplan.common.metatileentities.multi.multiblock.ecalculator;

import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.WorldCoord;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.AdvancedTextWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.PipelineUtil;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import keqing.gtqt.prismplan.api.capability.IThreadHatch;
import keqing.gtqt.prismplan.api.capability.Sides;
import keqing.gtqt.prismplan.api.multiblock.PrismPlanMultiblockAbility;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.List;

public class MetaTileEntityThreadHatch extends MetaTileEntityMultiblockPart
        implements IMultiblockAbilityPart<IThreadHatch>, IThreadHatch {


    private static final ThreadLocal<Boolean> WRITE_CPU_NBT = ThreadLocal.withInitial(() -> true);

    protected final ObjectArrayList<CraftingCPUCluster> cpus = new ObjectArrayList<>();

    protected int threads = 0;
    protected int maxThreads = 0;
    protected int maxHyperThreads = 0;

    public MetaTileEntityThreadHatch(ResourceLocation metaTileEntityId, int maxThreads, int maxHyperThreads) {
        super(metaTileEntityId, 1);
        this.maxThreads = maxThreads;
        this.maxHyperThreads = maxHyperThreads;
    }

    public List<CraftingCPUCluster> getCpus() {
        return cpus;
    }

    public boolean addCPU(final CraftingCPUCluster cluster, final boolean hyperThread) {
        if (cpus.size() >= maxThreads) {
            if (!hyperThread || cpus.size() >= maxThreads + maxHyperThreads) {
                return false;
            }
        }

        final boolean prevEmpty = cpus.isEmpty();

        ECPUCluster.from(cluster).prismplan_ec$setThreadCore(this);
        cpus.add(cluster);

        if (prevEmpty) {
            markDirty();
        }
        return true;
    }

    public boolean canAddCPU() {
        return cpus.size() < (maxThreads + maxHyperThreads);
    }

    /**
     * Client side only.
     */
    public int getThreads() {
        return threads;
    }


    public int getMaxThreads() {
        return maxThreads;
    }

    public int getMaxHyperThreads() {
        return maxHyperThreads;
    }

    public void refreshCPUSource() {
        for (final CraftingCPUCluster cluster : cpus) {
            ECPUCluster eCluster = ECPUCluster.from(cluster);
            eCluster.prismplan_ec$setThreadCore(this);
        }
    }

    @Override
    public void onRemoval() {
        super.onRemoval();
        onBlockDestroyed();
    }

    public void onBlockDestroyed() {
        cpus.forEach(cluster -> ECPUCluster.from(cluster).prismplan_ec$markDestroyed());
        cpus.clear();
        MetaTileEntityCalculatorControl controller = (MetaTileEntityCalculatorControl) getController();
        if (controller != null && controller.getNetWorkCalculatorHatch().getProxy() != null) {
            controller.onClusterChanged();
        }
    }

    public void onCPUDestroyed(final CraftingCPUCluster cluster) {
        cpus.remove(cluster);
        MetaTileEntityCalculatorControl controller = (MetaTileEntityCalculatorControl) getController();
        if (controller != null) {
            controller.onClusterChanged();
        }
        if (cpus.isEmpty()) {
            markDirty();
        }
    }

    public long getUsedStorage() {
        if (cpus.isEmpty()) {
            return 0L;
        }
        return cpus.stream().mapToLong(CraftingCPUCluster::getAvailableStorage).sum();
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        if (compound.hasKey("maxClusterCount")) {
            this.maxThreads = compound.getByte("maxClusterCount");
        }
        if (compound.hasKey("maxClusterCountHyperThread")) {
            this.maxHyperThreads = compound.getByte("maxClusterCountHyperThread");
        }
        this.threads = compound.getByte("threads");

        readCPUNBT(compound);
    }

    public void readCPUNBT(final NBTTagCompound compound) {
        cpus.clone().forEach(CraftingCPUCluster::destroy);
        cpus.clear();

        final NBTTagList clustersTag = compound.getTagList("clusters", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < clustersTag.tagCount(); i++) {
            NBTTagCompound clusterTag = clustersTag.getCompoundTagAt(i);

            WorldCoord coord = new WorldCoord(getPos());
            CraftingCPUCluster cluster = new CraftingCPUCluster(coord, coord);
            ECPUCluster eCluster = ECPUCluster.from(cluster);

            eCluster.prismplan_ec$setThreadCore(this);
            eCluster.prismplan_ec$setAvailableStorage(clusterTag.getLong("availableStorage"));
            eCluster.prismplan_ec$setUsedExtraStorage(clusterTag.getLong("usedExtraStorage"));
            cluster.readFromNBT(clusterTag);
            cpus.add(cluster);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        compound.setByte("maxClusterCount", (byte) maxThreads);
        compound.setByte("maxClusterCountHyperThread", (byte) maxHyperThreads);
        compound.setByte("threads", (byte) this.cpus.size());

        if (Sides.isRunningOnClient() || (Sides.isRunningOnServer() && WRITE_CPU_NBT.get())) {
            writeCPUNBT(compound);
        }

        return super.writeToNBT(compound);
    }

    public void writeCPUNBT(final NBTTagCompound compound) {
        final NBTTagList clustersTag = new NBTTagList();
        cpus.forEach(cluster -> {
            ECPUCluster eCluster = ECPUCluster.from(cluster);
            NBTTagCompound clusterTag = new NBTTagCompound();
            cluster.writeToNBT(clusterTag);
            clusterTag.setLong("availableStorage", cluster.getAvailableStorage());
            clusterTag.setLong("usedExtraStorage", eCluster.prismplan_ec$getUsedExtraStorage());
            clustersTag.appendTag(clusterTag);
        });
        compound.setTag("clusters", clustersTag);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityThreadHatch(metaTileEntityId, maxThreads, maxHyperThreads);
    }

    @Override
    public MultiblockAbility<IThreadHatch> getAbility() {
        return PrismPlanMultiblockAbility.THREAD_HATCH;
    }

    @Override
    public void registerAbilities(List<IThreadHatch> abilityList) {
        abilityList.add(this);
    }

    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (this.shouldRenderOverlay()) {
            Textures.FUSION_REACTOR_OVERLAY.renderSided(this.getFrontFacing(), renderState, translation, PipelineUtil.color(pipeline, GTValues.VC[this.getTier()]));
        }
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 180, 240);
        builder.dynamicLabel(8, 12, () -> "线程单元", 0xFFFFFF);

        builder.image(4, 28, 172, 128, GuiTextures.DISPLAY);
        builder.widget((new AdvancedTextWidget(8, 32, this::addDisplayText, 16777215)).setMaxWidthLimit(180));

        builder.bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 8, 160);
        return builder.build(this.getHolder(), entityPlayer);
    }

    private void addDisplayText(List<ITextComponent> textList) {
        List<CraftingCPUCluster> cpus = this.getCpus();
        int maxThreads = this.getMaxThreads();
        int maxHyperThreads = this.getMaxHyperThreads();
        float percent = (float) cpus.size() / (maxThreads + maxHyperThreads);
        textList.add(new TextComponentTranslation(">>核心占用率：" + percent));
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

    @Override
    public void addInformation(ItemStack stack, World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);

        if (maxHyperThreads > 0) {
            tooltip.add(I18n.format("prismplan.ecalculator_thread_core_hyper.info.0"));
            tooltip.add(I18n.format("prismplan.ecalculator_thread_core_hyper.info.1"));
            tooltip.add(I18n.format("prismplan.ecalculator_thread_core_hyper.info.2"));
            tooltip.add(I18n.format("prismplan.ecalculator_thread_core.modifiers"));
            tooltip.add(I18n.format("prismplan.ecalculator_thread_core.modifier.add", this.getMaxThreads()));
            tooltip.add(I18n.format("prismplan.ecalculator_thread_core_hyper.modifier.add", this.getMaxHyperThreads()));
        } else {
            tooltip.add(I18n.format("prismplan.ecalculator_thread_core.info.0"));
            tooltip.add(I18n.format("prismplan.ecalculator_thread_core.info.1"));
            tooltip.add(I18n.format("prismplan.ecalculator_thread_core.info.2"));
            tooltip.add(I18n.format("prismplan.ecalculator_thread_core.modifiers"));
            tooltip.add(I18n.format("prismplan.ecalculator_thread_core.modifier.add", this.getMaxThreads()));
        }

    }
}
