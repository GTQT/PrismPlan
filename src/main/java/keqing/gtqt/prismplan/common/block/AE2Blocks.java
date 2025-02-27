package keqing.gtqt.prismplan.common.block;

import appeng.api.definitions.ITileDefinition;
import appeng.block.crafting.ItemCraftingStorage;
import appeng.bootstrap.definitions.TileEntityDefinition;
import co.neeve.nae2.common.features.Features;
import co.neeve.nae2.common.features.subfeatures.DenseCellFeatures;
import keqing.gtqt.prismplan.common.block.ae2.BlockExtremeCraftingUnit;
import keqing.gtqt.prismplan.common.register.registry.AE2Registry;
import keqing.gtqt.prismplan.common.register.registry.rendering.ExtremeCraftingCubeRendering;
import keqing.gtqt.prismplan.common.tile.TileExtremeCraftingUnit;

import static keqing.gtqt.prismplan.common.block.ae2.BlockExtremeCraftingUnit.ExtremeCraftingUnitType.*;

public class AE2Blocks {
    // Please pre-init your block here like `private final ITileDefinition blockName;`.
    public static ITileDefinition storageCrafting65536x;
    public static ITileDefinition storageCrafting262144x;
    public static ITileDefinition storageCrafting1048576x;

    public static ITileDefinition coprocessor256x;
    public static ITileDefinition coprocessor1024x;
    public static ITileDefinition coprocessor4096x;


    public AE2Blocks(AE2Registry registry) {

        // Please register your block here, just use `addBlock()` method in AE2Registry.
        storageCrafting65536x = registry.addBlock("storage_crafting_65536k", () -> new BlockExtremeCraftingUnit(STORAGE_65536K))
                .tileEntity(new TileEntityDefinition(TileExtremeCraftingUnit.class, "crafting_storage"))
                .rendering(new ExtremeCraftingCubeRendering("storage_crafting_65536k", STORAGE_65536K))
                .useCustomItemModel()
                .item(ItemCraftingStorage::new)
                .features(DenseCellFeatures.DENSE_CPU_STORAGE_UNITS).build();

        storageCrafting262144x = registry.addBlock("storage_crafting_262144k", () -> new BlockExtremeCraftingUnit(STORAGE_262144K))
                .tileEntity(new TileEntityDefinition(TileExtremeCraftingUnit.class, "crafting_storage"))
                .rendering(new ExtremeCraftingCubeRendering("storage_crafting_262144k", STORAGE_262144K))
                .useCustomItemModel()
                .item(ItemCraftingStorage::new)
                .features(DenseCellFeatures.DENSE_CPU_STORAGE_UNITS).build();

        storageCrafting1048576x = registry.addBlock("storage_crafting_1048576k", () -> new BlockExtremeCraftingUnit(STORAGE_1048576K))
                .tileEntity(new TileEntityDefinition(TileExtremeCraftingUnit.class, "crafting_storage"))
                .rendering(new ExtremeCraftingCubeRendering("storage_crafting_1048576k", STORAGE_1048576K))
                .useCustomItemModel()
                .item(ItemCraftingStorage::new)
                .features(DenseCellFeatures.DENSE_CPU_STORAGE_UNITS).build();

        coprocessor256x = registry.addBlock("coprocessor_256x", () -> new BlockExtremeCraftingUnit(COPROCESSOR_256X))
                .tileEntity(new TileEntityDefinition(TileExtremeCraftingUnit.class, "crafting_storage"))
                .rendering(new ExtremeCraftingCubeRendering("coprocessor_256x", COPROCESSOR_256X))
                .useCustomItemModel()
                .features(Features.DENSE_CPU_COPROCESSORS).build();

        coprocessor1024x = registry.addBlock("coprocessor_1024x", () -> new BlockExtremeCraftingUnit(COPROCESSOR_1024X))
                .tileEntity(new TileEntityDefinition(TileExtremeCraftingUnit.class, "crafting_storage"))
                .rendering(new ExtremeCraftingCubeRendering("coprocessor_1024x", COPROCESSOR_1024X))
                .useCustomItemModel()
                .features(Features.DENSE_CPU_COPROCESSORS).build();

        coprocessor4096x = registry.addBlock("coprocessor_4096x", () -> new BlockExtremeCraftingUnit(COPROCESSOR_4096X))
                .tileEntity(new TileEntityDefinition(TileExtremeCraftingUnit.class, "crafting_storage"))
                .rendering(new ExtremeCraftingCubeRendering("coprocessor_4096x", COPROCESSOR_4096X))
                .useCustomItemModel()
                .features(Features.DENSE_CPU_COPROCESSORS).build();
    }
}
