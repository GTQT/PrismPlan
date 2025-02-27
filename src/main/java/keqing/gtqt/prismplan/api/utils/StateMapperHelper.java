package keqing.gtqt.prismplan.api.utils;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.util.ResourceLocation;

public class StateMapperHelper extends StateMapperBase {

    private final ResourceLocation registryName;

    public StateMapperHelper(ResourceLocation registryName) {
        this.registryName = registryName;
    }

    @Override
    public ModelResourceLocation getModelResourceLocation(IBlockState state) {
        return new ModelResourceLocation(this.registryName, this.getPropertyString(state.getProperties()));
    }

}

