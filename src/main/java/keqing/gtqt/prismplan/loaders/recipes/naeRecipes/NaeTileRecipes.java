package keqing.gtqt.prismplan.loaders.recipes.naeRecipes;

import co.neeve.nae2.NAE2;
import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.GTUtility;
import net.minecraft.item.ItemStack;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.ASSEMBLER_RECIPES;
import static gregtech.api.unification.material.Materials.Lead;
import static io.github.phantamanta44.threng.item.ItemMaterial.Type.PARALLEL_PROCESSOR;
import static keqing.gtqt.prismplan.api.utils.PrimsPlanUtility.SECOND;
import static keqing.gtqt.prismplan.loaders.recipes.naeRecipes.index.*;

public class NaeTileRecipes {
    public static void init() {
        //合成存储器 256k
        ModHandler.removeRecipeByName("nae2:block/crafting/storage_256k");
        cellRecipes(NAE2.definitions().materials().cellPart256K().maybeStack(1).orElse(null), NAE2.definitions().blocks().storageCrafting256K().maybeStack(1).orElse(null));
        ModHandler.removeRecipeByName("nae2:block/crafting/storage_1024k");
        cellRecipes(NAE2.definitions().materials().cellPart1024K().maybeStack(1).orElse(null), NAE2.definitions().blocks().storageCrafting1024K().maybeStack(1).orElse(null));
        ModHandler.removeRecipeByName("nae2:block/crafting/storage_4096k");
        cellRecipes(NAE2.definitions().materials().cellPart4096K().maybeStack(1).orElse(null), NAE2.definitions().blocks().storageCrafting4096K().maybeStack(1).orElse(null));
        ModHandler.removeRecipeByName("nae2:block/crafting/storage_16384k");
        cellRecipes(NAE2.definitions().materials().cellPart16384K().maybeStack(1).orElse(null), NAE2.definitions().blocks().storageCrafting16384K().maybeStack(1).orElse(null));

        ModHandler.removeRecipeByName("nae2:block/crafting/4x_coprocessor");
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.circuit, MarkerMaterials.Tier.HV)
                .inputs(PARALLEL_PROCESSOR.newStack(1))
                .inputs(GTUtility.copy(1, cell4k))
                .inputs(GTUtility.copy(3, parallelProcessingUnit))
                .fluidInputs(Lead.getFluid(576))
                .outputs(NAE2.definitions().blocks().coprocessor4x().maybeStack(1).orElse(null))
                .EUt(VA[HV])
                .duration(15 * SECOND)
                .buildAndRegister();
        ModHandler.removeRecipeByName("nae2:block/crafting/16x_coprocessor");
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.circuit, MarkerMaterials.Tier.HV)
                .inputs(PARALLEL_PROCESSOR.newStack(1))
                .inputs(GTUtility.copy(1, cell16k))
                .inputs(NAE2.definitions().blocks().coprocessor4x().maybeStack(3).orElse(null))
                .fluidInputs(Lead.getFluid(576))
                .outputs(NAE2.definitions().blocks().coprocessor16x().maybeStack(1).orElse(null))
                .EUt(VA[HV])
                .duration(15 * SECOND)
                .buildAndRegister();
        ModHandler.removeRecipeByName("nae2:block/crafting/64x_coprocessor");
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.circuit, MarkerMaterials.Tier.EV)
                .inputs(PARALLEL_PROCESSOR.newStack(1))
                .inputs(GTUtility.copy(1, cell64k))
                .inputs(NAE2.definitions().blocks().coprocessor16x().maybeStack(3).orElse(null))
                .fluidInputs(Lead.getFluid(576))
                .outputs(NAE2.definitions().blocks().coprocessor64x().maybeStack(1).orElse(null))
                .EUt(VA[EV])
                .duration(15 * SECOND)
                .buildAndRegister();
    }

    private static void cellRecipes(ItemStack cell, ItemStack craftingStorage) {
        //cell+外壳=盘符
        RecipeMaps.PACKER_RECIPES.recipeBuilder()
                .inputs(cell)
                .inputs(craftingUnit)
                .outputs(craftingStorage)
                .EUt(VA[HV])
                .duration(20)
                .buildAndRegister();

        RecipeMaps.PACKER_RECIPES.recipeBuilder()
                .inputs(craftingStorage)
                .outputs(cell)
                .outputs(craftingUnit)
                .EUt(VA[HV])
                .duration(20)
                .buildAndRegister();
    }
}
