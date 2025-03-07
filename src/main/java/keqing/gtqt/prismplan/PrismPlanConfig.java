package keqing.gtqt.prismplan;

import net.minecraftforge.common.config.Config;

@Config(modid = Tags.MOD_ID)
public class PrismPlanConfig {

    @Config.Comment("Prism Plan Config")
    public static MachineSwitch MachineSwitch = new MachineSwitch();

    public static class MachineSwitch {
        @Config.Comment("注册设备的初始ID值，将向后占用200位")
        @Config.RequiresMcRestart
        @Config.Name("Start ID ")
        public int startId = 29800;
    }
}
