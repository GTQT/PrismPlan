package keqing.gtqt.prismplan.loaders.recipes.naeRecipes;

import co.neeve.nae2.NAE2;
import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.util.GTUtility;
import gregtech.common.items.MetaItems;
import net.minecraft.item.ItemStack;

import static gregtech.api.GTValues.*;
import static gregtech.api.GTValues.EV;
import static gregtech.api.unification.ore.OrePrefix.circuit;
import static keqing.gtqt.prismplan.loaders.recipes.naeRecipes.index.*;

public class NaeCellRecipes {
    public static void init(){
        deleteRecipes();

        cellRecipes();

        cellRecipes(NAE2.definitions().materials().cellPart256K().maybeStack(1).orElse(null), NAE2.definitions().items().storageCell256K().maybeStack(1).orElse(null));
        cellRecipes(NAE2.definitions().materials().cellPart1024K().maybeStack(1).orElse(null), NAE2.definitions().items().storageCell1024K().maybeStack(1).orElse(null));
        cellRecipes(NAE2.definitions().materials().cellPart4096K().maybeStack(1).orElse(null), NAE2.definitions().items().storageCell4096K().maybeStack(1).orElse(null));
        cellRecipes(NAE2.definitions().materials().cellPart16384K().maybeStack(1).orElse(null), NAE2.definitions().items().storageCell16384K().maybeStack(1).orElse(null));
        cellRecipes(NAE2.definitions().materials().cellFluidPart256K().maybeStack(1).orElse(null), NAE2.definitions().items().storageCellFluid256K().maybeStack(1).orElse(null));
        cellRecipes(NAE2.definitions().materials().cellFluidPart1024K().maybeStack(1).orElse(null), NAE2.definitions().items().storageCellFluid1024K().maybeStack(1).orElse(null));
        cellRecipes(NAE2.definitions().materials().cellFluidPart4096K().maybeStack(1).orElse(null), NAE2.definitions().items().storageCellFluid4096K().maybeStack(1).orElse(null));
        cellRecipes(NAE2.definitions().materials().cellFluidPart16384K().maybeStack(1).orElse(null), NAE2.definitions().items().storageCellFluid16384K().maybeStack(1).orElse(null));
    }
    private static void cellRecipes() {
        //256k
        RecipeMaps.CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(GTUtility.copy(4, cell64k))
                .input(circuit, MarkerMaterials.Tier.HV, 16)
                .inputs(engineeringProcessor)
                .input(MetaItems.EXTREME_CIRCUIT_BOARD)
                .circuitMeta(1)
                .outputs(NAE2.definitions().materials().cellPart256K().maybeStack(1).orElse(null))
                .EUt(VA[EV])
                .duration(200)
                .buildAndRegister();

        RecipeMaps.CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder()
                .input(circuit, MarkerMaterials.Tier.EV, 4)
                .input(circuit, MarkerMaterials.Tier.HV, 16)
                .inputs(engineeringProcessor)
                .input(MetaItems.EXTREME_CIRCUIT_BOARD)
                .circuitMeta(1)
                .outputs(NAE2.definitions().materials().cellPart256K().maybeStack(1).orElse(null))
                .EUt(VA[EV])
                .duration(200)
                .buildAndRegister();

        //1024K
        RecipeMaps.CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(NAE2.definitions().materials().cellPart256K().maybeStack(4).orElse(null))
                .input(circuit, MarkerMaterials.Tier.EV, 16)
                .inputs(engineeringProcessor)
                .input(MetaItems.ELITE_CIRCUIT_BOARD)
                .circuitMeta(1)
                .outputs(NAE2.definitions().materials().cellPart1024K().maybeStack(1).orElse(null))
                .EUt(VA[IV])
                .duration(200)
                .buildAndRegister();

        RecipeMaps.CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder()
                .input(circuit, MarkerMaterials.Tier.IV, 4)
                .input(circuit, MarkerMaterials.Tier.EV, 16)
                .inputs(engineeringProcessor)
                .input(MetaItems.ELITE_CIRCUIT_BOARD)
                .circuitMeta(1)
                .outputs(NAE2.definitions().materials().cellPart1024K().maybeStack(1).orElse(null))
                .EUt(VA[IV])
                .duration(200)
                .buildAndRegister();
        //4096K
        RecipeMaps.CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(NAE2.definitions().materials().cellPart1024K().maybeStack(4).orElse(null))
                .input(circuit, MarkerMaterials.Tier.IV, 16)
                .inputs(engineeringProcessor)
                .input(MetaItems.WETWARE_CIRCUIT_BOARD)
                .circuitMeta(1)
                .outputs(NAE2.definitions().materials().cellPart4096K().maybeStack(1).orElse(null))
                .EUt(VA[IV])
                .duration(200)
                .buildAndRegister();

        RecipeMaps.CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder()
                .input(circuit, MarkerMaterials.Tier.LuV, 4)
                .input(circuit, MarkerMaterials.Tier.IV, 16)
                .inputs(engineeringProcessor)
                .input(MetaItems.WETWARE_CIRCUIT_BOARD)
                .circuitMeta(1)
                .outputs(NAE2.definitions().materials().cellPart4096K().maybeStack(1).orElse(null))
                .EUt(VA[IV])
                .duration(200)
                .buildAndRegister();

        //16384k
        RecipeMaps.CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(NAE2.definitions().materials().cellPart4096K().maybeStack(4).orElse(null))
                .input(circuit, MarkerMaterials.Tier.ZPM, 16)
                .inputs(engineeringProcessor)
                .input(MetaItems.WETWARE_CIRCUIT_BOARD,16)
                .circuitMeta(1)
                .outputs(NAE2.definitions().materials().cellPart16384K().maybeStack(1).orElse(null))
                .EUt(VA[LuV])
                .duration(200)
                .buildAndRegister();

        RecipeMaps.CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder()
                .input(circuit, MarkerMaterials.Tier.ZPM, 4)
                .input(circuit, MarkerMaterials.Tier.LuV, 16)
                .inputs(engineeringProcessor)
                .input(MetaItems.WETWARE_CIRCUIT_BOARD,16)
                .circuitMeta(1)
                .outputs(NAE2.definitions().materials().cellPart16384K().maybeStack(1).orElse( null))
                .EUt(VA[LuV])
                .duration(200)
                .buildAndRegister();

        //256k 流体
        RecipeMaps.CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(GTUtility.copy(4, fluidCell64k))
                .input(circuit, MarkerMaterials.Tier.HV, 16)
                .inputs(engineeringProcessor)
                .input(MetaItems.EXTREME_CIRCUIT_BOARD)
                .circuitMeta(2)
                .outputs(NAE2.definitions().materials().cellFluidPart256K().maybeStack(1).orElse( null))
                .EUt(VA[EV])
                .duration(200)
                .buildAndRegister();

        RecipeMaps.CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder()
                .input(circuit, MarkerMaterials.Tier.HV, 4)
                .input(circuit, MarkerMaterials.Tier.MV, 16)
                .inputs(engineeringProcessor)
                .input(MetaItems.EXTREME_CIRCUIT_BOARD)
                .circuitMeta(2)
                .outputs(NAE2.definitions().materials().cellFluidPart256K().maybeStack(1).orElse( null))
                .EUt(VA[EV])
                .duration(200)
                .buildAndRegister();

        //1024k 流体
        RecipeMaps.CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(NAE2.definitions().materials().cellFluidPart256K().maybeStack(4).orElse(null))
                .input(circuit, MarkerMaterials.Tier.EV, 16)
                .inputs(engineeringProcessor)
                .input(MetaItems.ELITE_CIRCUIT_BOARD)
                .circuitMeta(2)
                .outputs(NAE2.definitions().materials().cellFluidPart256K().maybeStack(1).orElse(null))
                .EUt(VA[IV])
                .duration(200)
                .buildAndRegister();

        RecipeMaps.CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder()
                .input(circuit, MarkerMaterials.Tier.IV, 4)
                .input(circuit, MarkerMaterials.Tier.EV, 16)
                .inputs(engineeringProcessor)
                .input(MetaItems.ELITE_CIRCUIT_BOARD)
                .circuitMeta(2)
                .outputs(NAE2.definitions().materials().cellFluidPart1024K().maybeStack(1).orElse(null))
                .EUt(VA[IV])
                .duration(200)
                .buildAndRegister();

        //4096K
        RecipeMaps.CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(NAE2.definitions().materials().cellFluidPart1024K().maybeStack(4).orElse(null))
                .input(circuit, MarkerMaterials.Tier.IV, 16)
                .inputs(engineeringProcessor)
                .input(MetaItems.WETWARE_CIRCUIT_BOARD)
                .circuitMeta(2)
                .outputs(NAE2.definitions().materials().cellFluidPart4096K().maybeStack(1).orElse(null))
                .EUt(VA[IV])
                .duration(200)
                .buildAndRegister();

        RecipeMaps.CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder()
                .input(circuit, MarkerMaterials.Tier.LuV, 4)
                .input(circuit, MarkerMaterials.Tier.IV, 16)
                .inputs(engineeringProcessor)
                .input(MetaItems.WETWARE_CIRCUIT_BOARD)
                .circuitMeta(2)
                .outputs(NAE2.definitions().materials().cellFluidPart4096K().maybeStack(1).orElse(null))
                .EUt(VA[IV])
                .duration(200)
                .buildAndRegister();

        //16384k
        RecipeMaps.CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(NAE2.definitions().materials().cellFluidPart4096K().maybeStack(4).orElse(null))
                .input(circuit, MarkerMaterials.Tier.ZPM, 16)
                .inputs(engineeringProcessor)
                .input(MetaItems.WETWARE_CIRCUIT_BOARD,16)
                .circuitMeta(2)
                .outputs(NAE2.definitions().materials().cellFluidPart16384K().maybeStack(1).orElse(null))
                .EUt(VA[LuV])
                .duration(200)
                .buildAndRegister();

        RecipeMaps.CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder()
                .input(circuit, MarkerMaterials.Tier.ZPM, 4)
                .input(circuit, MarkerMaterials.Tier.LuV, 16)
                .inputs(engineeringProcessor)
                .input(MetaItems.WETWARE_CIRCUIT_BOARD,16)
                .circuitMeta(2)
                .outputs(NAE2.definitions().materials().cellFluidPart16384K().maybeStack(1).orElse( null))
                .EUt(VA[LuV])
                .duration(200)
                .buildAndRegister();
    }
    private static void cellRecipes(ItemStack cell, ItemStack  plate) {
        //cell+外壳=盘符
        RecipeMaps.PACKER_RECIPES.recipeBuilder()
                .inputs(cell)
                .inputs(meStorageCell)
                .outputs(plate)
                .EUt(VA[LV])
                .duration(20)
                .buildAndRegister();

        RecipeMaps.PACKER_RECIPES.recipeBuilder()
                .inputs(plate)
                .outputs(cell)
                .outputs(meStorageCell)
                .EUt(VA[LV])
                .duration(20)
                .buildAndRegister();
    }
    private static void deleteRecipes() {
        ModHandler.removeRecipeByName("nae2:item/cell/storage_cell_256k");
        ModHandler.removeRecipeByName("nae2:item/cell/storage_cell_1024k");
        ModHandler.removeRecipeByName("nae2:item/cell/storage_cell_4096k");
        ModHandler.removeRecipeByName("nae2:item/cell/storage_cell_16384k");

        ModHandler.removeRecipeByName("nae2:item/cell/storage_cell_fluid_256k");
        ModHandler.removeRecipeByName("nae2:item/cell/storage_cell_fluid_1024k");
        ModHandler.removeRecipeByName("nae2:item/cell/storage_cell_fluid_4096k");
        ModHandler.removeRecipeByName("nae2:item/cell/storage_cell_fluid_16384k");

        ModHandler.removeRecipeByName("nae2:item/material/storage/cell_part_256k");
        ModHandler.removeRecipeByName("nae2:item/material/storage/cell_part_1024k");
        ModHandler.removeRecipeByName("nae2:item/material/storage/cell_part_4096k");
        ModHandler.removeRecipeByName("nae2:item/material/storage/cell_part_16384k");

        ModHandler.removeRecipeByName("nae2:item/material/storage/cell_part_fluid_256k");
        ModHandler.removeRecipeByName("nae2:item/material/storage/cell_part_fluid_1024k");
        ModHandler.removeRecipeByName("nae2:item/material/storage/cell_part_fluid_4096k");
        ModHandler.removeRecipeByName("nae2:item/material/storage/cell_part_fluid_16384k");
    }
}
