package meowmel.prismplan.loaders.recipes;

import meowmel.prismplan.loaders.recipes.naeRecipes.NaeCellRecipes;
import meowmel.prismplan.loaders.recipes.naeRecipes.NaeTileRecipes;
import meowmel.prismplan.loaders.recipes.threngRecipes.threngMisc;
import meowmel.prismplan.loaders.recipes.threngRecipes.threngTile;

import static meowmel.prismplan.PrismPlanConfig.RecipesSwitch;

public class PrismPlanRecipes {
    private PrismPlanRecipes() {

    }

    public static void load() {
    }

    public static void init() {
        if (RecipesSwitch.CellRecipes) {
            CellRecipes.init();
            NaeCellRecipes.init();
        }
        if (RecipesSwitch.StorageCraftingRecipes) {
            StorageCraftingRecipes.init();
            NaeTileRecipes.init();
        }
        if (RecipesSwitch.CircuitChain) CircuitChain.init();
        if (RecipesSwitch.MiscRecipes) {
            MiscRecipes.init();
            threngMisc.init();
        }
        if (RecipesSwitch.MachineRecipes) MachineRecipes.init();
        if (RecipesSwitch.ThrengRecipes) threngTile.init();
    }

}
