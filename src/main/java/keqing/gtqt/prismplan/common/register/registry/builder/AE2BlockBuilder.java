package keqing.gtqt.prismplan.common.register.registry.builder;

import appeng.api.definitions.IBlockDefinition;
import appeng.block.AEBaseBlock;
import appeng.block.AEBaseItemBlock;
import appeng.block.AEBaseTileBlock;
import appeng.bootstrap.BlockRenderingCustomizer;
import appeng.bootstrap.IBlockRendering;
import appeng.bootstrap.IBootstrapComponent;
import appeng.bootstrap.IItemRendering;
import appeng.bootstrap.components.IBlockRegistrationComponent;
import appeng.bootstrap.components.IItemRegistrationComponent;
import appeng.bootstrap.components.IPreInitComponent;
import appeng.bootstrap.definitions.TileEntityDefinition;
import appeng.core.features.ActivityState;
import appeng.core.features.BlockDefinition;
import appeng.core.features.BlockStackSrc;
import appeng.core.features.TileDefinition;
import appeng.tile.AEBaseTile;
import appeng.util.Platform;
import co.neeve.nae2.common.features.IFeature;
import gregtech.api.util.Mods;
import keqing.gtqt.prismplan.Tags;
import keqing.gtqt.prismplan.api.utils.PrimsPlanUtility;
import keqing.gtqt.prismplan.client.rendering.AE2BlockRendering;
import keqing.gtqt.prismplan.client.rendering.AE2ItemRendering;
import keqing.gtqt.prismplan.common.register.registry.AE2Registry;
import keqing.gtqt.prismplan.integration.JeiModule;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static keqing.gtqt.prismplan.common.CommonProxy.PRISM_PLAN_TAB;

public class AE2BlockBuilder implements IAE2BlockBuilder {

    private final AE2Registry registry;

    private final String registryName;

    private final Supplier<? extends Block> blockSupplier;

    private final List<BiFunction<Block, Item, IBootstrapComponent>> bootstrapComponents = new ArrayList<>();
    private final CreativeTabs creativeTab = PRISM_PLAN_TAB;
    
    private IFeature[] features = null;
    private TileEntityDefinition tileEntityDefinition;

    private boolean disableItem = false;

    private Function<Block, ItemBlock> itemFactory;

    @SideOnly(Side.CLIENT)
    private AE2BlockRendering blockRendering;

    @SideOnly(Side.CLIENT)
    private AE2ItemRendering itemRendering;

    @SideOnly(Side.CLIENT)
    private boolean jeiDescription;

    public AE2BlockBuilder(AE2Registry registry, String id, Supplier<? extends Block> blockSupplier) {
        this.registry = registry;
        this.registryName = id;
        this.blockSupplier = blockSupplier;

        if (Platform.isClient()) {
            this.blockRendering = new AE2BlockRendering();
            this.itemRendering = new AE2ItemRendering();
        }
    }

    @Override
    public AE2BlockBuilder bootstrap(BiFunction<Block, Item, IBootstrapComponent> callback) {
        this.bootstrapComponents.add(callback);
        return this;
    }

    @Override
    public IAE2BlockBuilder features(IFeature... features) {
        this.features = features;
        return this;
    }

    @Override
    public AE2BlockBuilder rendering(BlockRenderingCustomizer callback) {
        if (Platform.isClient()) {
            this.customizeForClient(callback);
        }
        return this;
    }

    @Override
    public IAE2BlockBuilder tileEntity(TileEntityDefinition tileEntityDefinition) {
        this.tileEntityDefinition = tileEntityDefinition;
        return this;
    }

    @Override
    public IAE2BlockBuilder useCustomItemModel() {
        this.rendering(new BlockRenderingCustomizer() {
            @Override
            @SideOnly(Side.CLIENT)
            public void customize(IBlockRendering rendering, IItemRendering itemRendering) {
                var model = new ModelResourceLocation(PrimsPlanUtility.prismPlanID(AE2BlockBuilder.this.registryName), "inventory");
                itemRendering.model(model).variants(model);
            }
        });
        return this;
    }

    @Override
    public IAE2BlockBuilder item(Function<Block, ItemBlock> factory) {
        this.itemFactory = factory;
        return this;
    }

    @Override
    public IAE2BlockBuilder disableItem() {
        this.disableItem = true;
        return this;
    }

    @SideOnly(Side.CLIENT)
    private void customizeForClient(BlockRenderingCustomizer callback) {
        callback.customize(this.blockRendering, this.itemRendering);
    }

    @Override
    public IAE2BlockBuilder withJEIDescription() {
        if (Platform.isClient()) {
            this.jeiDescription = true;
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IBlockDefinition> T build() {
        if (this.features != null && Stream.of(this.features)
                .noneMatch(IFeature::isEnabled)) {
            return (T) new TileDefinition(this.registryName, null, null);
        }

        //  Create block and matching item, and set factory name of both
        var block = this.blockSupplier.get();
        block.setRegistryName(Tags.MOD_ID, this.registryName);
        block.setTranslationKey(Tags.MOD_ID + "." + this.registryName);

        var item = this.constructItemFromBlock(block);
        if (item != null) {
            item.setRegistryName(Tags.MOD_ID, this.registryName);
        }

        //  Register the item and block with the game.
        this.registry.addBootstrapComponent((IBlockRegistrationComponent) (side, registry) -> registry.register(block));
        if (item != null) {
            this.registry.addBootstrapComponent((IItemRegistrationComponent) (side, registry) -> registry.register(item));
        }

        block.setCreativeTab(this.creativeTab);

        //  Register all extra handlers.
        this.bootstrapComponents.forEach(component -> this.registry.addBootstrapComponent(component.apply(block, item)));

        if (this.tileEntityDefinition != null && block instanceof AEBaseTileBlock) {
            ((AEBaseTileBlock) block).setTileEntity(this.tileEntityDefinition.getTileEntityClass());
            if (this.tileEntityDefinition.getName() == null) {
                this.tileEntityDefinition.setName(this.registryName);
            }

        }

        if (Platform.isClient()) {
            if (block instanceof AEBaseTileBlock tileBlock) {
                this.blockRendering.apply(this.registry, block, tileBlock.getTileEntityClass());
            } else {
                this.blockRendering.apply(this.registry, block, null);
            }

            if (item != null) {
                this.itemRendering.apply(this.registry, item);
            }
        }

        final T definition;

        if (block instanceof AEBaseTileBlock) {
            this.registry.addBootstrapComponent((IPreInitComponent) side ->
                    AEBaseTile.registerTileItem(
                            this.tileEntityDefinition == null ? ((AEBaseTileBlock) block).getTileEntityClass() :
                                    this.tileEntityDefinition.getTileEntityClass(),
                            new BlockStackSrc(block, 0, ActivityState.Enabled)));

            if (this.tileEntityDefinition != null) {
                this.registry.tileEntityComponent.addTileEntity(this.tileEntityDefinition);
            }

            definition = (T) new TileDefinition(this.registryName, (AEBaseTileBlock) block, item);
        } else {
            definition = (T) new BlockDefinition(this.registryName, block, item);
        }


        if (Platform.isClient() && Mods.JustEnoughItems.isModLoaded()) {
            if (this.jeiDescription && item != null) {
                JeiModule.addDescription(definition, item.getTranslationKey() + ".desc");
            }
        }
        


        return definition;
    }
    
    private ItemBlock constructItemFromBlock(Block block) {
        if (this.disableItem) {
            return null;
        }
        if (this.itemFactory != null) {
            return this.itemFactory.apply(block);
        } else if (block instanceof AEBaseBlock) {
            return new AEBaseItemBlock(block);
        } else {
            return new ItemBlock(block);
        }
    }
}
