package keqing.gtqt.prismplan.common.tile;

import appeng.tile.crafting.TileCraftingStorageTile;
import co.neeve.nae2.common.interfaces.IDenseCoProcessor;
import keqing.gtqt.prismplan.common.block.ae2.BlockExtremeCraftingUnit;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

public class TileExtremeCraftingUnit extends TileCraftingStorageTile implements IDenseCoProcessor {

    @Override
    protected ItemStack getItemFromTile(Object obj) {
        BlockExtremeCraftingUnit unit = getBlock();
        return unit != null ? unit.type.getBlock().maybeStack(1).orElse(ItemStack.EMPTY) : ItemStack.EMPTY;
    }

    @Override
    public boolean isAccelerator() {
        return false;
    }

    @Override
    public boolean isStorage() {
        BlockExtremeCraftingUnit unit = getBlock();
        return unit != null && unit.type.bytes > 0;
    }

    @Override
    public int getStorageBytes() {
        BlockExtremeCraftingUnit unit = getBlock();
        return unit != null ? unit.type.bytes : 0;
    }

    @Override
    public int getAccelerationFactor() {
        BlockExtremeCraftingUnit unit = getBlock();
        return unit != null ? unit.type.accelFactor : 0;
    }


    public BlockExtremeCraftingUnit getBlock() {
        if (world != null && !notLoaded() && !isInvalid()) {
            Block block = getWorld().getBlockState(getPos()).getBlock();
            if (block instanceof BlockExtremeCraftingUnit unit) {
                return unit;
            }
        }
        return null;
    }
}
