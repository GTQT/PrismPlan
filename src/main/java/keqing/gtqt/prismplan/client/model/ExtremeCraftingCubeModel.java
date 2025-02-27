package keqing.gtqt.prismplan.client.model;

import com.google.common.collect.ImmutableList;
import keqing.gtqt.prismplan.api.utils.AE2ClientUtility;
import keqing.gtqt.prismplan.api.utils.PrimsPlanUtility;
import keqing.gtqt.prismplan.api.utils.PrismPlanLog;
import keqing.gtqt.prismplan.common.block.ae2.BlockExtremeCraftingUnit;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

public class ExtremeCraftingCubeModel implements IModel {
    private static final ResourceLocation RING_CORNER = texture("ring_corner");
    private static final ResourceLocation RING_SIDE_HOR = texture("ring_side_hor");
    private static final ResourceLocation RING_SIDE_VER = texture("ring_side_ver");
    private static final ResourceLocation UNIT_BASE = texture("unit_base");
    private static final ResourceLocation LIGHT_BASE = texture("light_base");

    private static final ResourceLocation STORAGE_65536K_LIGHT = gtliteTexture("crafting_storage_65536k_light");
    private static final ResourceLocation STORAGE_262144K_LIGHT = gtliteTexture("crafting_storage_262144k_light");
    private static final ResourceLocation STORAGE_1048576K_LIGHT = gtliteTexture("crafting_storage_1048576k_light");
    private static final ResourceLocation COPROCESSOR_256X_LIGHT = gtliteTexture("coprocessor_256x_light");
    private static final ResourceLocation COPROCESSOR_1024X_LIGHT = gtliteTexture("coprocessor_1024x_light");
    private static final ResourceLocation COPROCESSOR_4096X_LIGHT = gtliteTexture("coprocessor_4096x_light");

    private static final Class<?>[] ARGS = { VertexFormat.class, TextureAtlasSprite.class, TextureAtlasSprite.class, TextureAtlasSprite.class, TextureAtlasSprite.class, TextureAtlasSprite.class };

    private static Class<?> modelClass;

    static {
        try {
            modelClass = Class.forName("co.neeve.nae2.client.rendering.models.crafting.DenseLightBakedModel");
        } catch (ClassNotFoundException e) {
            PrismPlanLog.logger.error("Can not load co.neeve.nae2.client.rendering.models.crafting.DenseLightBakedModel");
        }
    }

    private final BlockExtremeCraftingUnit.ExtremeCraftingUnitType type;

    public ExtremeCraftingCubeModel(final BlockExtremeCraftingUnit.ExtremeCraftingUnitType type) {
        this.type = type;
    }

    private static TextureAtlasSprite getLightTexture(
            Function<ResourceLocation,
            TextureAtlasSprite> textureGetter,
            BlockExtremeCraftingUnit.ExtremeCraftingUnitType type) {

        return switch (type) {
            case STORAGE_65536K -> textureGetter.apply(STORAGE_65536K_LIGHT);
            case STORAGE_262144K -> textureGetter.apply(STORAGE_262144K_LIGHT);
            case STORAGE_1048576K -> textureGetter.apply(STORAGE_1048576K_LIGHT);
            case COPROCESSOR_256X -> textureGetter.apply(COPROCESSOR_256X_LIGHT);
            case COPROCESSOR_1024X -> textureGetter.apply(COPROCESSOR_1024X_LIGHT);
            case COPROCESSOR_4096X -> textureGetter.apply(COPROCESSOR_4096X_LIGHT);
        };
    }

    private static ResourceLocation texture(String name) {
        return new ResourceLocation("appliedenergistics2", "blocks/crafting/" + name);
    }

    private static ResourceLocation gtliteTexture(String name) {
        return PrimsPlanUtility.prismPlanID("blocks/crafting/" + name);
    }


    @Override
    public Collection<ResourceLocation> getDependencies() {
        return Collections.emptyList();
    }


    @Override
    public Collection<ResourceLocation> getTextures() {
        return ImmutableList.of(
                RING_CORNER, RING_SIDE_HOR, RING_SIDE_VER, UNIT_BASE, LIGHT_BASE,
                STORAGE_65536K_LIGHT, STORAGE_262144K_LIGHT, STORAGE_1048576K_LIGHT,
                COPROCESSOR_256X_LIGHT, COPROCESSOR_1024X_LIGHT, COPROCESSOR_4096X_LIGHT);
    }

    @Override
    public IBakedModel bake( IModelState state,  VertexFormat format,
                            Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        TextureAtlasSprite ringCorner = bakedTextureGetter.apply(RING_CORNER);
        TextureAtlasSprite ringSideHor = bakedTextureGetter.apply(RING_SIDE_HOR);
        TextureAtlasSprite ringSideVer = bakedTextureGetter.apply(RING_SIDE_VER);
        if (modelClass != null) {
            try {
                Constructor<?> constructor = modelClass.getDeclaredConstructor(ARGS);
                constructor.setAccessible(true);
                return (IBakedModel) constructor.newInstance(format, ringCorner, ringSideHor, ringSideVer, bakedTextureGetter.apply(LIGHT_BASE), getLightTexture(bakedTextureGetter, this.type));
            } catch (Exception e) {
                PrismPlanLog.logger.error("Can not create model");
            }
        }
        return AE2ClientUtility.missingModel();
    }

    public IModelState getDefaultState() {
        return TRSRTransformation.identity();
    }
}
