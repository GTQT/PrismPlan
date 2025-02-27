package keqing.gtqt.prismplan.common.register.registry.builder;

import appeng.bootstrap.IBootstrapComponent;
import appeng.bootstrap.ItemRenderingCustomizer;
import appeng.core.features.ItemDefinition;
import co.neeve.nae2.common.features.IFeature;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.item.Item;

import java.util.function.Function;
import java.util.function.Supplier;

public interface IAE2ItemBuilder {

    IAE2ItemBuilder bootstrap(Function<Item, IBootstrapComponent> bootstrap);

    IAE2ItemBuilder features(IFeature... features);

    IAE2ItemBuilder creativeTab(CreativeTabs creativeTab);

    IAE2ItemBuilder rendering(ItemRenderingCustomizer rendering);

    IAE2ItemBuilder dispenserBehavior(Supplier<IBehaviorDispenseItem> behavior);

    ItemDefinition build();

    IAE2ItemBuilder hidden();
}
