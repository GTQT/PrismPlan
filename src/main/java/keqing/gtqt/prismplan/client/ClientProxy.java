package keqing.gtqt.prismplan.client;

import keqing.gtqt.prismplan.client.textures.PrismPlanTextures;
import keqing.gtqt.prismplan.common.CommonProxy;
import keqing.gtqt.prismplan.common.block.PrismPlanBlocks;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber({Side.CLIENT})
public class ClientProxy extends CommonProxy {
    public ClientProxy() {
        MinecraftForge.EVENT_BUS.register(this);
    }
    public void preLoad()
    {
        super.preLoad();
        PrismPlanTextures.init();
        PrismPlanTextures.preInit();
    }
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event)
    {
        PrismPlanBlocks.registerItemModels();
    }
}

