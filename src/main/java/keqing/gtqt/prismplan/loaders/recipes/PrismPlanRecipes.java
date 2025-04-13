package keqing.gtqt.prismplan.loaders.recipes;

import static keqing.gtqt.prismplan.PrismPlanConfig.RecipesSwitch;

public class PrismPlanRecipes {
    private PrismPlanRecipes() {

    }

    public static void load() {
    }

    public static void init() {
        if (RecipesSwitch.CellRecipes) CellRecipes.init();
        if (RecipesSwitch.StorageCraftingRecipes) StorageCraftingRecipes.init();
        if (RecipesSwitch.CircuitChain) CircuitChain.init();
        if (RecipesSwitch.MiscRecipes) MiscRecipes.init();
        if (RecipesSwitch.MachineRecipes) MachineRecipes.init();
    }

}
