package keqing.gtqt.prismplan;

import gregtech.api.GregTechAPI;
import gregtech.api.metatileentity.registry.MTEManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class PrismPlanEventHandlers {

    @SubscribeEvent
    public static void registerMTERegistry(MTEManager.MTERegistryEvent event) {
        GregTechAPI.mteManager.createRegistry(Tags.MOD_ID);
    }
}
