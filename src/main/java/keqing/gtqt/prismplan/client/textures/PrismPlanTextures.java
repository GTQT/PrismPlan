package keqing.gtqt.prismplan.client.textures;

import codechicken.lib.texture.TextureUtils;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.cube.OrientedOverlayRenderer;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import net.minecraft.client.renderer.texture.TextureMap;

public class PrismPlanTextures {

    public static SimpleOverlayRenderer MULTI_CASING = new SimpleOverlayRenderer("multiblock/computing_casing");

    public static OrientedOverlayRenderer THREAD_OVERLAY;
    public static OrientedOverlayRenderer HYPER_THREAD_OVERLAY;
    public static OrientedOverlayRenderer NETWORK_HATCH;
    public static OrientedOverlayRenderer ENERGY_HATCH;
    public static OrientedOverlayRenderer PARALLEL_HATCH;

    public static void init() {
        THREAD_OVERLAY = new OrientedOverlayRenderer("multiblock/thread_overlay");
        HYPER_THREAD_OVERLAY = new OrientedOverlayRenderer("multiblock/hyper_thread_overlay");
        NETWORK_HATCH = new OrientedOverlayRenderer("multiblock/network_hatch");
        ENERGY_HATCH = new OrientedOverlayRenderer("multiblock/energy_hatch");
        PARALLEL_HATCH = new OrientedOverlayRenderer("multiblock/parallel_hatch");
    }

    public static void register(TextureMap textureMap) {

    }

    public static void preInit() {
        TextureUtils.addIconRegister(PrismPlanTextures::register);
    }
}
