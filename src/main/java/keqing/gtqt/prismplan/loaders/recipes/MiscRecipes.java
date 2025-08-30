package keqing.gtqt.prismplan.loaders.recipes;

import gregtech.api.util.Mods;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.dust;
import static gregtech.api.unification.ore.OrePrefix.plate;

public class MiscRecipes {
    public static void init() {
        //充能-》高纯
        AUTOCLAVE_RECIPES.recipeBuilder()
                .inputs(Mods.AppliedEnergistics2.getItem("material", 1, 1))
                .fluidInputs(DistilledWater.getFluid(1000))
                .outputs(Mods.AppliedEnergistics2.getItem("material", 10, 1))
                .EUt(VA[MV])
                .duration(200)
                .buildAndRegister();

        //普通-》高纯
        AUTOCLAVE_RECIPES.recipeBuilder()
                .input("crystalCertusQuartz")
                .fluidInputs(DistilledWater.getFluid(1000))
                .outputs(Mods.AppliedEnergistics2.getItem("material", 10, 1))
                .EUt(VA[HV])
                .duration(200)
                .buildAndRegister();

        AUTOCLAVE_RECIPES.recipeBuilder()
                .input("crystalFluix")
                .fluidInputs(DistilledWater.getFluid(1000))
                .outputs(Mods.AppliedEnergistics2.getItem("material", 12, 1))
                .EUt(VA[MV])
                .duration(200)
                .buildAndRegister();

        //充能
        POLARIZER_RECIPES.recipeBuilder()
                .input("crystalCertusQuartz")
                .outputs(Mods.AppliedEnergistics2.getItem("material", 1, 1))
                .EUt(VA[MV])
                .duration(200)
                .buildAndRegister();

        //粉碎
        MACERATOR_RECIPES.recipeBuilder()
                .input("crystalCertusQuartz")
                .output(dust, CertusQuartz)
                .EUt(VA[LV])
                .duration(200)
                .buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
                .input("crystalPureCertusQuartz")
                .output(dust, CertusQuartz)
                .EUt(VA[LV])
                .duration(200)
                .buildAndRegister();

        //Fluix
        MIXER_RECIPES.recipeBuilder()
                .input("dustCertusQuartz", 2)
                .input(dust, Electrum, 1)
                .input(dust, Redstone, 1)
                .fluidInputs(DistilledWater.getFluid(1000))
                .circuitMeta(5)
                .outputs(Mods.AppliedEnergistics2.getItem("material", 7, 4))
                .EUt(VA[MV])
                .duration(400)
                .buildAndRegister();
    }
}
