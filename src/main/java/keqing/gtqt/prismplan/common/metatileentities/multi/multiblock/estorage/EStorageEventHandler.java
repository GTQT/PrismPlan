package keqing.gtqt.prismplan.common.metatileentities.multi.multiblock.estorage;

import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.ISecurityGrid;
import appeng.tile.inventory.AppEngCellInventory;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import keqing.gtqt.prismplan.api.capability.INetWorkProxy;
import keqing.gtqt.prismplan.api.capability.INetWorkStore;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EStorageEventHandler {

    public static final EStorageEventHandler INSTANCE = new EStorageEventHandler();

    private static boolean canInteract(final EntityPlayer player, final INetWorkProxy proxyable) {
        final IGridNode gn = proxyable.getProxy().getNode();
        if (gn != null) {
            final IGrid g = gn.getGrid();
            final IEnergyGrid eg = g.getCache(IEnergyGrid.class);
            if (!eg.isNetworkPowered()) {
                return true;
            }

            final ISecurityGrid sg = g.getCache(ISecurityGrid.class);
            return sg.hasPermission(player, SecurityPermissions.BUILD);
        }
        return true;
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        World world = event.getWorld();
        if (world.isRemote) {
            return;
        }

        EnumHand hand = event.getHand();
        if (hand != EnumHand.MAIN_HAND) {
            return;
        }

        EntityPlayer player = event.getEntityPlayer();
        if (!player.isSneaking()) {
            return;
        }

        TileEntity te = world.getTileEntity(event.getPos());

        if (te instanceof IGregTechTileEntity igtte) {
            MetaTileEntity mte = igtte.getMetaTileEntity();

            if (mte instanceof final MetaTileEntityStorageCellHatch drive) {
                MetaTileEntityStorageCellControl controller = (MetaTileEntityStorageCellControl) drive.getController();
                if (controller != null) {
                    INetWorkStore channel = controller.getNetWorkStoreHatch();
                    if (channel != null && !canInteract(player, channel)) {
                        player.sendMessage(new TextComponentTranslation("prismplan.estorage_cell_drive.player.no_permission"));
                        event.setCanceled(true);
                        return;
                    }
                }

                ItemStack stackInHand = player.getHeldItem(hand);

                AppEngCellInventory inv = drive.getDriveInv();
                ItemStack stackInSlot = inv.getStackInSlot(0);
                if (stackInSlot.isEmpty()) {
                    if (stackInHand.isEmpty() || EStorageCellHandler.getHandler(stackInHand) == null) {
                        return;
                    }
                    player.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, inv.insertItem(0, stackInHand.copy(), false));
                    player.sendMessage(new TextComponentTranslation("prismplan.estorage_cell_drive.player.inserted"));
                    event.setCanceled(true);
                    return;
                }

                if (!stackInHand.isEmpty()) {
                    return;
                }

                player.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, inv.extractItem(0, stackInSlot.getCount(), false));
                player.sendMessage(new TextComponentTranslation("prismplan.estorage_cell_drive.player.removed"));
                event.setCanceled(true);
            }

        }


    }
}
