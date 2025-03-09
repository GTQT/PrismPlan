package keqing.gtqt.prismplan.common;
import appeng.api.storage.ICellRegistry;
import appeng.core.features.registries.cell.CellRegistry;
import appeng.api.AEApi;
import appeng.api.storage.ICellHandler;
import gregtech.api.block.VariantItemBlock;
import gregtech.common.metatileentities.MetaTileEntities;
import keqing.gtqt.prismplan.api.utils.PrismPlanLog;
import keqing.gtqt.prismplan.client.textures.PrismPlanTextures;
import keqing.gtqt.prismplan.common.block.PrismPlanBlocks;
import keqing.gtqt.prismplan.common.metatileentities.multi.multiblock.estorage.EStorageCellHandler;
import keqing.gtqt.prismplan.common.metatileentities.multi.multiblock.estorage.EStorageEventHandler;
import keqing.gtqt.prismplan.integration.theoneprobe.IntegrationTOP;
import keqing.gtqt.prismplan.loaders.recipes.PrismPlanRecipes;
import keqing.gtqt.prismplan.mixin.ae2.AccessorCellRegistry;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

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
        PrismPlanRecipes.init();
        List<ICellHandler> handlers = ((AccessorCellRegistry) (AEApi.instance().registries().cell())).getHandlers();
        handlers.add(0, EStorageCellHandler.INSTANCE);
    }
    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        PrismPlanLog.logger.info("Registering blocks...");
        IForgeRegistry<Block> registry = event.getRegistry();
        registry.register(PrismPlanBlocks.blockMultiblockCasing);

    }
    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        PrismPlanLog.logger.info("Registering Items...");
        IForgeRegistry<Item> registry = event.getRegistry();
        registry.register(createItemBlock(PrismPlanBlocks.blockMultiblockCasing, VariantItemBlock::new));

    }
    private static <T extends Block> ItemBlock createItemBlock(T block, Function<T, ItemBlock> producer) {
        ItemBlock itemBlock = producer.apply(block);
        itemBlock.setRegistryName(Objects.requireNonNull(block.getRegistryName()));
        return itemBlock;
    }
}