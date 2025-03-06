package keqing.gtqt.prismplan.common;

import appeng.api.AEApi;
import appeng.api.storage.ICellHandler;
import gregtech.common.items.MetaItems;
import gregtech.common.metatileentities.MetaTileEntities;
import keqing.gtqt.prismplan.api.utils.PrismPlanLog;
import keqing.gtqt.prismplan.common.metatileentities.multi.multiblock.estorage.EStorageCellHandler;
import keqing.gtqt.prismplan.common.metatileentities.multi.multiblock.estorage.EStorageEventHandler;
import keqing.gtqt.prismplan.integration.theoneprobe.IntegrationTOP;
import keqing.gtqt.prismplan.mixin.ae2.AccessorCellRegistry;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.List;

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
        MinecraftForge.EVENT_BUS.register(EStorageEventHandler.INSTANCE);
    }
    public void preLoad() {

    }
    public void init() {
        IntegrationTOP.registerProvider();

        List<ICellHandler> handlers = ((AccessorCellRegistry) (AEApi.instance().registries().cell())).getHandlers();
        handlers.add(0, EStorageCellHandler.INSTANCE);
    }
}