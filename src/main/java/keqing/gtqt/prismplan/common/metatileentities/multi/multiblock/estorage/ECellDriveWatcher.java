package keqing.gtqt.prismplan.common.metatileentities.multi.multiblock.estorage;


import appeng.api.config.Actionable;
import appeng.api.networking.events.MENetworkCellArrayUpdate;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.ICellHandler;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.core.features.registries.cell.CreativeCellHandler;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.storage.MEInventoryHandler;
import appeng.tile.storage.TileDrive;
import io.netty.util.internal.ThrowableUtil;
import keqing.gtqt.prismplan.api.utils.PrismPlanLog;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.List;

public class ECellDriveWatcher<T extends IAEStack<T>> extends MEInventoryHandler<T> {
    private int oldStatus = 0;
    protected final estoreTEST drive;
    private final ICellHandler handler;
    private final ItemStack is;


    public ECellDriveWatcher(final IMEInventory<T> i,  ItemStack is,ICellHandler han, final estoreTEST drive) {
        super(i, i.getChannel());
        this.drive = drive;
        this.handler = han;
        this.is = is;
    }

    public int getStatus() {
        return this.handler.getStatusForCell(this.is, (ICellInventoryHandler)this.getInternal());
    }


    /*
    @Override
    public T injectItems(final T input, final Actionable type, final IActionSource src) {
        final long size = input.getStackSize();
        final T remainder = super.injectItems(input, type, src);

        if (type == Actionable.MODULATE && (remainder == null || remainder.getStackSize() != size)) {

            AENetworkProxy proxy = this.drive.getProxy();
            if (proxy.isActive()) {
                try {
                    List<T> changed = Collections.singletonList(input.copy().setStackSize(input.getStackSize() - (remainder == null ? 0 : remainder.getStackSize())));
                    proxy.getStorage().postAlterationOfStoredItems(this.getChannel(), changed, this.drive.getSource());
                } catch (GridAccessException e) {
                    PrismPlanLog.logger.warn(ThrowableUtil.stackTraceToString(e));
                }
            }
           // this.drive.onWriting();
        }

        return remainder;
    }

    @Override
    public T extractItems(final T request, final Actionable type, final IActionSource src) {
        final T extractable = super.extractItems(request, type, src);

        if (type == Actionable.MODULATE && extractable != null) {

            AENetworkProxy proxy = this.drive.getProxy();
            if (proxy.isActive()) {
                try {
                    List<T> changed = Collections.singletonList(request.copy().setStackSize(-extractable.getStackSize()));
                    proxy.getStorage().postAlterationOfStoredItems(this.getChannel(), changed, this.drive.getSource());
                } catch (GridAccessException e) {
                    PrismPlanLog.logger.warn(ThrowableUtil.stackTraceToString(e));
                }
            }
           // this.drive.onWriting();
        }

        return extractable;
    }

    @Override
    public boolean isSticky() {
        if (this.getInternal() instanceof ICellInventoryHandler<?> cellInventoryHandler) {
            return cellInventoryHandler.isSticky();
        }

        return super.isSticky();
    }

     */
    public T injectItems(T input, Actionable type, IActionSource src) {
        long size = input.getStackSize();
        T remainder = super.injectItems(input, type, src);
        if (type == Actionable.MODULATE && (remainder == null || remainder.getStackSize() != size)) {
            int newStatus = this.getStatus();
            if (newStatus != this.oldStatus) {
                this.drive.blinkCell(this.getSlot());
                this.oldStatus = newStatus;
            }

            if (this.drive.getNetWorkProxyHatch().getProxy().isActive() && !(this.handler instanceof CreativeCellHandler)) {
                try {
                    this.drive.getNetWorkProxyHatch().getProxy().getStorage().postAlterationOfStoredItems(this.getChannel(), Collections.singletonList(input.copy().setStackSize(input.getStackSize() - (remainder == null ? 0L : remainder.getStackSize()))), drive.getNetWorkProxyHatch().getSource());
                } catch (GridAccessException var9) {
                    GridAccessException e = var9;
                    e.printStackTrace();
                }
            }
        }
        drive.saveChanges();
        try {
            drive.getNetWorkProxyHatch().getProxy().getGrid().postEvent(new MENetworkCellArrayUpdate());
        } catch (GridAccessException e) {
            PrismPlanLog.logger.warn(ThrowableUtil.stackTraceToString(e));
        }
        return remainder;
    }

    public T extractItems(T request, Actionable type, IActionSource src) {
        T extractable = super.extractItems(request, type, src);
        if (type == Actionable.MODULATE && extractable != null) {
            int newStatus = this.getStatus();
            if (newStatus != this.oldStatus) {
                this.drive.blinkCell(this.getSlot());
                this.oldStatus = newStatus;
            }

            if (this.drive.getNetWorkProxyHatch().getProxy().isActive() && !(this.handler instanceof CreativeCellHandler)) {
                try {
                    this.drive.getNetWorkProxyHatch().getProxy().getStorage().postAlterationOfStoredItems(this.getChannel(), Collections.singletonList(request.copy().setStackSize(-extractable.getStackSize())), drive.getNetWorkProxyHatch().getSource());
                } catch (GridAccessException var7) {
                    GridAccessException e = var7;
                    e.printStackTrace();
                }
            }
        }
        drive.saveChanges();
        try {
            drive.getNetWorkProxyHatch().getProxy().getGrid().postEvent(new MENetworkCellArrayUpdate());
        } catch (GridAccessException e) {
            PrismPlanLog.logger.warn(ThrowableUtil.stackTraceToString(e));
        }
        return extractable;
    }

    public boolean isSticky() {
        IMEInventory var2 = this.getInternal();
        if (var2 instanceof ICellInventoryHandler) {
            ICellInventoryHandler<?> cellInventoryHandler = (ICellInventoryHandler)var2;
            return cellInventoryHandler.isSticky();
        } else {
            return super.isSticky();
        }
    }
}
