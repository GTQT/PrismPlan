package keqing.gtqt.prismplan.common.network;


import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import io.netty.buffer.ByteBuf;
import keqing.gtqt.prismplan.api.utils.PrismPlanLog;
import keqing.gtqt.prismplan.common.metatileentities.multi.multiblock.estorage.MetaTileEntityStorageCellHatch;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PktCellDriveStatusUpdate implements IMessage, IMessageHandler<PktCellDriveStatusUpdate, IMessage> {

    private BlockPos pos = null;
    private boolean writing = false;

    public PktCellDriveStatusUpdate() {
    }

    public PktCellDriveStatusUpdate(final BlockPos pos, final boolean writing) {
        this.pos = pos;
        this.writing = writing;
    }

    @Override
    public void fromBytes(final ByteBuf buf) {
        try {
            pos = BlockPos.fromLong(buf.readLong());
            writing = buf.readBoolean();
        } catch (Exception e) {
            PrismPlanLog.logger.error("PktCellDriveStatusUpdate read failed.", e);
        }
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        buf.writeLong(pos.toLong());
        buf.writeBoolean(writing);
    }

    @Override
    public IMessage onMessage(final PktCellDriveStatusUpdate message, final MessageContext ctx) {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            processPacket(message);
        }
        return null;
    }

    @SideOnly(Side.CLIENT)
    protected static void processPacket(final PktCellDriveStatusUpdate message) {
        BlockPos pos = message.pos;
        boolean writing = message.writing;
        if (pos == null) {
            return;
        }

        WorldClient world = Minecraft.getMinecraft().world;
        if (world == null) {
            return;
        }
        TileEntity te = world.getTileEntity(pos);

        if (te instanceof IGregTechTileEntity igtte) {
            MetaTileEntity mte = igtte.getMetaTileEntity();
            if (mte instanceof MetaTileEntityStorageCellHatch hatch) {

                hatch.setWriting(writing);
                hatch.markDirty();
            }
        }
    }

}
