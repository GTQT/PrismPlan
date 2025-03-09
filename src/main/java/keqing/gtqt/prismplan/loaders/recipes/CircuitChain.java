package keqing.gtqt.prismplan.loaders.recipes;

import gregtech.api.metatileentity.multiblock.CleanroomType;
import gregtech.api.util.Mods;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.MarkerMaterials.Color.Magenta;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.common.items.MetaItems.*;
import static keqing.gtqt.prismplan.common.item.PrismPlanMetaItems.*;

public class CircuitChain {
    public static void init() {
        //////////////////////////////////////////////////////////////////////////////////
        AUTOCLAVE_RECIPES.recipeBuilder().EUt(VA[ZPM]).duration(600)
                .input(CRYSTAL_CENTRAL_PROCESSING_UNIT)
                .fluidInputs(Americium.getFluid(16))
                .output(FLUIX_SOC, 1)
                .cleanroom(CleanroomType.STERILE_CLEANROOM)
                .buildAndRegister();

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[EV]).duration(200)
                .inputs(Mods.AppliedEnergistics2.getItem("material", 16, 1))
                .input(plate, Cobalt)
                .input(wireFine, Platinum, 4)
                .input(screw, Titanium, 4)
                .output(PARALLEL_CIRCUIT_BOARD)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[EV]).duration(200)
                .inputs(Mods.AppliedEnergistics2.getItem("material", 16, 1))
                .input(plate, Nickel)
                .input(wireFine, Platinum, 4)
                .input(screw, Titanium, 4)
                .output(SPECULATIVE_CIRCUIT_BOARD)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();


        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(VA[EV]).duration(200)
                .inputs(Mods.AppliedEnergistics2.getItem("material", 16, 1))
                .input(plate, Amethyst)
                .input(wireFine, Platinum, 4)
                .input(screw, Titanium, 4)
                .output(DEDUCTION_CIRCUIT_BOARD)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();


        //////////////////////////////////////////////////////////////////////////////////
        LASER_ENGRAVER_RECIPES.recipeBuilder()
                .EUt(VA[LuV])
                .duration(200)
                .inputs(Mods.AppliedEnergistics2.getItem("material", 12, 1))
                .notConsumable(craftingLens, Magenta)
                .output(FLUIX_CPU)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        // Neuro Processing Unit
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(80000).duration(600)
                .input(DEDUCTION_CIRCUIT_BOARD)
                .inputs(Mods.AppliedEnergistics2.getItem("material", 12, 16))
                .input(wireFine, YttriumBariumCuprate, 8)
                .input(stick, Samarium, 8)
                .input(foil, StyreneButadieneRubber, 16)
                .input(bolt, HSSS, 8)
                .fluidInputs(NaquadahAlloy.getFluid(144))
                .output(DEDUCTION_PROCESSOR)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        // LuV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(38400).duration(200)
                .input(DEDUCTION_PROCESSOR)
                .input(FLUIX_CPU)
                .input(QUBIT_CENTRAL_PROCESSING_UNIT)
                .input(ADVANCED_SMD_CAPACITOR, 8)
                .input(ADVANCED_SMD_TRANSISTOR, 8)
                .input(wireFine, IndiumTinBariumTitaniumCuprate, 8)
                .output(FLUIX_LOGIC_PROCESSOR, 1)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        // SoC LuV
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(150000).duration(100)
                .input(DEDUCTION_PROCESSOR)
                .input(FLUIX_SOC)
                .input(wireFine, IndiumTinBariumTitaniumCuprate, 8)
                .input(bolt, Naquadria, 8)
                .output(FLUIX_LOGIC_PROCESSOR, 2)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        // ZPM
        CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder().EUt(38400).duration(400)
                .input(DEDUCTION_CIRCUIT_BOARD)
                .input(FLUIX_LOGIC_PROCESSOR, 2)
                .input(ADVANCED_SMD_INDUCTOR, 6)
                .input(ADVANCED_SMD_CAPACITOR, 12)
                .input(RANDOM_ACCESS_MEMORY, 24)
                .input(wireFine, IndiumTinBariumTitaniumCuprate, 16)
                .solderMultiplier(2)
                .output(FLUIX_LOGIC_ASSEMBLY)
                .cleanroom(CleanroomType.CLEANROOM)
                .buildAndRegister();

        // UV
        ASSEMBLY_LINE_RECIPES.recipeBuilder().EUt(38400).duration(400)
                .input(DEDUCTION_CIRCUIT_BOARD)
                .input(FLUIX_LOGIC_ASSEMBLY, 2)
                .input(ADVANCED_SMD_DIODE, 8)
                .input(NOR_MEMORY_CHIP, 16)
                .input(RANDOM_ACCESS_MEMORY, 32)
                .input(wireFine, IndiumTinBariumTitaniumCuprate, 24)
                .input(foil, Polybenzimidazole, 32)
                .input(plate, Europium, 4)
                .fluidInputs(SolderingAlloy.getFluid(1152))
                .output(FLUIX_LOGIC_COMPUTER)
                .stationResearch(b -> b
                        .researchStack(FLUIX_LOGIC_ASSEMBLY.getStackForm())
                        .CWUt(16))
                .buildAndRegister();

        // UHV
        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(frameGt, Trinium, 2)
                .input(FLUIX_LOGIC_COMPUTER, 2)
                .input(ADVANCED_SMD_DIODE, 32)
                .input(ADVANCED_SMD_CAPACITOR, 32)
                .input(ADVANCED_SMD_TRANSISTOR, 32)
                .input(ADVANCED_SMD_RESISTOR, 32)
                .input(ADVANCED_SMD_INDUCTOR, 32)
                .input(foil, Polybenzimidazole, 64)
                .input(RANDOM_ACCESS_MEMORY, 32)
                .input(wireGtDouble, EnrichedNaquadahTriniumEuropiumDuranide, 16)
                .input(plate, Indium, 8)
                .fluidInputs(SolderingAlloy.getFluid(L * 20))
                .fluidInputs(Polybenzimidazole.getFluid(L * 8))
                .output(FLUIX_LOGIC_MAINFRAME)
                .stationResearch(b -> b
                        .researchStack(FLUIX_LOGIC_COMPUTER.getStackForm())
                        .CWUt(96)
                        .EUt(VA[UV]))
                .EUt(300000).duration(2000).buildAndRegister();
    }

}
