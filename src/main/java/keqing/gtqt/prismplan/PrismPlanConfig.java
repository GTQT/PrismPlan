package keqing.gtqt.prismplan;

import net.minecraftforge.common.config.Config;

@Config(modid = Tags.MOD_ID)
public class PrismPlanConfig {

    @Config.Comment("Prism Plan Config")
    public static MachineSwitch MachineSwitch = new MachineSwitch();
    public static RecipesSwitch RecipesSwitch = new RecipesSwitch();
    public static class MachineSwitch {
        @Config.Comment("注册设备的初始ID值，将向后占用200位")
        @Config.RequiresMcRestart
        @Config.Name("Start ID ")
        public int startId = 29800;
    }

    public static class RecipesSwitch {

        public boolean MiscRecipes = true;
        public boolean CellRecipes = true;
        public boolean MachineRecipes = true;
        public boolean StorageCraftingRecipes = true;
        public boolean CircuitChain = true;
    }
}
