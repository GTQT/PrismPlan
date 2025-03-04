package keqing.gtqt.prismplan.mixin;

import appeng.api.networking.IGridNode;
import appeng.api.storage.*;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import keqing.gtqt.prismplan.common.metatileentities.multi.multiblock.estorage.MetaTileEntityNetWorkStoreHatch;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@Mixin(value = MetaTileEntityHolder.class, remap = false)
public abstract class MixinMetaTileEntityHolder implements ICellContainer {

    @Shadow private MetaTileEntity metaTileEntity;

    @Unique
    @Override
    public void blinkCell(int i) {
        return;
    }

    @Nonnull
    @Unique
    @Override
    public IGridNode getActionableNode() {
        return this.metaTileEntity.getProxy().getNode();
    }

    @Unique
    @Override
    public List<IMEInventoryHandler> getCellArray(IStorageChannel<?> iStorageChannel) {
        if (this.metaTileEntity instanceof MetaTileEntityNetWorkStoreHatch) {
            return ((MetaTileEntityNetWorkStoreHatch)this.metaTileEntity).getCellArray(iStorageChannel);
        }
        return null;
    }

    @Unique
    @Override
    public int getPriority() {
        return 0;
    }

    @Unique
    @Override
    public void saveChanges(@Nullable ICellInventory<?> iCellInventory) {
        return ;

    }
}
