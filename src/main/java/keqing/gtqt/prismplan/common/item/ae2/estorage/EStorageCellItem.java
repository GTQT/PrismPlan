package keqing.gtqt.prismplan.common.item.ae2.estorage;


import appeng.api.AEApi;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import keqing.gtqt.prismplan.Tags;
import keqing.gtqt.prismplan.api.capability.DriveStorageLevel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class EStorageCellItem extends EStorageCell<IAEItemStack> {

    public static final EStorageCellItem LEVEL_A = new EStorageCellItem(DriveStorageLevel.A, 16, 4);
    public static final EStorageCellItem LEVEL_B = new EStorageCellItem(DriveStorageLevel.B, 64, 16);
    public static final EStorageCellItem LEVEL_C = new EStorageCellItem(DriveStorageLevel.C, 256, 64);

    public EStorageCellItem(DriveStorageLevel level, final int millionBytes, final int byteMultiplier) {
        super(level, millionBytes, byteMultiplier);
        setRegistryName(new ResourceLocation(Tags.MOD_ID, "estorage_cell_item_" + millionBytes + "m"));
        setTranslationKey(Tags.MOD_ID + '.' + "estorage_cell_item_" + millionBytes + "m");
    }

    @Override
    public int getTotalTypes(@Nonnull final ItemStack cellItem) {
        return 315;
    }

    @Override
    public int getBytesPerType(@Nonnull final ItemStack cellItem) {
        return byteMultiplier * 1024;
    }

    @Nonnull
    @Override
    public IStorageChannel<IAEItemStack> getChannel() {
        return AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
    }
}
