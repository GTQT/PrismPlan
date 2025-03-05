package keqing.gtqt.prismplan.api.capability;


import appeng.api.storage.ICellInventory;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.data.IAEItemStack;
import appeng.tile.inventory.AppEngCellInventory;
import com.github.bsideup.jabel.Desugar;
import keqing.gtqt.prismplan.api.utils.PrismPlanLog;
import keqing.gtqt.prismplan.common.item.ae2.estorage.EStorageCell;
import keqing.gtqt.prismplan.common.metatileentities.multi.multiblock.estorage.ECellDriveWatcher;
import keqing.gtqt.prismplan.common.metatileentities.multi.multiblock.estorage.EStorageCellHandler;
import keqing.gtqt.prismplan.common.metatileentities.multi.multiblock.estorage.MetaTileEntityStorageCellHatch;
import net.minecraft.item.ItemStack;

@Desugar
public record EStorageCellData(DriveStorageType type, DriveStorageLevel level, int usedTypes, long usedBytes) {

    public static EStorageCellData from(final MetaTileEntityStorageCellHatch drive) {
        AppEngCellInventory driveInv = drive.getDriveInv();
        ItemStack stack = driveInv.getStackInSlot(0);
        if (stack.isEmpty()) {
            return null;
        }
        EStorageCellHandler handler = EStorageCellHandler.getHandler(stack);
        if (handler == null) {
            return null;
        }
        EStorageCell<?> cell = (EStorageCell<?>) stack.getItem();
        DriveStorageType type = MetaTileEntityStorageCellHatch.getCellType(cell);
        if (type == null) {
            return null;
        }
        DriveStorageLevel level = cell.getLevel();
        ECellDriveWatcher<IAEItemStack> watcher = drive.getWatcher();
        if (watcher == null) {
            return null;
        }
        ICellInventoryHandler<?> cellInventory = (ICellInventoryHandler<?>) watcher.getInternal();
        if (cellInventory == null) {
            return null;
        }
        ICellInventory<?> cellInv = cellInventory.getCellInv();
        if (cellInv == null) {
            PrismPlanLog.logger.warn("cellInv");
            return null;
        }
        long storedTypes = cellInv.getStoredItemTypes();
        long usedBytes = cellInv.getUsedBytes();
        return new EStorageCellData(type, level, (int) storedTypes, usedBytes);
    }

}
