package keqing.gtqt.prismplan.loaders.recipes;

import keqing.gtqt.prismplan.loaders.recipes.naeRecipes.NaeCellRecipes;
import keqing.gtqt.prismplan.loaders.recipes.naeRecipes.NaeTileRecipes;
import keqing.gtqt.prismplan.loaders.recipes.threngRecipes.threngMisc;
import keqing.gtqt.prismplan.loaders.recipes.threngRecipes.threngTile;
import net.minecraftforge.fml.common.Loader;

import static keqing.gtqt.prismplan.PrismPlanConfig.RecipesSwitch;

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
