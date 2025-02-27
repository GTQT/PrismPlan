package keqing.gtqt.prismplan.common;

import gregtech.common.items.MetaItems;
import gregtech.common.metatileentities.MetaTileEntities;
import keqing.gtqt.prismplan.api.utils.PrismPlanLog;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(
        modid = "prismplan"
)
public class CommonProxy {

    public CommonProxy() {
    }

    public static final CreativeTabs PRISM_PLAN_TAB = new CreativeTabs("prism_plan") {
        @Override
        public ItemStack createIcon() {
            return MetaTileEntities.ITEM_IMPORT_BUS_ME.getStackForm();
        }
    };

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        PrismPlanLog.logger.info("Registering blocks...");
        //IForgeRegistry<Block> registry = event.getRegistry();
    }
    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {

    }
    public void postInit() {
    }

    public void construction() {
    }

    public void loadComplete() {

    }
    public void preInit() {

    }
    public void preLoad() {

    }
    public void init() {

    }
}