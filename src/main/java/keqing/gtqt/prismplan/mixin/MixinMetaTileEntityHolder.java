package keqing.gtqt.prismplan.mixin;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IAEPowerStorage;
import appeng.api.storage.*;
import appeng.util.inv.IAEAppEngInventory;
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
public abstract class MixinMetaTileEntityHolder implements ICellContainer, IAEPowerStorage {

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
    public double injectAEPower(final double amt, @Nonnull final Actionable mode) {
        if (this.metaTileEntity instanceof MetaTileEntityNetWorkStoreHatch) {
            return ((MetaTileEntityNetWorkStoreHatch)this.metaTileEntity).injectAEPower(amt, mode);
        }
        return 0;
    }
    @Unique
    @Override
    public double extractAEPower(final double amt, @Nonnull final Actionable mode, @Nonnull final PowerMultiplier multiplier) {
        if (this.metaTileEntity instanceof MetaTileEntityNetWorkStoreHatch) {
            return ((MetaTileEntityNetWorkStoreHatch)this.metaTileEntity).extractAEPower(amt, mode,multiplier);
        }
        return 0;
    }
    @Unique
    @Override
    public double getAEMaxPower() {
        if (this.metaTileEntity instanceof MetaTileEntityNetWorkStoreHatch) {
            return ((MetaTileEntityNetWorkStoreHatch)this.metaTileEntity).getAEMaxPower();
        }
        return 0;
    }
    @Unique
    @Override
    public double getAECurrentPower() {
        if (this.metaTileEntity instanceof MetaTileEntityNetWorkStoreHatch) {
            return ((MetaTileEntityNetWorkStoreHatch)this.metaTileEntity).getAECurrentPower();
        }
        return 0;
    }

    @Override
    public boolean isAEPublicPowerStorage() {
        if (this.metaTileEntity instanceof MetaTileEntityNetWorkStoreHatch) {
            return ((MetaTileEntityNetWorkStoreHatch)this.metaTileEntity).isAEPublicPowerStorage();
        }
        return false;
    }

    @Nonnull
    @Override
    public AccessRestriction getPowerFlow() {
        if (this.metaTileEntity instanceof MetaTileEntityNetWorkStoreHatch) {
            return ((MetaTileEntityNetWorkStoreHatch)this.metaTileEntity).getPowerFlow();
        }
        return AccessRestriction.NO_ACCESS;
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
