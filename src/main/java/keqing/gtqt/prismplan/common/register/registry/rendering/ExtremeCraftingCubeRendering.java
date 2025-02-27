package keqing.gtqt.prismplan.common.register.registry.rendering;


import appeng.block.crafting.BlockCraftingUnit;
import appeng.bootstrap.BlockRenderingCustomizer;
import appeng.bootstrap.IBlockRendering;
import appeng.bootstrap.IItemRendering;
import keqing.gtqt.prismplan.api.utils.PrimsPlanUtility;
import keqing.gtqt.prismplan.client.model.ExtremeCraftingCubeModel;
import keqing.gtqt.prismplan.common.block.ae2.BlockExtremeCraftingUnit;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Map;

public class ExtremeCraftingCubeRendering extends BlockRenderingCustomizer {
    private final String registryName;
    private final BlockExtremeCraftingUnit.ExtremeCraftingUnitType type;

    public ExtremeCraftingCubeRendering(String registryName, BlockExtremeCraftingUnit.ExtremeCraftingUnitType type) {
        this.registryName = registryName;
        this.type = type;
    }

    @SideOnly(Side.CLIENT)
    public void customize(IBlockRendering rendering, IItemRendering itemRendering) {
        ResourceLocation baseName = PrimsPlanUtility.prismPlanID(registryName);
        ModelResourceLocation defaultModel = new ModelResourceLocation(baseName, "normal");
        String builtInName = "models/block/crafting/" + registryName + "/builtin";
        ModelResourceLocation builtInModelName = new ModelResourceLocation(PrimsPlanUtility.prismPlanID(builtInName), "normal");
        rendering.builtInModel(builtInName, new ExtremeCraftingCubeModel(type));
        rendering.stateMapper(block -> mapState(block, defaultModel, builtInModelName));
        rendering.modelCustomizer((loc, model) -> model);
    }

    @SideOnly(Side.CLIENT)
    private Map<IBlockState, ModelResourceLocation> mapState(Block block, ModelResourceLocation defaultModel, ModelResourceLocation formedModel) {
        Map<IBlockState, ModelResourceLocation> result = new HashMap<>();

        for (IBlockState state : block.getBlockState().getValidStates()) {
            if (state.getValue(BlockCraftingUnit.FORMED)) {
                result.put(state, formedModel);
            } else {
                result.put(state, defaultModel);
            }
        }

        return result;
    }
}