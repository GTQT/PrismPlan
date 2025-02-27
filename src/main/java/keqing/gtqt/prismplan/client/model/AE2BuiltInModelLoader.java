package keqing.gtqt.prismplan.client.model;

import com.google.common.collect.ImmutableMap;
import keqing.gtqt.prismplan.Tags;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;

import java.util.Map;

public class AE2BuiltInModelLoader implements ICustomModelLoader {

    private final Map<String, IModel> builtInModels;

    public AE2BuiltInModelLoader(Map<String, IModel> builtInModels) {
        this.builtInModels = ImmutableMap.copyOf(builtInModels);
    }

    // TODO Check if this override is running properly.
    @Override
    public boolean accepts(ResourceLocation modelLocation) {
        return modelLocation.getNamespace().equals(Tags.MOD_ID)
                && this.builtInModels.containsKey(modelLocation.getPath());
    }

    @Override
    public IModel loadModel(ResourceLocation modelLocation) {
        return this.builtInModels.get(modelLocation.getPath());
    }

    // TODO Find some substitute of IResourceManagerReloadListener.
    @SuppressWarnings("deprecation")
    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        for (var model : this.builtInModels.values()) {
            if (model instanceof IResourceManagerReloadListener) {
                ((IResourceManagerReloadListener) model).onResourceManagerReload(resourceManager);
            }
        }
    }

}
