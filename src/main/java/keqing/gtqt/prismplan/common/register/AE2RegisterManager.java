package keqing.gtqt.prismplan.common.register;

import appeng.bootstrap.IModelRegistry;
import appeng.bootstrap.components.*;
import keqing.gtqt.prismplan.common.block.AE2Blocks;
import keqing.gtqt.prismplan.common.item.AE2Items;
import keqing.gtqt.prismplan.common.register.registry.AE2Registry;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AE2RegisterManager {

    private final AE2Registry registry;
    private final AE2Items item;
    //private final AE2Materials material;
    private final AE2Blocks block;
    //private final AE2Parts part;
    //private final AE2Upgrades upgrade;

    public AE2RegisterManager() {
        MinecraftForge.EVENT_BUS.register(this);
        this.registry = new AE2Registry();
        this.item = new AE2Items(this.registry);
        //this.material = new AE2Materials(this.registry);
        this.block = new AE2Blocks(this.registry);
        //this.part = new AE2Parts(this.registry);
        //this.upgrade = new AE2Upgrades(this.registry);
    }

    public void onPreInit(FMLPreInitializationEvent event) {
        this.registry.getBootstrapComponents(IPreInitComponent.class)
                .forEachRemaining(b -> b.preInitialize(event.getSide()));
    }

    public void onInit(FMLInitializationEvent event) {
        this.registry.getBootstrapComponents(IInitComponent.class)
                .forEachRemaining(b -> b.initialize(event.getSide()));
    }

    public void onPostInit(FMLPostInitializationEvent event) {
        this.registry.getBootstrapComponents(IPostInitComponent.class)
                .forEachRemaining(b -> b.postInitialize(event.getSide()));
    }

    @SubscribeEvent
    public void registerBlocks(final RegistryEvent.Register<Block> event) {
        final var registry = event.getRegistry();
        final var side = FMLCommonHandler.instance().getEffectiveSide();
        this.registry.getBootstrapComponents(IBlockRegistrationComponent.class)
                .forEachRemaining(b -> b.blockRegistration(side, registry));
    }

    @SubscribeEvent
    public void registerItems(final RegistryEvent.Register<Item> event) {
        final var registry = event.getRegistry();
        final var side = FMLCommonHandler.instance().getEffectiveSide();
        this.registry.getBootstrapComponents(IItemRegistrationComponent.class)
                .forEachRemaining(b -> b.itemRegistration(side, registry));
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void registerModels(final ModelRegistryEvent event) {
        final var registry = new ModelLoaderWrapper();
        final var side = FMLCommonHandler.instance().getEffectiveSide();
        this.registry.getBootstrapComponents(IModelRegistrationComponent.class)
                .forEachRemaining(b -> b.modelRegistration(side, registry));
    }

    private static class ModelLoaderWrapper implements IModelRegistry {

        @Override
        public void registerItemVariants(Item item, ResourceLocation... names) {
            ModelLoader.registerItemVariants(item, names);
        }

        @Override
        public void setCustomModelResourceLocation(Item item, int metadata, ModelResourceLocation model) {
            ModelLoader.setCustomModelResourceLocation(item, metadata, model);
        }

        @Override
        public void setCustomMeshDefinition(Item item, ItemMeshDefinition meshDefinition) {
            ModelLoader.setCustomMeshDefinition(item, meshDefinition);
        }

        @Override
        public void setCustomStateMapper(Block block, IStateMapper mapper) {
            ModelLoader.setCustomStateMapper(block, mapper);
        }

    }

}

