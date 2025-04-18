package keqing.gtqt.prismplan.integration.theoneprobe;

import appeng.api.storage.data.IAEItemStack;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import keqing.gtqt.prismplan.Tags;
import keqing.gtqt.prismplan.api.capability.IThreadHatch;
import keqing.gtqt.prismplan.api.utils.ColorUtils;
import keqing.gtqt.prismplan.api.utils.PrimsPlanUtility;
import keqing.gtqt.prismplan.api.utils.TimeRecorder;
import keqing.gtqt.prismplan.common.metatileentities.multi.multiblock.ecalculator.*;
import mcjty.theoneprobe.api.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.awt.*;
import java.util.List;

import static keqing.gtqt.prismplan.api.utils.ColorUtils.*;
import static keqing.gtqt.prismplan.api.utils.PrimsPlanUtility.formatNumber;

public class ECalculatorInfoProvider implements IProbeInfoProvider {

    public static final ECalculatorInfoProvider INSTANCE = new ECalculatorInfoProvider();

    private static final int CPU_USAGE_GREEN_THRESHOLD = 400;
    private static final int CPU_USAGE_YELLOW_THRESHOLD = 800;
    private static final int CPU_USAGE_RED_THRESHOLD = 1200;

    private static final int GLOBAL_CPU_USAGE_GREEN_THRESHOLD = 2000;
    private static final int GLOBAL_CPU_USAGE_YELLOW_THRESHOLD = 4000;
    private static final int GLOBAL_CPU_USAGE_RED_THRESHOLD = 6000;

    private ECalculatorInfoProvider() {
    }

    private static void processThreadCoreInfo(final IProbeInfo probeInfo, final MetaTileEntityThreadHatch threadCore) {
        final boolean online = threadCore.getController() != null;
        IProbeInfo box = newBox(probeInfo);
        box.text("{*top.ecalculator.thread_core.status*}" +
                (online ? "{*top.ecalculator.thread_core.status.online*}" : "{*top.ecalculator.thread_core.status.offline*}")
        );
        if (!online) {
            return;
        }

        final List<CraftingCPUCluster> cpus = threadCore.getCpus();
        final int maxThreads = threadCore.getMaxThreads();
        final int maxHyperThreads = threadCore.getMaxHyperThreads();
        float percent = (float) cpus.size() / (maxThreads + maxHyperThreads);
        final int color = ColorUtils.getGradientColor(new Color[]{
                LOW_COLOR, LOW_COLOR, LOW_COLOR, MID_COLOR, MID_COLOR, FULL_COLOR, FULL_COLOR
        }, 0xCC, percent).getRGB();
        final String progressStr = String.format("%s / %s %s(+%s)%s", cpus.size(), maxThreads, TextFormatting.YELLOW, maxHyperThreads, TextFormatting.RESET);
        newBox(probeInfo).horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER))
                .text(TextFormatting.AQUA + "{*top.ecalculator.thread_core.threads*}")
                .progress((int) (percent * 75), 75, probeInfo.defaultProgressStyle()
                        .prefix(progressStr)
                        .filledColor(color)
                        .alternateFilledColor(darkenColor(color, .8))
                        .borderColor(lightenColor(color, .8))
                        .backgroundColor(0xFF000000)
                        .numberFormat(NumberFormat.NONE)
                        .width(75)
                );

        if (cpus.isEmpty()) {
            return;
        }

        box = newBox(probeInfo).vertical();
        box.text("{*top.ecalculator.thread_core.crafting*}");
        for (final CraftingCPUCluster cpu : cpus) {
            final IAEItemStack output = cpu.getFinalOutput();
            if (output == null) {
                continue;
            }

            final IProbeInfo taskBox = newBox(box).vertical();
            IProbeInfo row = taskBox.horizontal(taskBox.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER));

            final long count = output.getStackSize();
            final ItemStack stack = output.getCachedItemStack(1);
            row.item(stack);
            row.itemLabel(stack);
            row.text("  x " + count);

            final ECPUCluster ecpu = ECPUCluster.from(cpu);
            int cpuUsagePerTick = ecpu.prismplan_ec$getTimeRecorder().usedTimeAvg();
            int parallelismPerTick = ecpu.prismplan_ec$getParallelismRecorder().usedTimeAvg();
            row = taskBox.horizontal();

            TextFormatting cpuUsageColor;
            if (cpuUsagePerTick < CPU_USAGE_GREEN_THRESHOLD) {
                cpuUsageColor = TextFormatting.GREEN;
            } else if (cpuUsagePerTick < CPU_USAGE_YELLOW_THRESHOLD) {
                cpuUsageColor = TextFormatting.YELLOW;
            } else if (cpuUsagePerTick < CPU_USAGE_RED_THRESHOLD) {
                cpuUsageColor = TextFormatting.RED;
            } else {
                cpuUsageColor = TextFormatting.DARK_RED;
            }

            row.text("{*top.ecalculator.thread_core.avg_time_usage*}" + cpuUsageColor + cpuUsagePerTick + TextFormatting.RESET + "µs/t");
            row.text("{*top.ecalculator.thread_core.avg_parallelism*}" + TextFormatting.DARK_PURPLE + parallelismPerTick + "/t");
        }
    }

    private static void processControllerInfo(final IProbeInfo probeInfo, final MetaTileEntityCalculatorControl controller) {
        if (!controller.isStructureFormed()) {
            return;
        }
        final long totalMemory = controller.getTotalBytes();
        final long usedMemory = controller.getUsedBytes();
        float percent = (float) usedMemory / totalMemory;
        int color = ColorUtils.getGradientColor(new Color[]{
                LOW_COLOR, LOW_COLOR, LOW_COLOR, MID_COLOR, MID_COLOR, FULL_COLOR, FULL_COLOR
        }, 0xCC, percent).getRGB();
        String progressStr = String.format("%s / %s", formatNumber(usedMemory), formatNumber(totalMemory));
        newBox(probeInfo).horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER))
                .text(TextFormatting.AQUA + "{*top.ecalculator.controller.storage*}")
                .progress((int) (percent * 150), 150, probeInfo.defaultProgressStyle()
                        .prefix(progressStr)
                        .filledColor(color)
                        .alternateFilledColor(darkenColor(color, .8))
                        .borderColor(lightenColor(color, .8))
                        .backgroundColor(0xFF000000)
                        .numberFormat(NumberFormat.NONE)
                        .width(150)
                );

        final int totalWorking = controller.getIThreadHatch().stream().mapToInt(core -> core.getCpus().size()).sum();
        final int maxThreads = controller.getIThreadHatch().stream().mapToInt(IThreadHatch::getMaxThreads).sum();
        final int maxHyperThreads = controller.getIThreadHatch().stream().mapToInt(IThreadHatch::getMaxHyperThreads).sum();
        percent = (float) totalWorking / (maxThreads + maxHyperThreads);
        color = ColorUtils.getGradientColor(new Color[]{
                LOW_COLOR, LOW_COLOR, LOW_COLOR, MID_COLOR, MID_COLOR, FULL_COLOR, FULL_COLOR
        }, 0xCC, percent).getRGB();
        progressStr = String.format("%s / %s %s(+%s)%s", totalWorking, maxThreads, TextFormatting.YELLOW, maxHyperThreads, TextFormatting.RESET);
        newBox(probeInfo).horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER))
                .text(TextFormatting.AQUA + "{*top.ecalculator.controller.threads*}")
                .progress((int) (percent * 100), 100, probeInfo.defaultProgressStyle()
                        .prefix(progressStr)
                        .filledColor(color)
                        .alternateFilledColor(darkenColor(color, .8))
                        .borderColor(lightenColor(color, .8))
                        .backgroundColor(0xFF000000)
                        .numberFormat(NumberFormat.NONE)
                        .width(100)
                );

        final IProbeInfo box = newBox(probeInfo);
        final IProbeInfo leftInfo = newVertical(box);
        final IProbeInfo rightInfo = newVertical(box);

        final int parallelism = controller.getSharedParallelism();
        leftInfo.text("{*top.ecalculator.controller.parallelism*}");
        rightInfo.text(TextFormatting.DARK_PURPLE + PrimsPlanUtility.formatDecimal(parallelism));

        final int totalParallelismPerSecond = controller.getIThreadHatch().stream()
                .flatMap(core -> core.getCpus().stream())
                .map(ECPUCluster::from)
                .map(ECPUCluster::prismplan_ec$getParallelismRecorder)
                .mapToInt(TimeRecorder::usedTimeAvg)
                .sum();

        leftInfo.text("{*top.ecalculator.controller.avg_parallelism*}");
        rightInfo.text(TextFormatting.DARK_PURPLE + PrimsPlanUtility.formatDecimal(totalParallelismPerSecond) + "/t");

        final int totalCPUUsagePerSecond = controller.getIThreadHatch().stream()
                .flatMap(core -> core.getCpus().stream())
                .map(ECPUCluster::from)
                .map(ECPUCluster::prismplan_ec$getTimeRecorder)
                .mapToInt(TimeRecorder::usedTimeAvg)
                .sum();

        TextFormatting cpuUsageColor;
        if (totalCPUUsagePerSecond < GLOBAL_CPU_USAGE_GREEN_THRESHOLD) {
            cpuUsageColor = TextFormatting.GREEN;
        } else if (totalCPUUsagePerSecond < GLOBAL_CPU_USAGE_YELLOW_THRESHOLD) {
            cpuUsageColor = TextFormatting.YELLOW;
        } else if (totalCPUUsagePerSecond < GLOBAL_CPU_USAGE_RED_THRESHOLD) {
            cpuUsageColor = TextFormatting.RED;
        } else {
            cpuUsageColor = TextFormatting.DARK_RED;
        }

        leftInfo.text("{*top.ecalculator.controller.avg_time_usage*}");
        rightInfo.text(cpuUsageColor + PrimsPlanUtility.formatDecimal(totalCPUUsagePerSecond) + "µs/t");
    }

    private static IProbeInfo newVertical(final IProbeInfo probeInfo) {
        return probeInfo.vertical(probeInfo.defaultLayoutStyle().spacing(0));
    }

    private static IProbeInfo newBox(final IProbeInfo info) {
        return info.horizontal(info.defaultLayoutStyle().borderColor(0x801E90FF));
    }

    @Override
    public String getID() {
        return Tags.MOD_ID + ':' + "ecalculator_info_provider";
    }

    @Override
    public void addProbeInfo(final ProbeMode mode, final IProbeInfo probeInfo, final EntityPlayer player, final World world, final IBlockState blockState, final IProbeHitData data) {
        final TileEntity te = world.getTileEntity(data.getPos());

        if (te instanceof IGregTechTileEntity igtte) {
            MetaTileEntity mte = igtte.getMetaTileEntity();

            if (mte instanceof final MetaTileEntityCalculatorControl controller) {
                processControllerInfo(probeInfo, controller);
                return;
            }
            if (mte instanceof final MetaTileEntityThreadHatch hatch) {
                processThreadCoreInfo(probeInfo, hatch);
            }
            if (mte instanceof final MetaTileEntityCalculatorCellHatch hatch) {
                processCellHatchInfo(probeInfo, hatch);
            }
            if (mte instanceof final MetaTileEntityParallelHatch hatch) {
                processParallelHatchInfo(probeInfo, hatch);
            }
        }
    }

    private void processParallelHatchInfo(IProbeInfo probeInfo, MetaTileEntityParallelHatch hatch) {
        final boolean online = hatch.getController() != null;
        IProbeInfo box = newBox(probeInfo);
        box.text("{*top.ecalculator.thread_core.status*}" +
                (online ? "{*top.ecalculator.thread_core.status.online*}" : "{*top.ecalculator.thread_core.status.offline*}")
        );
        if (!online) {
            return;
        }

        final long parallelism = hatch.getParallelism();
        box = newBox(probeInfo).vertical();
        box.text("{*top.ecalculator.calculator_core.parallelism*}" + formatNumber(parallelism)
        );
    }

    private void processCellHatchInfo(IProbeInfo probeInfo, MetaTileEntityCalculatorCellHatch hatch) {
        final boolean online = hatch.getController() != null;
        IProbeInfo box = newBox(probeInfo);
        box.text("{*top.ecalculator.thread_core.status*}" +
                (online ? "{*top.ecalculator.thread_core.status.online*}" : "{*top.ecalculator.thread_core.status.offline*}")
        );
        if (!online) {
            return;
        }

        final long bytes = hatch.getSuppliedBytes();
        box = newBox(probeInfo).vertical();
        box.text("{*top.ecalculator.calculator_core.bytes*}" + formatNumber(bytes)
        );
    }

}
