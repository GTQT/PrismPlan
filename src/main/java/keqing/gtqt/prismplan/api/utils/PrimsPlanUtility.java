package keqing.gtqt.prismplan.api.utils;

import keqing.gtqt.prismplan.Tags;
import net.minecraft.util.ResourceLocation;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class PrimsPlanUtility {

    public static final int TICK = 1;

    public static final int SECOND = 20;

    public static final int MINUTE = 60 * SECOND;

    public static final int HOUR = 60 * MINUTE;

    public static final int HALF_HOUR = HOUR / 2;

    public static final int QUAT_HOUR = HOUR / 4;

    public static ResourceLocation prismPlanID( String path) {
        return new ResourceLocation(Tags.MOD_ID, path);
    }

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,###.##");

    static {
        DECIMAL_FORMAT.setRoundingMode(RoundingMode.HALF_UP);
    }

    public static String formatDecimal(double value) {
        return DECIMAL_FORMAT.format(value);
    }

    public static String formatNumber(long value) {
        if (value < 1_000L) {
            return String.valueOf(value);
        } else if (value < 1_000_000L) {
            return formatFloat((float) value / 1_000L, 2) + "K";
        } else if (value < 1_000_000_000L) {
            return formatDouble((double) value / 1_000_000L, 2) + "M";
        } else if (value < 1_000_000_000_000L) {
            return formatDouble((double) value / 1_000_000_000L, 2) + "G";
        } else if (value < 1_000_000_000_000_000L) {
            return formatDouble((double) value / 1_000_000_000_000L, 2) + "T";
        } else if (value < 1_000_000_000_000_000_000L) {
            return formatDouble((double) value / 1_000_000_000_000_000L, 2) + "P";
        } else {
            return formatDouble((double) value / 1_000_000_000_000_000_000L, 2) + "E";
        }
    }
    public static String formatFloat(float value, int decimalFraction) {
        return formatDouble(value, decimalFraction);
    }
    public static String formatDouble(double value, int decimalFraction) {
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(decimalFraction);
        return nf.format(value);
    }


}
