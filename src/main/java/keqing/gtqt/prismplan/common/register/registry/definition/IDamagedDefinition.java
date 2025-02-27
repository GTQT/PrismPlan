package keqing.gtqt.prismplan.common.register.registry.definition;

import appeng.api.definitions.IItemDefinition;
import co.neeve.nae2.common.registration.registry.rendering.IModelProvider;
import net.minecraft.item.ItemStack;

import java.util.Collection;

public interface IDamagedDefinition<T extends IItemDefinition, U extends IModelProvider> extends IRegisterDefinition<T> {

    Collection<U> getEntries();

    U getType(ItemStack stack);
}
