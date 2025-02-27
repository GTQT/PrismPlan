package keqing.gtqt.prismplan.api.utils;

import keqing.gtqt.prismplan.Tags;
import net.minecraft.util.ResourceLocation;

public class PrimsPlanUtility {

    public static ResourceLocation prismPlanID( String path) {
        return new ResourceLocation(Tags.MOD_ID, path);
    }
}
