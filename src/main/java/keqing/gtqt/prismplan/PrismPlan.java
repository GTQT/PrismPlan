package keqing.gtqt.prismplan;

import keqing.gtqt.prismplan.api.utils.PrismPlanLog;
import keqing.gtqt.prismplan.client.ClientProxy;
import keqing.gtqt.prismplan.common.CommonProxy;
import keqing.gtqt.prismplan.common.metatileentities.multi.PrismPlanMetaTileEntities;
import keqing.gtqt.prismplan.common.register.AE2RegisterManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION,
        acceptedMinecraftVersions = "[1.12.2]",
        dependencies = "required:forge@[14.23.5.2847,);" +
                "required-after:codechickenlib@[3.2.3,);" +
                "required-after:modularui@[2.4.3,);" +
                "required-after:gregtech@[2.8.10-beta,);" +
                "after:appliedenergistics2@[v0.56.6,);" +
                "after:jei@[4.25.1,);")
public class PrismPlan {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);

    @Mod.Instance(Tags.MOD_ID)
    public static PrismPlan instance;

    @SidedProxy(
            clientSide = "keqing.gtqt.prismplan.client.ClientProxy",
            serverSide = "keqing.gtqt.prismplan.common.CommonProxy"
    )
    public static CommonProxy Proxy;
    public static ClientProxy clientProxy;

    private static AE2RegisterManager registerManager;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        PrismPlanLog.init(event.getModLog());
        registerManager = new AE2RegisterManager();
        registerManager.onPreInit(event);
        PrismPlanMetaTileEntities.initialization();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        registerManager.onInit(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        registerManager.onPostInit(event);
    }
}