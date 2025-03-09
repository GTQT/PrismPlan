package keqing.gtqt.prismplan.loaders.recipes;

import co.neeve.nae2.NAE2;
import gregtech.api.GTValues;
import gregtech.api.unification.material.MarkerMaterial;
import gregtech.api.unification.material.MarkerMaterials;
import keqing.gtqt.prismplan.common.block.PrismPlanBlocks;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.common.blocks.MetaBlocks.OPTICAL_PIPES;
import static gregtech.common.items.MetaItems.*;
import static gregtech.common.metatileentities.MetaTileEntities.HULL;
import static keqing.gtqt.prismplan.api.utils.MaterialHelper.*;
import static keqing.gtqt.prismplan.common.block.prismPlan.BlockMultiblockCasing.CasingType.*;
import static keqing.gtqt.prismplan.common.item.PrismPlanMetaItems.FLUIX_CPU;
import static keqing.gtqt.prismplan.common.item.PrismPlanMetaItems.FLUIX_LOGIC_PROCESSOR;
import static keqing.gtqt.prismplan.common.metatileentities.multi.PrismPlanMetaTileEntities.*;

public class MachineRecipes {
    public static void init() {
        ////////////////////////////////////////////////////////////////////////////////////
        //批量注册系统
        for (int i = 0; i < 5; i++) {
            CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder()
                    .input(circuitList[i], 4)
                    .inputs(NAE2.definitions().materials().cellPart16384K().maybeStack((int) Math.pow(2,i+2)).orElse(null))
                    .input(circuit, MarkerMaterial.create(GTValues.VN[i + 5].toLowerCase()), 8)
                    .input(screw, Plate[i], 4)
                    .input(wireFine, Pipe[i], 32)
                    .output(itemCellList[i])
                    .duration(200)
                    .EUt(VA[IV + i]).buildAndRegister();

            CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder()
                    .input(circuitList[i], 4)
                    .inputs(NAE2.definitions().materials().cellFluidPart16384K().maybeStack((int) Math.pow(2,i+2)).orElse(null))
                    .input(circuit, MarkerMaterial.create(GTValues.VN[i + 5].toLowerCase()), 8)
                    .input(screw, Plate[i], 4)
                    .input(wireFine, Pipe[i], 32)
                    .output(fluidCellList[i])
                    .duration(200)
                    .EUt(VA[IV + i]).buildAndRegister();

            CIRCUIT_ASSEMBLER_RECIPES.recipeBuilder()
                    .input(circuitList[i], 16)
                    .inputs(NAE2.definitions().materials().cellPart16384K().maybeStack((int) Math.pow(2,i+2)).orElse(null))
                    .input(circuit, MarkerMaterial.create(GTValues.VN[i + 5].toLowerCase()), 16)
                    .input(emitterList[i],2)
                    .input(wireFine, Pipe[i], 16)
                    .output(calculatorCellList[i])
                    .duration(200)
                    .EUt(VA[IV + i]).buildAndRegister();

            //EC 线程仓
            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(HULL[5 + i])
                    .input(circuitList[i], 4)
                    .input(emitterList[i])
                    .input(plate, Plate[i], 2)
                    .output(THREAD_HATCH[i])
                    .duration(200)
                    .fluidInputs(Polybenzimidazole.getFluid(L * 4))
                    .EUt(VA[IV + i]).buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(HULL[5 + i])
                    .input(circuitList[i], 8)
                    .input(emitterList[i])
                    .input(fieldGeneratorList[i])
                    .input(plate, Plate[i], 2)
                    .output(THREAD_HATCH[i + 5])
                    .duration(200)
                    .fluidInputs(Polybenzimidazole.getFluid(L * 4))
                    .EUt(VA[IV + i]).buildAndRegister();

            //EC 并行仓
            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(HULL[5 + i])
                    .input(circuitList[i], 4)
                    .input(plate, Plate[i], 2)
                    .input(wireFine, Pipe[i], 8)
                    .output(PARALLEL_HATCH[i])
                    .duration(200)
                    .fluidInputs(Polybenzimidazole.getFluid(L * 4))
                    .EUt(VA[IV + i]).buildAndRegister();

            //EC 计算元件仓
            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(HULL[5 + i])
                    .input(circuitList[i], 2)
                    .input(fieldGeneratorList[i])
                    .input(plate, Plate[i], 2)
                    .output(CALCULATOR_CELL_HATCH[i])
                    .duration(200)
                    .fluidInputs(Polybenzimidazole.getFluid(L * 4))
                    .EUt(VA[IV + i]).buildAndRegister();

            //ES 电池仓
            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(HULL[5 + i])
                    .input(circuitList[i], 1)
                    .input(batteryList[i], 1)
                    .input(wireFine, Pipe[i], 32)
                    .output(STORAGE_ENERGY_CELL[i])
                    .duration(200)
                    .fluidInputs(Polybenzimidazole.getFluid(L * 4))
                    .EUt(VA[IV + i]).buildAndRegister();

            //ES 元件仓
            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(HULL[5 + i])
                    .input(circuitList[i], 2)
                    .input(roborArmList[i])
                    .input(plate, Plate[i], 4)
                    .output(STORAGE_CELL_HATCH[i])
                    .duration(200)
                    .fluidInputs(Polybenzimidazole.getFluid(L * 4))
                    .EUt(VA[IV + i]).buildAndRegister();

        }

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[5])
                .input(FLUIX_LOGIC_PROCESSOR, 2)
                .input(EMITTER_IV, 2)
                .input(plate, Iridium, 8)
                .input(gear, Ruridit, 1)
                .input(circuit, MarkerMaterials.Tier.IV, 2)
                .fluidInputs(Polybenzimidazole.getFluid(L * 4))
                .output(NETWORK_STORE_HATCH)
                .EUt(VA[EV])
                .duration(600)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(HULL[5])
                .input(FLUIX_LOGIC_PROCESSOR, 2)
                .input(FIELD_GENERATOR_IV, 2)
                .input(plate, Iridium, 8)
                .input(screw, RhodiumPlatedPalladium, 4)
                .input(circuit, MarkerMaterials.Tier.IV, 2)
                .fluidInputs(Polybenzimidazole.getFluid(L * 4))
                .output(NETWORK_CALCULATOR_HATCH)
                .EUt(VA[EV])
                .duration(600)
                .buildAndRegister();

        ////////////////////////////////////////////////////////////////////////////////////
        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputs(PrismPlanBlocks.blockMultiblockCasing.getItemVariant(MULTI_CASING))
                .input(frameGt, HSSG, 4)
                .input(circuit, MarkerMaterials.Tier.ZPM, 4)
                .input(FIELD_GENERATOR_LuV, 16)
                .input(FLUIX_LOGIC_PROCESSOR, 8)
                .input(rotor, RhodiumPlatedPalladium, 4)
                .input(gear, Ruridit, 4)
                .input(OPTICAL_PIPES[0], 16)
                .input(wireGtDouble, IndiumTinBariumTitaniumCuprate, 64)
                .fluidInputs(SolderingAlloy.getFluid(L * 8))
                .fluidInputs(VanadiumGallium.getFluid(L * 8))
                .fluidInputs(Polybenzimidazole.getFluid(L * 16))
                .fluidInputs(NaquadahAlloy.getFluid(1440))
                .output(STORAGE_CELL_CONTROL)
                .scannerResearch(b -> b
                        .researchStack(FLUIX_CPU.getStackForm())
                        .duration(2400)
                        .EUt(VA[IV]))
                .duration(1200).EUt(VA[LuV]).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputs(PrismPlanBlocks.blockMultiblockCasing.getItemVariant(MULTI_CASING))
                .input(frameGt, HSSG, 4)
                .input(circuit, MarkerMaterials.Tier.UV, 4)
                .input(circuit, MarkerMaterials.Tier.ZPM, 16)
                .input(FLUIX_LOGIC_PROCESSOR, 8)
                .input(rotor, RhodiumPlatedPalladium, 4)
                .input(gear, Ruridit, 4)
                .input(OPTICAL_PIPES[0], 16)
                .input(wireGtDouble, IndiumTinBariumTitaniumCuprate, 64)
                .fluidInputs(SolderingAlloy.getFluid(L * 8))
                .fluidInputs(VanadiumGallium.getFluid(L * 8))
                .fluidInputs(Polybenzimidazole.getFluid(L * 16))
                .fluidInputs(NaquadahAlloy.getFluid(1440))
                .output(CALCULATOR_CONTROL)
                .scannerResearch(b -> b
                        .researchStack(FLUIX_CPU.getStackForm())
                        .duration(2400)
                        .EUt(VA[IV]))
                .duration(1200).EUt(VA[LuV]).buildAndRegister();

        ////////////////////////////////////////////////////////////////////////////////////

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(frameGt, Iridium)
                .input(plate, Platinum, 6)
                .input(circuit, MarkerMaterials.Tier.LuV)
                .input(wireFine, Cobalt, 32)
                .input(wireFine, Copper, 32)
                .input(wireGtSingle, VanadiumGallium, 2)
                .fluidInputs(Polybenzimidazole.getFluid(L * 4))
                .outputs(PrismPlanBlocks.blockMultiblockCasing.getItemVariant(MULTI_CASING))
                .duration(200).EUt(VA[LuV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(frameGt, Titanium)
                .input(ELECTRIC_MOTOR_IV, 2)
                .input(rotor, Titanium, 2)
                .input(pipeTinyFluid, Titanium, 16)
                .input(plate, Gold, 16)
                .input(wireGtSingle, SamariumIronArsenicOxide)
                .fluidInputs(Polybenzimidazole.getFluid(L * 4))
                .outputs(PrismPlanBlocks.blockMultiblockCasing.getItemVariant(MULTI_HEAT_VENT))
                .duration(100).EUt(VA[EV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(PrismPlanBlocks.blockMultiblockCasing.getItemVariant(MULTI_CASING))
                .input(circuit, MarkerMaterials.Tier.ZPM)
                .input(wireFine, NiobiumTitanium, 64)
                .input(wireFine, Platinum, 64)
                .input(wireGtSingle, IndiumTinBariumTitaniumCuprate, 4)
                .fluidInputs(Polybenzimidazole.getFluid(L * 4))
                .outputs(PrismPlanBlocks.blockMultiblockCasing.getItemVariant(MULTI_CONNECT))
                .duration(200).EUt(VA[LuV]).buildAndRegister();
    }
}
