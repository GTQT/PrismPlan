package keqing.gtqt.prismplan.loaders.recipes;

import co.neeve.nae2.NAE2;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.Mods;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.ASSEMBLER_RECIPES;
import static gregtech.api.recipes.RecipeMaps.ASSEMBLY_LINE_RECIPES;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.common.items.MetaItems.*;
import static gregtech.common.metatileentities.MetaTileEntities.QUANTUM_CHEST;
import static gregtech.common.metatileentities.MetaTileEntities.QUANTUM_TANK;
import static keqing.gtqt.prismplan.api.utils.AE2ItemReferences.QUANTUM_FLUID_STORAGE_CELL;
import static keqing.gtqt.prismplan.api.utils.AE2ItemReferences.QUANTUM_STORAGE_CELL;
import static keqing.gtqt.prismplan.api.utils.PrimsPlanUtility.SECOND;
import static keqing.gtqt.prismplan.common.item.AE2Items.storageCellQuantum;
import static keqing.gtqt.prismplan.common.item.ItemRegistry.*;

public class CellRecipes {
    public static void init() {
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(circuit, MarkerMaterials.Tier.HV,4)
                .inputs(Mods.AppliedEnergistics2.getItem("chest"))
                .inputs(Mods.AppliedEnergistics2.getItem("material", 36, 1))
                .input(BATTERY_HV_SODIUM)
                .fluidInputs(Polyethylene.getFluid(1440))
                .output(LARGE_PORTABLE_CELL_2048)
                .EUt(VA[HV])
                .duration(15 * SECOND)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(circuit, MarkerMaterials.Tier.EV,4)
                .inputs(Mods.AppliedEnergistics2.getItem("chest"))
                .inputs(Mods.AppliedEnergistics2.getItem("material", 37, 1))
                .input(BATTERY_EV_VANADIUM)
                .fluidInputs(Epoxy.getFluid(1440))
                .output(LARGE_PORTABLE_CELL_8192)
                .EUt(VA[EV])
                .duration(15 * SECOND)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(circuit, MarkerMaterials.Tier.IV,4)
                .inputs(Mods.AppliedEnergistics2.getItem("chest"))
                .inputs(Mods.AppliedEnergistics2.getItem("material", 38, 1))
                .input(BATTERY_IV_VANADIUM)
                .fluidInputs(Polytetrafluoroethylene.getFluid(1440))
                .output(LARGE_PORTABLE_CELL_32768)
                .EUt(VA[IV])
                .duration(15 * SECOND)
                .buildAndRegister();

        /////////////////////////////////////////////////////////////////////////////////////////
        // Ultimate Battery
        ASSEMBLY_LINE_RECIPES.recipeBuilder().EUt(300000).duration(2000)
                .input(plateDouble, Darmstadtium, 16)
                .input(circuit, MarkerMaterials.Tier.UHV, 4)
                .input(QUANTUM_CHEST[9], 64)
                .inputs(NAE2.definitions().materials().cellPart16384K().maybeStack(64).orElse(null))
                .input(FIELD_GENERATOR_UV, 4)
                .input(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT_WAFER, 64)
                .input(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT_WAFER, 64)
                .input(ADVANCED_SMD_DIODE, 64)
                .input(ADVANCED_SMD_CAPACITOR, 64)
                .input(ADVANCED_SMD_RESISTOR, 64)
                .input(ADVANCED_SMD_TRANSISTOR, 64)
                .input(ADVANCED_SMD_INDUCTOR, 64)
                .input(bolt, Neutronium, 64)
                .fluidInputs(SolderingAlloy.getFluid(L * 40))
                .fluidInputs(Polybenzimidazole.getFluid(2304))
                .fluidInputs(Naquadria.getFluid(L * 18))
                .outputs(QUANTUM_STORAGE_CELL)
                .stationResearch(b -> b
                        .researchStack(NAE2.definitions().materials().cellPart16384K().maybeStack(1).orElse(null))
                        .CWUt(144)
                        .EUt(VA[UHV]))
                .buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder().EUt(300000).duration(2000)
                .input(plateDouble, Darmstadtium, 16)
                .input(circuit, MarkerMaterials.Tier.UHV, 4)
                .input(QUANTUM_TANK[9], 64)
                .inputs(NAE2.definitions().materials().cellFluidPart16384K().maybeStack(64).orElse(null))
                .input(FIELD_GENERATOR_UV, 4)
                .input(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT_WAFER, 64)
                .input(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT_WAFER, 64)
                .input(ADVANCED_SMD_DIODE, 64)
                .input(ADVANCED_SMD_CAPACITOR, 64)
                .input(ADVANCED_SMD_RESISTOR, 64)
                .input(ADVANCED_SMD_TRANSISTOR, 64)
                .input(ADVANCED_SMD_INDUCTOR, 64)
                .input(bolt, Neutronium, 64)
                .fluidInputs(SolderingAlloy.getFluid(L * 40))
                .fluidInputs(Polybenzimidazole.getFluid(2304))
                .fluidInputs(Naquadria.getFluid(L * 18))
                .outputs(QUANTUM_FLUID_STORAGE_CELL)
                .stationResearch(b -> b
                        .researchStack(NAE2.definitions().materials().cellFluidPart16384K().maybeStack(1).orElse(null))
                        .CWUt(144)
                        .EUt(VA[UHV]))
                .buildAndRegister();
    }

}
