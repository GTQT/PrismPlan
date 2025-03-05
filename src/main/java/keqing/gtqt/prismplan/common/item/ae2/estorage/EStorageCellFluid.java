package keqing.gtqt.prismplan.common.item.ae2.estorage;

import appeng.api.AEApi;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.fluids.helper.FluidCellConfig;
import keqing.gtqt.prismplan.Tags;
import keqing.gtqt.prismplan.api.capability.DriveStorageLevel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public class EStorageCellFluid extends EStorageCell<IAEFluidStack> {

    public static final EStorageCellFluid LEVEL_A = new EStorageCellFluid(DriveStorageLevel.A, 16, 4);
    public static final EStorageCellFluid LEVEL_B = new EStorageCellFluid(DriveStorageLevel.B, 64, 16);
    public static final EStorageCellFluid LEVEL_C = new EStorageCellFluid(DriveStorageLevel.C, 256, 64);

    public EStorageCellFluid(DriveStorageLevel level, final int millionBytes, final int byteMultiplier) {
        super(level, millionBytes, byteMultiplier);
        setRegistryName(new ResourceLocation(Tags.MOD_ID, "estorage_cell_fluid_" + millionBytes + "m"));
        setTranslationKey(Tags.MOD_ID + '.' + "estorage_cell_fluid_" + millionBytes + "m");
    }

    @Override
    public int getTotalTypes(@Nonnull final ItemStack cellItem) {
        return 25;
    }

    @Override
    public int getBytesPerType(@Nonnull final ItemStack cellItem) {
        return byteMultiplier * 1024;
    }

    @Override
    public IItemHandler getConfigInventory(final ItemStack is) {
        return new FluidCellConfig(is);
    }

    @Nonnull
    @Override
    public IStorageChannel<IAEFluidStack> getChannel() {
        return AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class);
    }
}
