package keqing.gtqt.prismplan.mixin.threng;

import io.github.phantamanta44.threng.recipe.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ThrEngRecipes.class)
public abstract class MixinThrEngRecipes {

    @Overwrite
    public static void registerRecipeTypes() {
        // 方法体为空，原方法内容被覆盖，不会注册任何配方
    }
    /**
     * @author
     * @reason
     */
    @Overwrite
    public static void addRecipes() {
        // 方法体为空，原方法内容被覆盖，不会注册任何配方
    }
}