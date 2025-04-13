package keqing.gtqt.prismplan.api.utils;


import java.awt.*;

public class ColorUtils {

    public static final Color LOW_COLOR = new Color(0xCC54FF9F);
    public static final Color MID_COLOR = new Color(0xCCFFFF00);
    public static final Color FULL_COLOR = new Color(0xCCFF4500);


    public static int darkenColor(int color, double factor) {
        int a = (color >> 24) & 0xFF;
        int r = (int) (((color >> 16) & 0xFF) * factor);
        int g = (int) (((color >> 8) & 0xFF) * factor);
        int b = (int) ((color & 0xFF) * factor);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static int lightenColor(int color, double factor) {
        int a = (color >> 24) & 0xFF;
        int r = Math.min(255, (int) (((color >> 16) & 0xFF) / factor));
        int g = Math.min(255, (int) (((color >> 8) & 0xFF) / factor));
        int b = Math.min(255, (int) ((color & 0xFF) / factor));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static Color getGradientColor(final Color[] colors,
                                         final int alpha,
                                         final float percentage) {
        float percent = Math.max(0, Math.min(1, percentage));

        // 确保颜色数组和百分比数组长度相同
        if (colors.length < 2) {
            throw new IllegalArgumentException("Colors array must contain at least two colors.");
        }

        // 计算渐变段数
        int numSegments = colors.length - 1;

        // 计算当前百分比位于哪个渐变段
        float segmentPercent = percent * numSegments;
        int segmentIndex = (int) segmentPercent;
        if (segmentIndex >= numSegments) {
            // 如果超出最大渐变段数，则取最后一个渐变段
            return colors[numSegments];
        } else if (segmentIndex < 0) {
            // 如果百分比小于 0，则取第一个渐变段
            return colors[0];
        }

        // 获取当前渐变段的起始颜色和结束颜色
        Color startColor = colors[segmentIndex];
        Color endColor = colors[segmentIndex + 1];

        // 计算当前渐变段内的百分比
        float segmentPercentage = segmentPercent - segmentIndex;

        // 计算当前渐变段内的颜色
        int interpolatedRed = (int) (startColor.getRed() + (endColor.getRed() - startColor.getRed()) * segmentPercentage);
        int interpolatedGreen = (int) (startColor.getGreen() + (endColor.getGreen() - startColor.getGreen()) * segmentPercentage);
        int interpolatedBlue = (int) (startColor.getBlue() + (endColor.getBlue() - startColor.getBlue()) * segmentPercentage);

        // 返回计算得到的颜色，带有指定的透明度
        return new Color(interpolatedRed, interpolatedGreen, interpolatedBlue, alpha);
    }

}
