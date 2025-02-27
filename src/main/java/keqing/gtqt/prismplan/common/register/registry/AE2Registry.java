package keqing.gtqt.prismplan.common.register.registry;

import appeng.bootstrap.IBootstrapComponent;
import appeng.util.Platform;
import keqing.gtqt.prismplan.common.register.registry.builder.AE2BlockBuilder;
import keqing.gtqt.prismplan.common.register.registry.builder.AE2ItemBuilder;
import keqing.gtqt.prismplan.common.register.registry.builder.IAE2BlockBuilder;
import keqing.gtqt.prismplan.common.register.registry.builder.IAE2ItemBuilder;
import keqing.gtqt.prismplan.component.AE2BuiltInModelComponent;
import keqing.gtqt.prismplan.component.AE2ModelOverrideComponent;
import keqing.gtqt.prismplan.component.AE2TileEntityComponent;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class AE2Registry {

    // TileEntityDefinition Component (rewrite from AE2).
    public final AE2TileEntityComponent tileEntityComponent;
    // Bootstrap Component map.
    private final Map<Class<? extends IBootstrapComponent>, List<IBootstrapComponent>> bootstrapComponents;
    // Model Override Component.
    @SideOnly(Side.CLIENT)
    private AE2ModelOverrideComponent modelOverrideComponent;
    // Built in Model Component.
    @SideOnly(Side.CLIENT)
    private AE2BuiltInModelComponent builtInModelComponent;

    public AE2Registry() {
        // Initialization Bootstrap Component map.
        this.bootstrapComponents = new HashMap<>();
        // Add TileEntityDefinition Component to Bootstrap Components.
        this.tileEntityComponent = new AE2TileEntityComponent();
        this.addBootstrapComponent(this.tileEntityComponent);
        if (Platform.isClient()) {
            // Add Model Override Component to Bootstrap Component.
            this.modelOverrideComponent = new AE2ModelOverrideComponent();
            this.addBootstrapComponent(this.modelOverrideComponent);
            // Add Built in Model Component to Bootstrap Component.
            this.builtInModelComponent = new AE2BuiltInModelComponent();
            this.addBootstrapComponent(this.builtInModelComponent);
        }
    }

    /**
     * Add Component to AE2 Bootstrap Components.
     *
     * @author Magic_Sweepy
     *
     * @param component  Component.
     *
     * @since 1.0.0
     */
    @SuppressWarnings("unchecked")
    public void addBootstrapComponent(IBootstrapComponent component) {
        Stream.of(component.getClass().getInterfaces())
                .filter(IBootstrapComponent.class::isAssignableFrom)
                .forEach(c -> this.addBootstrapComponent(
                        (Class<? extends IBootstrapComponent>) c, component));
    }

    /**
     * Add Component to AE2 Bootstrap Components.
     *
     * @author Magic_Sweepy
     *
     * @param eventType  Bootstrap Event Type.
     * @param component  Component.
     *
     * @since 1.0.0
     */
    public <T extends IBootstrapComponent> void addBootstrapComponent(Class<? extends IBootstrapComponent> eventType, T component) {
        this.bootstrapComponents.computeIfAbsent(eventType, c -> new ArrayList<>()).add(component);
    }

    /**
     * Get Bootstrap Component by Event Type.
     *
     * @author Magic_Sweepy
     *
     * @param eventType  Bootstrap Event Type.
     * @return           Bootstrap Component.
     *
     * @since 1.0.0
     */
    @SuppressWarnings("unchecked")
    public <T extends IBootstrapComponent> Iterator<T> getBootstrapComponents(Class<T> eventType) {
        return (Iterator<T>) this.bootstrapComponents.getOrDefault(eventType, Collections.emptyList()).iterator();
    }

    /**
     * Add Built in Model.
     *
     * @author Magic_Sweepy
     *
     * @param path   Model path.
     * @param model  Model.
     *
     * @since 1.0.0
     */
    @SideOnly(Side.CLIENT)
    public void addBuiltInModel(String path, IModel model) {
        this.builtInModelComponent.addModel(path, model);
    }

    /**
     * Add Override Model.
     *
     * @author Magic_Sweepy
     *
     * @param resourcePath  Model Path.
     * @param customizer    Customizer.
     *
     * @since 1.0.0
     */
    @SideOnly(Side.CLIENT)
    public void addModelOverride(String resourcePath,
                                 BiFunction<ModelResourceLocation, IBakedModel, IBakedModel> customizer) {
        this.modelOverrideComponent.addOverride(resourcePath, customizer);
    }

    /**
     * Add AE2 Item by {@code id} and {@code itemSupplier}.
     *
     * @param id    Item ID.
     * @param item  Item Supplier.
     * @return      AE2 Item.
     */
    public IAE2ItemBuilder addItem(String id, Supplier<Item> item) {
        return new AE2ItemBuilder(this, id, item);
    }

    /**
     * Add AE2 Block by {@code id} and {@code blockSupplier}.
     *
     * @param id     Block ID.
     * @param block  Block Supplier.
     * @return       AE2 Block.
     */
    public IAE2BlockBuilder addBlock(String id, Supplier<Block> block) {
        return new AE2BlockBuilder(this, id, block);
    }

}
