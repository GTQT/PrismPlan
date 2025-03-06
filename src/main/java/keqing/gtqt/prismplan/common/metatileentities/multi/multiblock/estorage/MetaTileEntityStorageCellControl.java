package keqing.gtqt.prismplan.common.metatileentities.multi.multiblock.estorage;

import appeng.api.config.Actionable;
import appeng.api.storage.ICellInventory;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.data.IAEItemStack;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.AdvancedTextWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import keqing.gtqt.prismplan.api.capability.ICellHatch;
import keqing.gtqt.prismplan.api.capability.IEnergyHatch;
import keqing.gtqt.prismplan.api.capability.INetWorkStore;
import keqing.gtqt.prismplan.api.multiblock.PrismPlanMultiblockAbility;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.*;

import static gregtech.api.util.RelativeDirection.*;

public class MetaTileEntityStorageCellControl extends MultiblockWithDisplayBase {

    protected final Queue<IEnergyHatch> energyCellsMin = new PriorityQueue<>(
            Comparator.comparingDouble(IEnergyHatch::getRemainingCapacity).reversed()
    );
    protected final Queue<IEnergyHatch> energyCellsMax = new PriorityQueue<>(
            Comparator.comparingDouble(IEnergyHatch::getEnergyStored).reversed()
    );

    // 定义常量提升可读性
    protected double idleDrain = 64;

    long[] usedBytes = new long[2];
    long[] maxBytes = new long[2];
    int[] usedTypes = new int[2];
    int[] maxTypes = new int[2];

    public MetaTileEntityStorageCellControl(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        // 从多方块结构中获取所有能量仓口
        List<IEnergyHatch> cells = getCellHatch();
        if (cells != null) {
            energyCellsMin.addAll(cells);
            energyCellsMax.addAll(cells);
        }
    }

    @Override
    protected void updateFormedValid() {
        if (isStructureFormed() && this.getWorld().getTotalWorldTime() % 5 == 0) {
            this.energyCellsMax.forEach(cell -> {
                if (cell.shouldRecalculateCap()) {
                    cell.recalculateCapacity();
                }
            });

            getNetWorkStoreHatch().refresh();

            long[] currentUsedBytes = new long[2];
            long[] currentMaxBytes = new long[2];
            int[] currentUsedTypes = new int[2];
            int[] currentMaxTypes = new int[2];

            for (ICellHatch cell : getCellDrives()) {
                if (cell.getData() == null) continue;
                int index = switch (cell.getType()) {
                    case ITEM -> 0;
                    case FLUID -> 1;
                    default -> -1;
                };
                if (index == -1) continue;

                currentUsedBytes[index] += cell.usedBytes();
                currentMaxBytes[index] += cell.maxBytes();
                currentUsedTypes[index] += cell.usedTypes();
                currentMaxTypes[index] += cell.maxTypes();
            }

            // 使用内容比较替代引用比较
            if (!Arrays.equals(usedBytes, currentUsedBytes)) usedBytes = currentUsedBytes;
            if (!Arrays.equals(maxBytes, currentMaxBytes)) maxBytes = currentMaxBytes;
            if (!Arrays.equals(usedTypes, currentUsedTypes)) usedTypes = currentUsedTypes;
            if (!Arrays.equals(maxTypes, currentMaxTypes)) maxTypes = currentMaxTypes;
        }


    }

    public double injectPower(final double amt, final Actionable mode) {
        double toInject = amt;

        if (mode == Actionable.SIMULATE) {
            for (final IEnergyHatch cell : energyCellsMin) {
                double prev = toInject;
                toInject -= (toInject - cell.injectPower(toInject, mode));
                if (toInject <= 0 || prev == toInject) {
                    break;
                }
            }
            return toInject;
        }

        List<IEnergyHatch> toReInsert = new LinkedList<>();
        IEnergyHatch cell;
        while ((cell = energyCellsMin.poll()) != null) {
            double prev = toInject;
            toInject -= (toInject - cell.injectPower(toInject, mode));
            toReInsert.add(cell);
            if (toInject <= 0 || prev < toInject) {
                break;
            }
        }

        if (!toReInsert.isEmpty()) {
            energyCellsMin.addAll(toReInsert);
        }

        return toInject;
    }

    public double extractPower(final double amt, final Actionable mode) {
        double extracted = 0;

        if (mode == Actionable.SIMULATE) {
            for (final IEnergyHatch cell : energyCellsMax) {
                double prev = extracted;
                extracted += cell.extractPower(amt - extracted, mode);
                if (extracted >= amt || prev >= extracted) {
                    break;
                }
            }
            return extracted;
        }

        IEnergyHatch cell;
        List<IEnergyHatch> toReInsert = new LinkedList<>();
        while ((cell = energyCellsMax.poll()) != null) {
            double prev = extracted;
            extracted += cell.extractPower(amt - extracted, mode);
            toReInsert.add(cell);
            if (extracted >= amt || prev == extracted) {
                break;
            }
        }

        if (!toReInsert.isEmpty()) {
            energyCellsMax.addAll(toReInsert);
        }

        return extracted;
    }

    public void recalculateEnergyUsage() {
        if (!isStructureFormed()) return;
        double newIdleDrain = 64;
        for (final ICellHatch drive : getCellDrives()) {
            ECellDriveWatcher<IAEItemStack> watcher = drive.getWatcher();
            if (watcher == null) {
                continue;
            }
            ICellInventoryHandler<?> cellInventory = (ICellInventoryHandler<?>) watcher.getInternal();
            if (cellInventory == null) {
                continue;
            }
            ICellInventory<?> cellInv = cellInventory.getCellInv();
            if (cellInv == null) {
                continue;
            }
            newIdleDrain += cellInv.getIdleDrain();
        }
        this.idleDrain = newIdleDrain;
        if (this.getNetWorkStoreHatch() != null && this.getNetWorkStoreHatch().getProxy() != null) {
            this.getNetWorkStoreHatch().getProxy().setIdlePowerUsage(idleDrain);
        }
    }

    public double getEnergyStored() {
        double energyStored = 0;
        for (final IEnergyHatch cell : energyCellsMax) {
            double stored = cell.getEnergyStored();
            if (stored <= 0.000001) {
                break;
            }
            energyStored += stored;
        }
        return energyStored;
    }

    public double getMaxEnergyStore() {
        double maxEnergyStore = 0;
        for (final IEnergyHatch energyCell : energyCellsMax) {
            maxEnergyStore += energyCell.getMaxEnergyStore();
        }
        return maxEnergyStore;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    //仓口
    public INetWorkStore getNetWorkStoreHatch() {
        List<INetWorkStore> abilities = getAbilities(PrismPlanMultiblockAbility.NETWORK_STORE);
        if (abilities.isEmpty())
            return null;
        return abilities.get(0);
    }

    public List<IEnergyHatch> getCellHatch() {
        List<IEnergyHatch> abilities = getAbilities(PrismPlanMultiblockAbility.ENERGY_HATCH);
        if (abilities.isEmpty())
            return null;
        return abilities;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    //多方块
    protected BlockPattern createStructurePattern() {
        FactoryBlockPattern pattern = FactoryBlockPattern.start(FRONT, UP, RIGHT)
                .aisle("NX", "SX", "XX")
                .aisle("XX", "XX", "XX")
                .aisle("TJ", "TX", "TJ").setRepeatable(1, 16)
                .aisle("XX", "XX", "XX")
                .where('S', this.selfPredicate())
                .where('N', abilities(PrismPlanMultiblockAbility.NETWORK_STORE))
                .where('J', states(this.getCasingState())
                        .or(abilities(PrismPlanMultiblockAbility.ENERGY_HATCH)))
                .where('T', abilities(PrismPlanMultiblockAbility.CELL_HATCH))
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
        return new MetaTileEntityStorageCellControl(metaTileEntityId);
    }

    public List<ICellHatch> getCellDrives() {
        return getAbilities(PrismPlanMultiblockAbility.CELL_HATCH);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 180, 240);
        builder.dynamicLabel(8, 12, () -> "ECO S", 0xFFFFFF);

        builder.image(4, 28, 172, 128, GuiTextures.DISPLAY);
        builder.widget((new AdvancedTextWidget(8, 32, this::addDisplayText, 16777215)).setMaxWidthLimit(180));

        builder.bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 8, 160);
        return builder.build(this.getHolder(), entityPlayer);
    }

    protected void addDisplayText(List<ITextComponent> textList) {
        textList.add(new TextComponentTranslation("物品："));
        textList.add(new TextComponentTranslation("容量" + usedBytes[0] + " / " + maxBytes[0]));
        textList.add(new TextComponentTranslation("类型" + usedTypes[0] + " / " + maxTypes[0]));
        textList.add(new TextComponentTranslation("流体："));
        textList.add(new TextComponentTranslation("容量" + usedBytes[1] + " / " + maxBytes[1]));
        textList.add(new TextComponentTranslation("类型" + usedTypes[1] + " / " + maxTypes[1]));
    }
}
