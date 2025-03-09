package keqing.gtqt.prismplan.loaders.recipes;

public class PrismPlanRecipes {
    private PrismPlanRecipes() {

    }

    public static void load() {
    }

    public static void init() {
        CellRecipes.init();
        StorageCraftingRecipes.init();
        CircuitChain.init();
        MiscRecipes.init();
        MachineRecipes.init();
    }

}
