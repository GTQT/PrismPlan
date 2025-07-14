package keqing.gtqt.prismplan.loaders.recipes.threngRecipes;

import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTUtility;
import gregtech.common.items.MetaItems;
import keqing.gtqt.prismplan.common.item.PrismPlanMetaItems;

import static gregtech.api.GTValues.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;
import static io.github.phantamanta44.threng.item.ItemMaterial.Type.*;
import static keqing.gtqt.prismplan.common.item.PrismPlanMetaItems.PARALLEL_CIRCUIT_BOARD;
import static keqing.gtqt.prismplan.common.item.PrismPlanMetaItems.SPECULATIVE_CIRCUIT_BOARD;

public class threngMisc {
    public static void init()
    {
        //推测处理器
        RecipeMaps.CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder()
                .input(plate, Materials.Silicon)
                .input(SPECULATIVE_CIRCUIT_BOARD)
                .input(MetaItems.SMD_TRANSISTOR, 1)
                .input(MetaItems.SMD_RESISTOR, 1)
                .input(MetaItems.SMD_DIODE, 1)
                .input(MetaItems.SMD_INDUCTOR, 1)
                .outputs(SPEC_PROCESSOR.newStack(1))
                .EUt(VA[HV])
                .duration(200)
                .buildAndRegister();

        RecipeMaps.CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder()
                .input(plate, Materials.Silicon, 4)
                .input(SPECULATIVE_CIRCUIT_BOARD,4)
                .input(MetaItems.ADVANCED_SMD_TRANSISTOR, 1)
                .input(MetaItems.ADVANCED_SMD_RESISTOR, 1)
                .input(MetaItems.ADVANCED_SMD_DIODE, 1)
                .input(MetaItems.ADVANCED_SMD_INDUCTOR, 1)
                .outputs(SPEC_PROCESSOR.newStack(4))
                .EUt(VA[LuV])
                .duration(200)
                .buildAndRegister();

        //并行处理器
        RecipeMaps.CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder()
                .input(plate, Materials.Silicon)
                .input(PARALLEL_CIRCUIT_BOARD)
                .input(MetaItems.SMD_TRANSISTOR, 1)
                .input(MetaItems.SMD_RESISTOR, 1)
                .input(MetaItems.SMD_DIODE, 1)
                .input(MetaItems.SMD_INDUCTOR, 1)
                .outputs(PARALLEL_PROCESSOR.newStack(1))
                .EUt(VA[HV])
                .duration(200)
                .buildAndRegister();

        RecipeMaps.CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder()
                .input(plate, Materials.Silicon, 4)
                .input(PARALLEL_CIRCUIT_BOARD,4)
                .input(MetaItems.ADVANCED_SMD_TRANSISTOR, 1)
                .input(MetaItems.ADVANCED_SMD_RESISTOR, 1)
                .input(MetaItems.ADVANCED_SMD_DIODE, 1)
                .input(MetaItems.ADVANCED_SMD_INDUCTOR, 1)
                .outputs(PARALLEL_PROCESSOR.newStack(4))
                .EUt(VA[LuV])
                .duration(200)
                .buildAndRegister();

        //共振水晶
        RecipeMaps.POLARIZER_RECIPES.recipeBuilder()
                .input(PrismPlanMetaItems.FLUIX_CPU)
                .outputs(SPACE_GEM.newStack(1))
                .EUt(VA[IV])
                .duration(200)
                .buildAndRegister();

        //逻辑单元
        ModHandler.removeRecipeByName("threng:machine_core");
        RecipeMaps.CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder()
                .input(MetaItems.ELITE_CIRCUIT_BOARD)
                .inputs(SPACE_GEM.newStack(1))
                .input(MetaItems.SMD_DIODE, 1)
                .input(MetaItems.SMD_INDUCTOR, 1)
                .input(wireFine, Samarium, 4)
                .input(screw, NiobiumTitanium, 4)
                .outputs(MACHINE_CORE.newStack(1))
                .EUt(VA[IV])
                .duration(200)
                .buildAndRegister();
    }
}
