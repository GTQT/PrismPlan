package keqing.gtqt.prismplan.component;

import appeng.bootstrap.components.IPreInitComponent;
import com.google.common.base.Preconditions;
import keqing.gtqt.prismplan.client.model.AE2BuiltInModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class AE2BuiltInModelComponent implements IPreInitComponent {

    private final Map<String, IModel> builtInModels = new HashMap<>();
    private boolean hasInitialized = false;

    public AE2BuiltInModelComponent() {
    }

    /**
     * Add a new Model (at {@code path}).
     *
     * @param path  Model File path.
     * @param model Model.
     */
    public void addModel(String path, IModel model) {
        //  If model is not initialized, then run.
        Preconditions.checkState(!this.hasInitialized);
        this.builtInModels.put(path, model);
    }

    @Override
    public void preInitialize(Side side) {
        this.hasInitialized = true;
        var loader = new AE2BuiltInModelLoader(this.builtInModels);
        ModelLoaderRegistry.registerLoader(loader);
    }

}