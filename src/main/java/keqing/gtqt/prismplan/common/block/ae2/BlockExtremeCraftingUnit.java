package keqing.gtqt.prismplan.common.block.ae2;

import appeng.api.definitions.ITileDefinition;
import appeng.block.crafting.BlockCraftingUnit;
import keqing.gtqt.prismplan.api.utils.AE2Values;
import keqing.gtqt.prismplan.common.block.AE2Blocks;
public class BlockExtremeCraftingUnit extends BlockCraftingUnit {

    public final ExtremeCraftingUnitType type;

    public BlockExtremeCraftingUnit(ExtremeCraftingUnitType type) {
        super(null);
        this.type = type;
    }

    public enum ExtremeCraftingUnitType {
        STORAGE_65536K(65536 * AE2Values.KILO_SCALAR, 0) {
            @Override
            public ITileDefinition getBlock() {
                return AE2Blocks.storageCrafting65536x;
            }
        },
        STORAGE_262144K(262144 * AE2Values.KILO_SCALAR, 0) {
            @Override
            public ITileDefinition getBlock() {
                return AE2Blocks.storageCrafting262144x;
            }
        },
        STORAGE_1048576K(1048576 * AE2Values.KILO_SCALAR, 0) {
            @Override
            public ITileDefinition getBlock() {
                return AE2Blocks.storageCrafting1048576x;
            }
        },
        COPROCESSOR_256X(0, 256) {
            @Override
            public ITileDefinition getBlock() {
                return AE2Blocks.coprocessor256x;
            }
        },
        COPROCESSOR_1024X(0, 1024) {
            @Override
            public ITileDefinition getBlock() {
                return AE2Blocks.coprocessor1024x;
            }
        },
        COPROCESSOR_4096X(0, 4096) {
            @Override
            public ITileDefinition getBlock() {
                return AE2Blocks.coprocessor4096x;
            }
        };

        public final int bytes;

        public final int accelFactor;

        ExtremeCraftingUnitType(int bytes, int accelFactor) {
            this.bytes = bytes;
            this.accelFactor = accelFactor;
        }

        public abstract ITileDefinition getBlock();
    }
}


