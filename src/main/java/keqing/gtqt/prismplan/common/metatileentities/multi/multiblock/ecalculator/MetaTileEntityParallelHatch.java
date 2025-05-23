package keqing.gtqt.prismplan.common.metatileentities.multi.multiblock.ecalculator;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.AdvancedTextWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.AbilityInstances;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;
import keqing.gtqt.prismplan.api.capability.IParallelHatch;
import keqing.gtqt.prismplan.api.multiblock.PrismPlanMultiblockAbility;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import java.util.List;

public class MetaTileEntityParallelHatch extends MetaTileEntityMultiblockPart
        implements IMultiblockAbilityPart<IParallelHatch>, IParallelHatch {

    public int parallelism = 0;
    int tier;

    public MetaTileEntityParallelHatch(ResourceLocation metaTileEntityId, int tier, int parallelism) {
        super(metaTileEntityId, tier);
        this.parallelism = parallelism;
        this.tier = tier;
    }

    public int getParallelism() {
        return parallelism;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityParallelHatch(metaTileEntityId, tier, parallelism);
    }


    @Override
    public MultiblockAbility<IParallelHatch> getAbility() {
        return PrismPlanMultiblockAbility.PARALLEL_HATCH;
    }

    @Override
    public void registerAbilities(AbilityInstances abilityInstances) {
        abilityInstances.add(this);
    }

    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (this.shouldRenderOverlay()) {
            if (this.getController() != null && this.getController().isActive()) {
                Textures.HPCA_ADVANCED_COMPUTATION_ACTIVE_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
            } else {
                Textures.HPCA_ADVANCED_DAMAGED_ACTIVE_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
            }
        }
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 180, 240);
        builder.dynamicLabel(8, 12, () -> "并行单元 等级" + tier, 0xFFFFFF);

        builder.image(4, 28, 172, 128, GuiTextures.DISPLAY);
        builder.widget((new AdvancedTextWidget(8, 32, this::addDisplayText, 16777215)).setMaxWidthLimit(180));

        builder.bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 8, 160);
        return builder.build(this.getHolder(), entityPlayer);
    }

    private void addDisplayText(List<ITextComponent> iTextComponents) {
        iTextComponents.add(new TextComponentTranslation("提供修正并行：" + getParallelism()));
    }

    @Override
    public void addInformation(ItemStack stack, World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("prismplan.ecalculator_parallel_proc.info.0"));
        tooltip.add(I18n.format("prismplan.ecalculator_parallel_proc.info.1"));
        tooltip.add(I18n.format("prismplan.ecalculator_parallel_proc.modifiers"));
        tooltip.add(I18n.format("prismplan.ecalculator_parallel_proc.modifier.add", this.getParallelism()));
    }
}
