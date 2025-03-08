package keqing.gtqt.prismplan.common.item.ae2.ecalculator;

import keqing.gtqt.prismplan.PrismPlan;
import keqing.gtqt.prismplan.Tags;
import keqing.gtqt.prismplan.api.capability.DriveStorageLevel;
import keqing.gtqt.prismplan.api.utils.PrimsPlanUtility;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;
import java.util.List;

import static keqing.gtqt.prismplan.common.CommonProxy.PRISM_PLAN_TAB;

public class ECalculatorCell extends Item {

    public static final ECalculatorCell L1 = new ECalculatorCell(DriveStorageLevel.A, 4);
    public static final ECalculatorCell L2 = new ECalculatorCell(DriveStorageLevel.B, 64);
    public static final ECalculatorCell L3 = new ECalculatorCell(DriveStorageLevel.C, 1024);
    public static final ECalculatorCell L4 = new ECalculatorCell(DriveStorageLevel.D, 16384);
    public static final ECalculatorCell L5 = new ECalculatorCell(DriveStorageLevel.E, 262144);


    protected final DriveStorageLevel level;
    protected final long totalBytes;

    public ECalculatorCell(DriveStorageLevel level, final long millionBytes) {
        this.level = level;
        this.totalBytes = (millionBytes * 1000) * 1024;
        this.setMaxStackSize(1);
        this.setCreativeTab(PRISM_PLAN_TAB);
        this.setRegistryName(new ResourceLocation(Tags.MOD_ID, "ecalculator_cell_" + millionBytes + "m"));
        this.setTranslationKey(Tags.MOD_ID + '.' + "ecalculator_cell_" + millionBytes + "m");
    }

    @Override
    public void addInformation(@Nonnull final ItemStack stack, final World worldIn, final List<String> tooltip, @Nonnull final ITooltipFlag flagIn) {
        tooltip.add(I18n.format("prismplan.ecalculator_cell.insert.tip"));
        tooltip.add(I18n.format("prismplan.ecalculator_cell.extract.tip"));
        tooltip.add(I18n.format("prismplan.ecalculator_cell.tip.0"));
        tooltip.add(I18n.format("prismplan.ecalculator_cell.tip.1"));
        tooltip.add(I18n.format("prismplan.ecalculator_cell.tip.2"));
        final ECalculatorCell cell = (ECalculatorCell) stack.getItem();
        final boolean shiftPressed = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
        tooltip.add(I18n.format("prismplan.ecalculator_cell.tip.3",
                shiftPressed ? PrimsPlanUtility.formatNumber(cell.totalBytes) : PrimsPlanUtility.formatDecimal(cell.totalBytes))
        );
        if (cell == L1) {
            tooltip.add(I18n.format("prismplan.ecalculator_cell.l1.tip"));
        }
        if (cell == L2) {
            tooltip.add(I18n.format("prismplan.ecalculator_cell.l2.tip"));
        }
        if (cell == L3) {
            tooltip.add(I18n.format("prismplan.ecalculator_cell.l3.tip"));
        }
        if (cell == L4) {
            tooltip.add(I18n.format("prismplan.ecalculator_cell.l4.tip"));
        }
        if (cell == L5) {
            tooltip.add(I18n.format("prismplan.ecalculator_cell.l5.tip"));
        }
        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

    public DriveStorageLevel getLevel() {
        return level;
    }

    public long getTotalBytes() {
        return totalBytes;
    }

}
