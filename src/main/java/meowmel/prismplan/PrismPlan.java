package meowmel.prismplan;

import meowmel.prismplan.api.utils.PrismPlanLog;
import meowmel.prismplan.client.ClientProxy;
import meowmel.prismplan.common.CommonProxy;
import meowmel.prismplan.common.block.PrismPlanBlocks;
import meowmel.prismplan.common.item.PrismPlanMetaItems;
import meowmel.prismplan.common.metatileentities.PrismPlanMetaTileEntities;
import meowmel.prismplan.common.network.PktCellDriveStatusUpdate;
import meowmel.prismplan.common.register.AE2RegisterManager;
import meowmel.prismplan.prismplan.Tags;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION,
        acceptedMinecraftVersions = "[1.12.2]",
        dependencies = "required:forge@[14.23.5.2847,);" +
                "required-after:codechickenlib@[3.2.3,);" +
                "required-after:modularui@[2.4.3,);" +
                "required-after:gregtech@[2.8.0-beta,);" +
                "after:appliedenergistics2@[v0.56.4,);")

public class PrismPlan {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);

    @Mod.Instance(Tags.MOD_ID)
    public static PrismPlan instance;

    @SidedProxy(
            clientSide = "meowmel.prismplan.client.ClientProxy",
            serverSide = "meowmel.prismplan.common.CommonProxy"
    )
    public static CommonProxy Proxy;
    public static ClientProxy clientProxy;

    private static AE2RegisterManager registerManager;

    public static final SimpleNetworkWrapper NET_CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(Tags.MOD_ID);
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        byte start = 0;

        NET_CHANNEL.registerMessage(PktCellDriveStatusUpdate.class, PktCellDriveStatusUpdate.class, start++, Side.CLIENT);

        start = 64;

        PrismPlanLog.init(event.getModLog());
        registerManager = new AE2RegisterManager();
        registerManager.onPreInit(event);

        Proxy.preLoad();
        Proxy.preInit();
        PrismPlanBlocks.init();
        PrismPlanMetaItems.initialization();
        PrismPlanMetaTileEntities.initialization();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        registerManager.onInit(event);
        Proxy.init();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        registerManager.onPostInit(event);
    }
}