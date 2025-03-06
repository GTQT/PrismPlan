package keqing.gtqt.prismplan.common.item;


import keqing.gtqt.prismplan.Tags;
import keqing.gtqt.prismplan.common.item.ae2.cell.ToolPortableCellLarge;
import keqing.gtqt.prismplan.common.item.ae2.estorage.EStorageCellFluid;
import keqing.gtqt.prismplan.common.item.ae2.estorage.EStorageCellItem;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.LinkedList;
import java.util.List;

import static keqing.gtqt.prismplan.common.CommonProxy.PRISM_PLAN_TAB;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public class ItemRegistry {
    public static ToolPortableCellLarge LARGE_PORTABLE_CELL_2048;
    public static ToolPortableCellLarge LARGE_PORTABLE_CELL_8192;
    public static ToolPortableCellLarge LARGE_PORTABLE_CELL_32768;
    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        LARGE_PORTABLE_CELL_2048 = new ToolPortableCellLarge(2048,63);
        LARGE_PORTABLE_CELL_2048
                .setRegistryName(Tags.MOD_ID, "large_portable_cell_2048")
                .setTranslationKey("large_portable_cell_2048")
                .setCreativeTab(PRISM_PLAN_TAB);
        event.getRegistry().register(LARGE_PORTABLE_CELL_2048);

        LARGE_PORTABLE_CELL_8192 = new ToolPortableCellLarge(8192,63);
        LARGE_PORTABLE_CELL_8192
                .setRegistryName(Tags.MOD_ID, "large_portable_cell_8192")
                .setTranslationKey("large_portable_cell_8192")
                .setCreativeTab(PRISM_PLAN_TAB);
        event.getRegistry().register(LARGE_PORTABLE_CELL_8192);

        LARGE_PORTABLE_CELL_32768 = new ToolPortableCellLarge(32768,63);
        LARGE_PORTABLE_CELL_32768
                .setRegistryName(Tags.MOD_ID, "large_portable_cell_32768")
                .setTranslationKey("large_portable_cell_32768")
                .setCreativeTab(PRISM_PLAN_TAB);
        event.getRegistry().register(LARGE_PORTABLE_CELL_32768);

        event.getRegistry().register(EStorageCellItem.LEVEL_A);
        event.getRegistry().register(EStorageCellItem.LEVEL_B);
        event.getRegistry().register(EStorageCellItem.LEVEL_C);
        event.getRegistry().register(EStorageCellItem.LEVEL_D);
        event.getRegistry().register(EStorageCellItem.LEVEL_E);
        event.getRegistry().register(EStorageCellFluid.LEVEL_A);
        event.getRegistry().register(EStorageCellFluid.LEVEL_B);
        event.getRegistry().register(EStorageCellFluid.LEVEL_C);
        event.getRegistry().register(EStorageCellFluid.LEVEL_D);
        event.getRegistry().register(EStorageCellFluid.LEVEL_E);
    }
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void registerModels(ModelRegistryEvent event) {
        registerModel(LARGE_PORTABLE_CELL_2048);
        registerModel(LARGE_PORTABLE_CELL_8192);
        registerModel(LARGE_PORTABLE_CELL_32768);
        registerModel(EStorageCellItem.LEVEL_A);
        registerModel(EStorageCellItem.LEVEL_B);
        registerModel(EStorageCellItem.LEVEL_C);
        registerModel(EStorageCellItem.LEVEL_D);
        registerModel(EStorageCellItem.LEVEL_E);
        registerModel(EStorageCellFluid.LEVEL_A);
        registerModel(EStorageCellFluid.LEVEL_B);
        registerModel(EStorageCellFluid.LEVEL_C);
        registerModel(EStorageCellFluid.LEVEL_D);
        registerModel(EStorageCellFluid.LEVEL_E);
    }

    @SideOnly(Side.CLIENT)
    private static void registerModel(Item item) {
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }
}