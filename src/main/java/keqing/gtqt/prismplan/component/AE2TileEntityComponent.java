package keqing.gtqt.prismplan.component;

import appeng.bootstrap.components.IPreInitComponent;
import appeng.bootstrap.definitions.TileEntityDefinition;
import keqing.gtqt.prismplan.api.utils.PrimsPlanUtility;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.List;

public class AE2TileEntityComponent implements IPreInitComponent {

    private final List<TileEntityDefinition> definitions = new ArrayList<>();

    public AE2TileEntityComponent() {
    }

    /**
     * Add Tile Entity to {@code TileEntityDefinition} list.
     *
     * @param definition Tile Entity Definition.
     */
    public void addTileEntity(TileEntityDefinition definition) {
        //  If Tile Entity Definition is not in the total list, then add it to list.
        if (!this.definitions.contains(definition))
            this.definitions.add(definition);
    }

    /**
     * Pre-initialized Tile Entity in {@code gtlitecore} namespace.
     *
     * @param side Pre Init side.
     */
    @Override
    public void preInitialize(Side side) {
        // Traverse all {@code TileEntityDefinition}, and register it via {@code GameRegistry}.
        for (var definition : definitions) {
            GameRegistry.registerTileEntity(definition.getTileEntityClass(), PrimsPlanUtility.prismPlanID(definition.getName()));
            // Set state of registered Tile Entity be true.
            definition.setRegistered(true);
        }
    }
}