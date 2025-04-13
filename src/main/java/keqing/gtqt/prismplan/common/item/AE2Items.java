package keqing.gtqt.prismplan.common.item;

import appeng.api.AEApi;
import appeng.api.config.Upgrades;
import appeng.api.definitions.IItemDefinition;
import appeng.bootstrap.components.IPostInitComponent;
import co.neeve.nae2.common.features.Features;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import keqing.gtqt.prismplan.common.item.ae2.storage.*;
import keqing.gtqt.prismplan.common.register.registry.AE2Registry;
import keqing.gtqt.prismplan.common.register.registry.definition.IRegisterDefinition;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;


public class AE2Items implements IRegisterDefinition<IItemDefinition> {

    public static IItemDefinition storageCellQuantum;
    public static IItemDefinition storageCellFluidQuantum;
    public static IItemDefinition storageCellSingularity;
    public static IItemDefinition storageCellFluidSingularity;
    public static IItemDefinition storageCellUniverse;
    public static IItemDefinition storageCellFluidUniverse;
    private final Object2ObjectOpenHashMap<String, IItemDefinition> itemIdList = new Object2ObjectOpenHashMap<>();

    public AE2Items(AE2Registry registry) {
        // Quantum Item Storage Cell
        storageCellQuantum = this.registerItem(registry.addItem("quantum_cell",
                        () -> new QuantumItemCell(Long.MAX_VALUE / 8))
                .features(Features.DENSE_CELLS)
                .build());
        // Quantum Fluid Storage Cell
        storageCellFluidQuantum = registerItem(registry.addItem("quantum_cell_fluid",
                        () -> new QuantumFluidCell(Long.MAX_VALUE / 8000))
                .features(Features.DENSE_CELLS)
                .build());
        // Digital Singularity Storage Cell
        storageCellSingularity = registerItem(registry.addItem("singularity_cell",
                        SingularityItemCell::new)
                .features(Features.DENSE_CELLS)
                .build());
        // Digital Singularity Fluid Storage Cell
        storageCellFluidSingularity = registerItem(registry.addItem("singularity_cell_fluid",
                        SingularityFluidCell::new)
                .features(Features.DENSE_CELLS)
                .build());
        // Artificial Universe Storage Cell
        storageCellUniverse = registerItem(registry.addItem("universe_cell",
                        UniverseItemCell::new)
                .features(Features.DENSE_CELLS)
                .build());
        // Artificial Universe Fluid Storage Cell
        storageCellFluidUniverse = registerItem(registry.addItem("universe_cell_fluid",
                        UniverseFluidCell::new)
                .features(Features.DENSE_CELLS)
                .build());

        // Add Bootstrap Component to ItemDefinitions.
        registry.addBootstrapComponent((IPostInitComponent) c -> {
            var items = AEApi.instance().definitions().items();
            var cells = AEApi.instance().registries().cell();
            var cellDef = items.cell1k();
            var fluidCellDef = items.fluidCell1k();
            if (Features.DENSE_CELLS.isEnabled() && cellDef.isEnabled()) {
                // Add Mirror Cell Upgrade to Quantum Storage Cells.
                mirrorCellUpgrades(cellDef, new IItemDefinition[]{
                        storageCellQuantum,
                        storageCellFluidQuantum
                });
                // Add CellHandlers to AppEng Cell registry.
                cells.addCellHandler(new QuantumCell.Handler());
                cells.addCellHandler(new SingularityCell.Handler());
                cells.addCellHandler(new UniverseCell.Handler());
            }
        });
    }

    private static void mirrorCellUpgrades(IItemDefinition definition, IItemDefinition[] definitions) {
        var supported = new HashMap<Upgrades, Integer>();
        Stream.of(Upgrades.values())
                .forEach(upgrade -> upgrade.getSupported().entrySet().stream()
                        .filter(d -> definition.isSameAs(d.getKey()))
                        .map(Map.Entry::getValue)
                        .findFirst()
                        .ifPresent(v -> supported.put(upgrade, v)));
        Stream.of(definitions)
                .forEach(def -> supported.forEach((k, v) -> k.registerItem(def, v)));
    }

    private IItemDefinition registerItem(IItemDefinition item) {
        this.itemIdList.put(item.identifier(), item);
        return item;
    }

    @Override
    public Optional<IItemDefinition> getById(String id) {
        return Optional.ofNullable(this.itemIdList.getOrDefault(id, null));
    }

}