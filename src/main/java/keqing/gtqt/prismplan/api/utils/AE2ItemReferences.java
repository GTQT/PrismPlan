package keqing.gtqt.prismplan.api.utils;

import keqing.gtqt.prismplan.common.block.AE2Blocks;
import keqing.gtqt.prismplan.common.item.AE2Items;
import net.minecraft.item.ItemStack;

import static keqing.gtqt.prismplan.common.block.AE2Blocks.*;
import static keqing.gtqt.prismplan.common.item.AE2Items.*;

public class AE2ItemReferences {

    public static final ItemStack QUANTUM_STORAGE_CELL = storageCellQuantum
            .maybeStack(1).orElse(null);
    public static final ItemStack QUANTUM_FLUID_STORAGE_CELL = storageCellFluidQuantum
            .maybeStack(1).orElse(null);
    public static final ItemStack DIGITAL_SINGULARITY_STORAGE_CELL = storageCellSingularity
            .maybeStack(1).orElse(null);
    public static final ItemStack DIGITAL_SINGULARITY_FLUID_STORAGE_CELL = storageCellFluidSingularity
            .maybeStack(1).orElse(null);
    public static final ItemStack ARTIFICIAL_UNIVERSE_STORAGE_CELL = storageCellUniverse
            .maybeStack(1).orElse(null);
    public static final ItemStack ARTIFICIAL_UNIVERSE_FLUID_STORAGE_CELL = storageCellFluidUniverse
            .maybeStack(1).orElse(null);

    public static final ItemStack STORAGE_CRAFTING_65536x = storageCrafting65536x
            .maybeStack(1).orElse(null);
    public static final ItemStack STORAGE_CRAFTING_262144x = storageCrafting262144x
            .maybeStack(1).orElse(null);
    public static final ItemStack STORAGE_CRAFTING_1048576x = storageCrafting1048576x
            .maybeStack(1).orElse(null);
    public static final ItemStack COPROCESSOR_256x = coprocessor256x
            .maybeStack(1).orElse(null);
    public static final ItemStack COPROCESSOR_1024x = coprocessor1024x
            .maybeStack(1).orElse(null);
    public static final ItemStack COPROCESSOR_4096x = coprocessor4096x
            .maybeStack(1).orElse(null);

}
