package keqing.gtqt.prismplan.loaders.recipes;

import co.neeve.nae2.NAE2;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.Mods;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.ASSEMBLER_RECIPES;
import static keqing.gtqt.prismplan.api.utils.AE2ItemReferences.*;
import static keqing.gtqt.prismplan.api.utils.PrimsPlanUtility.SECOND;
import static keqing.gtqt.prismplan.common.block.AE2Blocks.*;
import static keqing.gtqt.prismplan.common.item.PrismPlanMetaItems.PARALLEL_CIRCUIT_BOARD;
import static keqing.gtqt.prismplan.common.item.PrismPlanMetaItems.SPECULATIVE_CIRCUIT_BOARD;

public class StorageCraftingRecipes {
    public static void init() {
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.circuit, MarkerMaterials.Tier.EV)
                .input(PARALLEL_CIRCUIT_BOARD)
                .inputs(Mods.AppliedEnergistics2.getItem("crafting_unit"))
                .inputs(NAE2.definitions().materials().cellPart256K().maybeStack(1).orElse(null))
                .inputs(NAE2.definitions().blocks().coprocessor64x().maybeStack(3).orElse(null))
                .outputs(coprocessor256x.maybeStack(1).orElse(null))
                .EUt(VA[EV])
                .duration(15 * SECOND)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.circuit, MarkerMaterials.Tier.IV)
                .input(PARALLEL_CIRCUIT_BOARD)
                .inputs(Mods.AppliedEnergistics2.getItem("crafting_unit"))
                .inputs(NAE2.definitions().materials().cellPart1024K().maybeStack(1).orElse(null))
                .inputs(coprocessor256x.maybeStack(3).orElse(null))
                .outputs(coprocessor1024x.maybeStack(1).orElse(null))
                .EUt(VA[IV])
                .duration(15 * SECOND)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.circuit, MarkerMaterials.Tier.LuV)
                .input(PARALLEL_CIRCUIT_BOARD)
                .inputs(Mods.AppliedEnergistics2.getItem("crafting_unit"))
                .inputs(NAE2.definitions().materials().cellPart4096K().maybeStack(1).orElse(null))
                .inputs(coprocessor1024x.maybeStack(3).orElse(null))
                .outputs(coprocessor4096x.maybeStack(1).orElse(null))
                .EUt(VA[LuV])
                .duration(15 * SECOND)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .circuitMeta(1)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.EV,4)
                .input(SPECULATIVE_CIRCUIT_BOARD)
                .inputs(Mods.AppliedEnergistics2.getItem("crafting_unit"))
                .inputs(NAE2.definitions().materials().cellFluidPart16384K().maybeStack(4).orElse(null))
                .outputs(storageCrafting65536x.maybeStack(1).orElse(null))
                .EUt(VA[EV])
                .duration(15 * SECOND)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .circuitMeta(2)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.IV,4)
                .input(SPECULATIVE_CIRCUIT_BOARD)
                .inputs(Mods.AppliedEnergistics2.getItem("crafting_unit"))
                .inputs(NAE2.definitions().materials().cellFluidPart16384K().maybeStack(16).orElse(null))
                .outputs(storageCrafting262144x.maybeStack(1).orElse(null))
                .EUt(VA[IV])
                .duration(15 * SECOND)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .circuitMeta(3)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.LuV,4)
                .input(SPECULATIVE_CIRCUIT_BOARD)
                .inputs(Mods.AppliedEnergistics2.getItem("crafting_unit"))
                .inputs(NAE2.definitions().materials().cellFluidPart16384K().maybeStack(64).orElse(null))
                .outputs(storageCrafting1048576x.maybeStack(1).orElse(null))
                .EUt(VA[LuV])
                .duration(15 * SECOND)
                .buildAndRegister();
    }
}
