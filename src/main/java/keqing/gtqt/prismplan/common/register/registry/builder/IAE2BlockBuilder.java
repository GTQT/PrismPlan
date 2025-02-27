package keqing.gtqt.prismplan.common.register.registry.builder;

import appeng.api.definitions.IBlockDefinition;
import appeng.bootstrap.BlockRenderingCustomizer;
import appeng.bootstrap.IBootstrapComponent;
import appeng.bootstrap.definitions.TileEntityDefinition;
import co.neeve.nae2.common.features.IFeature;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface IAE2BlockBuilder {

    IAE2BlockBuilder bootstrap(BiFunction<Block, Item, IBootstrapComponent> bootstrap);

    IAE2BlockBuilder features(IFeature... features);

    IAE2BlockBuilder rendering(BlockRenderingCustomizer rendering);

    IAE2BlockBuilder tileEntity(TileEntityDefinition definition);

    IAE2BlockBuilder disableItem();

    IAE2BlockBuilder useCustomItemModel();

    IAE2BlockBuilder item(Function<Block, ItemBlock> item);

    <T extends IBlockDefinition> T build();

    IAE2BlockBuilder withJEIDescription();
}
