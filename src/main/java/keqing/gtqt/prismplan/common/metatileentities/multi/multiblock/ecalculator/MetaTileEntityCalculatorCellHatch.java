package keqing.gtqt.prismplan.common.metatileentities.multi.multiblock.ecalculator;

import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.helpers.ItemHandlerUtil;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.InvOperation;
import appeng.util.inv.filter.IAEItemFilter;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.AdvancedTextWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.AbilityInstances;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;
import keqing.gtqt.prismplan.api.capability.DriveStorageLevel;
import keqing.gtqt.prismplan.api.capability.ICalculatorHatch;
import keqing.gtqt.prismplan.api.multiblock.PrismPlanMultiblockAbility;
import keqing.gtqt.prismplan.common.item.ae2.ecalculator.ECalculatorCell;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static appeng.helpers.ItemStackHelper.stackFromNBT;
import static appeng.helpers.ItemStackHelper.stackWriteToNBT;

public class MetaTileEntityCalculatorCellHatch extends MetaTileEntityMultiblockPart
        implements IMultiblockAbilityPart<ICalculatorHatch>, ICalculatorHatch, IAEAppEngInventory {

    protected final AppEngInternalInventory driveInv = new AppEngInternalInventory(this, 1);
    int tier;

    public MetaTileEntityCalculatorCellHatch(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
        this.tier = tier;
        this.driveInv.setFilter(CellInvFilter.INSTANCE);
    }

    @Override
    public void onRemoval() {
        super.onRemoval();
        var pos = getPos();
        if (!driveInv.getStackInSlot(0).isEmpty()) {
            getWorld().spawnEntity(new EntityItem(getWorld(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, driveInv.getStackInSlot(0)));
            driveInv.extractItem(0, 1, false);
        }
    }

    public NBTTagCompound writeToNBT(NBTTagCompound data) {

        final NBTTagCompound opt = new NBTTagCompound();
        for (int x = 0; x < driveInv.getSlots(); x++) {
            final NBTTagCompound itemNBT = new NBTTagCompound();
            final ItemStack is = driveInv.getStackInSlot(x);
            if (!is.isEmpty()) {
                stackWriteToNBT(is, itemNBT);
            }
            opt.setTag("item" + x, itemNBT);
        }
        data.setTag("driveInv", opt);

        return super.writeToNBT(data);
    }

    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);

        final NBTTagCompound opt = data.getCompoundTag("driveInv");
        for (int x = 0; x < driveInv.getSlots(); x++) {
            final NBTTagCompound item = opt.getCompoundTag("item" + x);
            ItemHandlerUtil.setStackInSlot(driveInv, x, stackFromNBT(item));
        }
    }

    @Override
    public void saveChanges() {
        markDirty();
    }

    public int getTier() {
        return tier;
    }

    @Override
    public void onChangeInventory(final IItemHandler inv, final int slot, final InvOperation mc, final ItemStack removedStack, final ItemStack newStack) {

        final MetaTileEntityCalculatorControl controller = (MetaTileEntityCalculatorControl) getController();
        if (controller != null) {
            controller.recalculateTotalBytes();
            controller.createVirtualCPU();
        }
        this.markDirty();
    }

    public long getSuppliedBytes() {
        final ItemStack stackInSlot = driveInv.getStackInSlot(0);
        if (stackInSlot.isEmpty()) {
            return 0;
        }
        if (!(stackInSlot.getItem() instanceof ECalculatorCell cell)) {
            return 0;
        }


        DriveStorageLevel cellLevel = cell.getLevel();
        switch (cellLevel) {
            case A -> {
                if (tier < 1) {
                    return 0;
                }
            }
            case B -> {
                if (tier < 2) {
                    return 0;
                }
            }
            case C -> {
                if (tier < 3) {
                    return 0;
                }
            }
            case D -> {
                if (tier < 4) {
                    return 0;
                }
            }
            case E -> {
                if (tier < 5) {
                    return 0;
                }
            }
        }

        return cell.getTotalBytes();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityCalculatorCellHatch(metaTileEntityId, tier);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 180, 240);
        builder.dynamicLabel(28, 12, () -> "存储单元仓口 等级" + tier, 0xFFFFFF);
        builder.widget(new SlotWidget(driveInv, 0, 8, 8, true, true)
                .setBackgroundTexture(GuiTextures.SLOT)
                .setTooltipText("输入硬盘"));

        builder.image(4, 28, 172, 128, GuiTextures.DISPLAY);
        builder.widget((new AdvancedTextWidget(8, 32, this::addDisplayText, 16777215)).setMaxWidthLimit(180));

        builder.bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 8, 160);
        return builder.build(this.getHolder(), entityPlayer);
    }

    private void addDisplayText(List<ITextComponent> iTextComponents) {
        iTextComponents.add(new TextComponentTranslation("提供字节数：" + getSuppliedBytes()));
    }

    @Override
    public boolean hasCapability(@Nonnull final Capability<?> capability, @Nullable final EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull final Capability<T> capability, @Nullable final EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(driveInv);
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public MultiblockAbility<ICalculatorHatch> getAbility() {
        return PrismPlanMultiblockAbility.CALCULATOR_HATCH;
    }

    @Override
    public void registerAbilities(AbilityInstances abilityInstances) {
        abilityInstances.add(this);
    }

    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (this.shouldRenderOverlay()) {
            Textures.DATA_ACCESS_HATCH.renderSided(this.getFrontFacing(), renderState, translation, pipeline);
        }
    }

    public AppEngInternalInventory getDriveInv() {
        return this.driveInv;
    }

    @Override
    public void addInformation(ItemStack stack, World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("prismplan.ecalculator_cell_drive.info.0"));
        tooltip.add(I18n.format("prismplan.ecalculator_cell_drive.info.1"));
    }

    private static class CellInvFilter implements IAEItemFilter {

        private static final CellInvFilter INSTANCE = new CellInvFilter();

        @Override
        public boolean allowExtract(IItemHandler inv, int slot, int amount) {
            return true;
        }

        @Override
        public boolean allowInsert(IItemHandler inv, int slot, ItemStack stack) {
            return !stack.isEmpty() && stack.getItem() instanceof ECalculatorCell;
        }
    }
}
