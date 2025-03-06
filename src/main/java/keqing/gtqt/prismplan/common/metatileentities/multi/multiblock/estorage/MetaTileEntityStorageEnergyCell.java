package keqing.gtqt.prismplan.common.metatileentities.multi.multiblock.estorage;

import appeng.api.config.Actionable;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
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
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.client.utils.PipelineUtil;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;
import keqing.gtqt.prismplan.api.capability.IEnergyHatch;
import keqing.gtqt.prismplan.api.multiblock.PrismPlanMultiblockAbility;
import keqing.gtqt.prismplan.api.utils.PrimsPlanUtility;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.List;

public class MetaTileEntityStorageEnergyCell extends MetaTileEntityMultiblockPart implements
        IMultiblockAbilityPart<IEnergyHatch>,IEnergyHatch,Comparable<IEnergyHatch>{

    protected double energyStored = 0D;
    protected double maxEnergyStore = 0D;

    protected boolean recalculateCap = false;

    int tier;
    public MetaTileEntityStorageEnergyCell(ResourceLocation metaTileEntityId, int tier, final double maxEnergyStore) {
        super(metaTileEntityId, tier);
        this.tier = tier;
        this.maxEnergyStore = maxEnergyStore;
        this.targetItem = new NotifiableItemStackHandler(this, 1, null, false);
    }

    @Override
    public int compareTo(final IEnergyHatch o) {
        return Double.compare(o.getEnergyStored(), energyStored);
    }


    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityStorageEnergyCell(metaTileEntityId,tier,maxEnergyStore);
    }
    protected IItemHandlerModifiable targetItem;
    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 180, 240);
        builder.dynamicLabel(28, 12, () -> "能量元件"+tier, 0xFFFFFF);
        builder.widget(new SlotWidget(targetItem, 0, 8, 8, true, true)
                .setBackgroundTexture(GuiTextures.SLOT)
                .setTooltipText("???"));

        builder.image(4, 28, 172, 128, GuiTextures.DISPLAY);
        builder.widget((new AdvancedTextWidget(8, 32, this::addDisplayText, 16777215)).setMaxWidthLimit(180));

        builder.bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 8, 160);
        return builder.build(this.getHolder(), entityPlayer);
    }

    protected void addDisplayText(List<ITextComponent> textList) {
        textList.add(new TextComponentTranslation("gui.estorage_controller.graph.energy_stored"
                , PrimsPlanUtility.formatNumber(Math.round(getEnergyStored())), PrimsPlanUtility.formatNumber(Math.round( getMaxEnergyStore()))));

    }
    public void recalculateCapacity() {
        recalculateCap = false;
        markDirty();
    }

    public double injectPower(final double amt, final Actionable mode) {
        if (mode == Actionable.SIMULATE) {
            final double fakeBattery = energyStored + amt;
            if (fakeBattery > maxEnergyStore) {
                return fakeBattery - maxEnergyStore;
            }
            return 0;
        }

        if (amt < 0.000001) {
            return 0;
        }
        if (energyStored >= maxEnergyStore) {
            return amt;
        }

        final double maxCanInsert = maxEnergyStore - energyStored;
        final double toInsert = Math.min(amt, maxCanInsert);

        energyStored += toInsert;
        recalculateCap = true;
        return amt - toInsert;
    }

    public double extractPower(final double amt, final Actionable mode) {
        if (mode == Actionable.SIMULATE) {
            return Math.min(this.energyStored, amt);
        }

        if (energyStored <= 0) {
            return 0;
        }

        final double maxCanExtract = energyStored;
        final double toExtract = Math.min(amt, maxCanExtract);

        energyStored -= toExtract;
        recalculateCap = true;
        return toExtract;
    }

    public double getEnergyStored() {
        return energyStored;
    }

    public void setEnergyStored(final double energyStored) {
        this.energyStored = energyStored;
    }

    public double getMaxEnergyStore() {
        return maxEnergyStore;
    }

    public double getFillFactor() {
        return maxEnergyStore == 0 ? 0 : energyStored / maxEnergyStore;
    }

    public boolean shouldRecalculateCap() {
        return recalculateCap;
    }

    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data.setDouble("energyStored", this.energyStored);
        data.setDouble("maxEnergyStore", this.maxEnergyStore);

        return super.writeToNBT(data);
    }

    public void readFromNBT(NBTTagCompound data) {
        this.energyStored = data.getDouble("energyStored");
        this.maxEnergyStore = data.getDouble("maxEnergyStore");

        super.readFromNBT(data);
    }

    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (this.shouldRenderOverlay()) {
            Textures.ENERGY_IN_HI.renderSided(this.getFrontFacing(), renderState, translation, PipelineUtil.color(pipeline, GTValues.VC[this.getTier()]));
        }
    }

    @Override
    public MultiblockAbility<IEnergyHatch> getAbility() {
        return PrismPlanMultiblockAbility.ENERGY_HATCH;
    }

    @Override
    public void registerAbilities(List<IEnergyHatch> abilityList) {
        abilityList.add(this);
    }
}
