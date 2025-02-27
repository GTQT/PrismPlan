package keqing.gtqt.prismplan.client.rendering;

import appeng.bootstrap.IItemRendering;
import appeng.bootstrap.components.ItemColorComponent;
import appeng.bootstrap.components.ItemMeshDefinitionComponent;
import appeng.bootstrap.components.ItemModelComponent;
import appeng.bootstrap.components.ItemVariantsComponent;
import com.google.common.collect.ImmutableMap;
import keqing.gtqt.prismplan.api.utils.StateMapperHelper;
import keqing.gtqt.prismplan.common.register.registry.AE2Registry;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;

public class AE2ItemRendering implements IItemRendering {

    @SideOnly(Side.CLIENT)
    private final Map<Integer, ModelResourceLocation> itemModels = new HashMap<>();
    @SideOnly(Side.CLIENT)
    private final Set<ResourceLocation> variants = new HashSet<>();
    @SideOnly(Side.CLIENT)
    private final Map<String, IModel> builtInModels = new HashMap<>();
    @SideOnly(Side.CLIENT)
    private IItemColor itemColor;
    @SideOnly(Side.CLIENT)
    private ItemMeshDefinition meshDefinition;

    @SideOnly(Side.CLIENT)
    @Override
    public IItemRendering meshDefinition(ItemMeshDefinition meshDefinition) {
        this.meshDefinition = meshDefinition;
        return this;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IItemRendering model(int meta, ModelResourceLocation model) {
        this.itemModels.put(meta, model);
        return this;
    }

    @Override
    public IItemRendering variants(Collection<ResourceLocation> resources) {
        this.variants.addAll(resources);
        return this;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IItemRendering color(IItemColor itemColor) {
        this.itemColor = itemColor;
        return this;
    }

    @Override
    public IItemRendering builtInModel(String name, IModel model) {
        this.builtInModels.put(name, model);
        return this;
    }

    /**
     * Apply Rendering to Item.
     *
     * @param registry  AE2 Registry (used to add Bootstrap Component to Item).
     * @param item      Item.
     */
    public void apply(AE2Registry registry, Item item) {
        //  Add missing Bootstrap Components.
        if (this.meshDefinition != null)
            registry.addBootstrapComponent(new ItemMeshDefinitionComponent(item, this.meshDefinition));
        if (!this.itemModels.isEmpty())
            registry.addBootstrapComponent(new ItemModelComponent(item, this.itemModels));

        Set<ResourceLocation> resources = new HashSet<>(this.variants);

        //  Register a default item model if neither items by meta nor an item mesh definition exist.
        if (this.meshDefinition == null && this.itemModels.isEmpty()) {
            ModelResourceLocation model;
            // For block items, the default will try to use the default state of the associated block.
            if (item instanceof ItemBlock) {
                var block = ((ItemBlock) item).getBlock();
                //  Only do this once the blocks are actually registered.
                var helper = new StateMapperHelper(item.getRegistryName());
                model = helper.getModelResourceLocation(block.getDefaultState());
            } else {
                model = new ModelResourceLocation(Objects.requireNonNull(item.getRegistryName()), "inventory");
            }
            registry.addBootstrapComponent(new ItemModelComponent(item, ImmutableMap.of(0, model)));
        }
        this.builtInModels.forEach(registry::addBuiltInModel);

        if (!resources.isEmpty()) {
            registry.addBootstrapComponent(new ItemVariantsComponent(item, resources));
        } else if (this.meshDefinition != null) {
            // Adding an empty variant list here will prevent Vanilla from trying to load the default item model in this case.
            registry.addBootstrapComponent(new ItemVariantsComponent(item, Collections.emptyList()));
        }

        if (this.itemColor != null) {
            registry.addBootstrapComponent(new ItemColorComponent(item, this.itemColor));
        }
    }
}

