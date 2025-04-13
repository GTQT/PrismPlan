package keqing.gtqt.prismplan.api.utils;


import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class AE2ClientUtility {

    private AE2ClientUtility() {
    }

    public static Minecraft mc() {
        return Minecraft.getMinecraft();
    }

    public static ItemRenderer itemRenderer() {
        return mc().getItemRenderer();
    }

    public static RenderItem renderItem() {
        return mc().getItemRenderer().itemRenderer;
    }

    public static BlockRendererDispatcher blockRenderer() {
        return mc().blockRenderDispatcher;
    }

    public static BlockModelRenderer blockModelRenderer() {
        return blockRenderer().getBlockModelRenderer();
    }

    public static BlockModelShapes blockModelShapes() {
        return blockRenderer().getBlockModelShapes();
    }

    public static ItemModelMesher itemModelMesher() {
        return renderItem().getItemModelMesher();
    }

    public static ModelManager modelManager() {
        return blockModelShapes().getModelManager();
    }

    public static IBakedModel missingModel() {
        return modelManager().getMissingModel();
    }

}
