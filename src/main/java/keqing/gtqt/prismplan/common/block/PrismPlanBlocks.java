package keqing.gtqt.prismplan.common.block;

import appeng.api.definitions.ITileDefinition;
import appeng.block.crafting.BlockCraftingUnit;
import gregtech.common.blocks.MetaBlocks;
import keqing.gtqt.prismplan.common.block.prismPlan.BlockMultiblockCasing;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PrismPlanBlocks {
    public static BlockMultiblockCasing blockMultiblockCasing;

    private PrismPlanBlocks() {}

    public static void init() {
        blockMultiblockCasing = new BlockMultiblockCasing();
        blockMultiblockCasing.setRegistryName("multiblock_casing");
    }
    @SideOnly(Side.CLIENT)
    public static void registerItemModels() {
        registerItemModel(blockMultiblockCasing);
    }
    @SideOnly(Side.CLIENT)
    private static void registerItemModel(Block block) {
        for (IBlockState state : block.getBlockState().getValidStates()) {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block),
                    block.getMetaFromState(state),
                    new ModelResourceLocation(block.getRegistryName(),
                            MetaBlocks.statePropertiesToString(state.getProperties())));
        }

    }
}
