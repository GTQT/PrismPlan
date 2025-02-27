package keqing.gtqt.prismplan.common.register.registry.definition;

import appeng.api.definitions.IItemDefinition;

import java.util.Optional;

public interface IRegisterDefinition<T extends IItemDefinition> {

    Optional<T> getById(String id);
}
