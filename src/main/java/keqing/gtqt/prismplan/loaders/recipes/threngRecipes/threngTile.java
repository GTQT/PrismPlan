package keqing.gtqt.prismplan.loaders.recipes.threngRecipes;

import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.util.GTUtility;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.ASSEMBLY_LINE_RECIPES;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.common.blocks.MetaBlocks.OPTICAL_PIPES;
import static gregtech.common.items.MetaItems.FIELD_GENERATOR_EV;
import static gregtech.common.metatileentities.MetaTileEntities.HULL;
import static io.github.phantamanta44.threng.block.BlockBigAssembler.Type.*;
import static io.github.phantamanta44.threng.block.BlockMachine.Type.FAST_CRAFTER;
import static io.github.phantamanta44.threng.block.BlockMachine.Type.LEVEL_MAINTAINER;
import static io.github.phantamanta44.threng.item.ItemMaterial.Type.*;
import static keqing.gtqt.prismplan.common.item.PrismPlanMetaItems.FLUIX_CPU;
import static keqing.gtqt.prismplan.common.item.PrismPlanMetaItems.FLUIX_LOGIC_PROCESSOR;
import static keqing.gtqt.prismplan.loaders.recipes.naeRecipes.index.*;

public class threngTile {
    public static void init() {
        //指令缓存器
        ModHandler.removeRecipeByName("threng:level_maintainer");
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[IV])
                .input(circuit, MarkerMaterials.Tier.IV, 4)
                .inputs(MACHINE_CORE.newStack(1))
                .inputs(GTUtility.copy(2, meInterface))
                .inputs(GTUtility.copy(4, engineeringProcessor))
                .inputs((SPEC_PROCESSOR.newStack(1)))
                .fluidInputs(Polytetrafluoroethylene.getFluid(L * 4))
                .circuitMeta(1)
                .outputs(LEVEL_MAINTAINER.newStack(1))
                .duration(200)
                .EUt(VA[IV])
                .buildAndRegister();

        //优先装配单元
        ModHandler.removeRecipeByName("threng:pau");
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[IV])
                .input(circuit, MarkerMaterials.Tier.IV, 4)
                .inputs(MACHINE_CORE.newStack(1))
                .inputs(GTUtility.copy(2, meInterface))
                .inputs(GTUtility.copy(4, parallelProcessingUnit))
                .inputs((SPEC_PROCESSOR.newStack(1)))
                .fluidInputs(Polytetrafluoroethylene.getFluid(L * 4))
                .circuitMeta(1)
                .outputs(FAST_CRAFTER.newStack(1))
                .duration(200)
                .EUt(VA[IV])
                .buildAndRegister();

        //大分子框架
        ModHandler.removeRecipeByName("threng:ma_frame");
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(FLUIX_STEEL.newStack(4))
                .input(frameGt, TungstenSteel)
                .input(wireFine, Cobalt, 32)
                .input(wireFine, Copper, 32)
                .input(wireGtSingle, VanadiumGallium, 2)
                .fluidInputs(Polybenzimidazole.getFluid(L * 4))
                .outputs(FRAME.newStack(4))
                .duration(200)
                .EUt(VA[EV])
                .buildAndRegister();
        //外壁
        ModHandler.removeRecipeByName("threng:ma_vent");
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(FRAME.newStack(4))
                .input(plate, Titanium)
                .fluidInputs(Lead.getFluid(L * 4))
                .outputs(VENT.newStack(4))
                .duration(400)
                .EUt(VA[EV])
                .buildAndRegister();

        //样板核心
        ModHandler.removeRecipeByName("threng:ma_mod_pattern");
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(FRAME.newStack(4))
                .input(circuit, MarkerMaterials.Tier.IV, 4)
                .inputs(GTUtility.copy(1, meInterface))
                .inputs(GTUtility.copy(1, cell1k))
                .inputs(GTUtility.copy(4, meGlassCable))
                .inputs((SPEC_PROCESSOR.newStack(1)))
                .fluidInputs(Polytetrafluoroethylene.getFluid(L * 4))
                .circuitMeta(1)
                .outputs(MODULE_PATTERN.newStack(1))
                .duration(200)
                .EUt(VA[IV])
                .buildAndRegister();

        //装配处理核心
        ModHandler.removeRecipeByName("threng:ma_mod_cpu");
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(FRAME.newStack(4))
                .input(circuit, MarkerMaterials.Tier.IV, 4)
                .inputs(GTUtility.copy(1, craftingUnit))
                .inputs(GTUtility.copy(1, molecularAssembler))
                .inputs(GTUtility.copy(4, meGlassCable))
                .inputs((PARALLEL_PROCESSOR.newStack(1)))
                .fluidInputs(Polytetrafluoroethylene.getFluid(L * 4))
                .circuitMeta(2)
                .outputs(MODULE_CPU.newStack(1))
                .duration(200)
                .EUt(VA[IV])
                .buildAndRegister();

        //IO口
        ModHandler.removeRecipeByName("threng:ma_io_port");
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(FRAME.newStack(4))
                .input(circuit, MarkerMaterials.Tier.IV, 4)
                .inputs(GTUtility.copy(1, engineeringProcessor))
                .inputs(GTUtility.copy(1, cell1k))
                .inputs(GTUtility.copy(4, meGlassCable))
                .inputs((PARALLEL_PROCESSOR.newStack(1)))
                .fluidInputs(Polytetrafluoroethylene.getFluid(L * 4))
                .circuitMeta(3)
                .outputs(IO_PORT.newStack(1))
                .duration(200)
                .EUt(VA[IV])
                .buildAndRegister();

        //控制器
        ModHandler.removeRecipeByName("threng:ma_controller");
        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputs(FRAME.newStack(1))
                .input(frameGt, HSSE, 4)
                .input(circuit, MarkerMaterials.Tier.LuV, 4)
                .input(FIELD_GENERATOR_EV, 16)
                .input(FLUIX_LOGIC_PROCESSOR, 8)
                .input(rotor, RhodiumPlatedPalladium, 4)
                .input(gear, Ruridit, 4)
                .input(OPTICAL_PIPES[0], 16)
                .input(wireGtDouble, IndiumTinBariumTitaniumCuprate, 64)
                .fluidInputs(SolderingAlloy.getFluid(L * 8))
                .fluidInputs(VanadiumGallium.getFluid(L * 8))
                .fluidInputs(Polybenzimidazole.getFluid(L * 16))
                .fluidInputs(NaquadahAlloy.getFluid(1440))
                .outputs(CONTROLLER.newStack(1))
                .scannerResearch(b -> b
                        .researchStack(FLUIX_CPU.getStackForm())
                        .duration(2400)
                        .EUt(VA[IV]))
                .duration(1200).EUt(VA[IV]).buildAndRegister();

        ModHandler.removeRecipeByName("threng:aggregator");
        ModHandler.removeRecipeByName("threng:centrifuge");
        ModHandler.removeRecipeByName("threng:etcher");
        ModHandler.removeRecipeByName("threng:energizer");
    }
}
