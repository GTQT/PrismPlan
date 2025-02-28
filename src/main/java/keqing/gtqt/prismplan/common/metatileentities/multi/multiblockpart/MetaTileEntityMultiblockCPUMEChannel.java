package keqing.gtqt.prismplan.common.metatileentities.multi.multiblockpart;

import appeng.api.networking.GridFlags;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkCraftingCpuChange;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.me.GridAccessException;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.BaseActionSource;
import appeng.me.helpers.IGridProxyable;
import appeng.me.helpers.MachineSource;
import gregtech.api.GTValues;
import gregtech.api.capability.IControllable;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.common.ConfigHolder;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import static gregtech.api.capability.GregtechDataCodes.UPDATE_ONLINE_STATUS;

public class MetaTileEntityMultiblockCPUMEChannel extends MetaTileEntityMultiblockPart
                                                  implements IControllable{

//todo: online状态相关的补全, 以及正确的时候重新计数cpu（也许应该在虚拟cpu创建是postevent）
    private AENetworkProxy aeProxy;
//    private int meUpdateTick;
    protected boolean isOnline;
    private boolean allowExtraConnections;
    protected boolean meStatusChanged = false;
    private boolean wasActive = false;
    private boolean onRemoval = false;
    private final List<CraftingCPUCluster> cpuClusters = new ArrayList<CraftingCPUCluster>();

    public MetaTileEntityMultiblockCPUMEChannel(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GTValues.EV);
//        this.meUpdateTick = 0;
        this.allowExtraConnections = false;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityMultiblockCPUMEChannel(metaTileEntityId);
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    @Override
    public void update() {
        super.update();
//        if (!this.getWorld().isRemote || !this.aeProxy.isActive() || !this.aeProxy.isPowered()) {
//            postCPUClusterChangeEvent();
//        }
        if(isFirstTick()){
            postCPUClusterChangeEvent();
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        if (this.aeProxy != null) {
            buf.writeBoolean(true);
            NBTTagCompound proxy = new NBTTagCompound();
            this.aeProxy.writeToNBT(proxy);
            buf.writeCompoundTag(proxy);
        } else {
            buf.writeBoolean(false);
        }
//        buf.writeInt(this.meUpdateTick);
        buf.writeBoolean(this.isOnline);
        buf.writeBoolean(this.allowExtraConnections);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        if (buf.readBoolean()) {
            NBTTagCompound nbtTagCompound;
            try {
                nbtTagCompound = buf.readCompoundTag();
            } catch (IOException ignored) {
                nbtTagCompound = null;
            }

            if (this.aeProxy != null && nbtTagCompound != null) {
                this.aeProxy.readFromNBT(nbtTagCompound);
            }
        }
//        this.meUpdateTick = buf.readInt();
        this.isOnline = buf.readBoolean();
        this.allowExtraConnections = buf.readBoolean();
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == UPDATE_ONLINE_STATUS) {
            boolean isOnline = buf.readBoolean();
            if (this.isOnline != isOnline) {
                this.isOnline = isOnline;
                scheduleRenderUpdate();
            }
        }
    }


    @Nonnull
    @Override
    public AECableType getCableConnectionType(@Nonnull AEPartLocation part) {
        if (part.getFacing() != this.frontFacing && !this.allowExtraConnections) {
            return AECableType.NONE;
        }
        return AECableType.SMART;
    }

    @Nullable
    @Override
    public AENetworkProxy getProxy() {
        if (this.aeProxy == null) {
            return this.aeProxy = this.createProxy();
        }
        if (!this.aeProxy.isReady() && this.getWorld() != null) {
            this.aeProxy.onReady();
        }
        return this.aeProxy;
    }

    @Override
    public void setFrontFacing(EnumFacing frontFacing) {
        super.setFrontFacing(frontFacing);
        updateConnectableSides();
    }


    @Override
    public void gridChanged() {
    }

    /**
     * Get the me network connection status, updating it if on serverside.
     *
     * @return the updated status.
     */
//    public boolean updateMEStatus() {
//        if (!getWorld().isRemote) {
//            boolean isOnline = this.aeProxy != null && this.aeProxy.isActive() && this.aeProxy.isPowered();
//            if (this.isOnline != isOnline) {
//                writeCustomData(UPDATE_ONLINE_STATUS, buf -> buf.writeBoolean(isOnline));
//                this.isOnline = isOnline;
//                this.meStatusChanged = true;
//            } else {
//                this.meStatusChanged = false;
//            }
//        }
//        return this.isOnline;
//    }

//    protected boolean shouldSyncME() {
//        return this.meUpdateTick % ConfigHolder.compat.ae2.updateIntervals == 0;
//    }

    @Override
    public void onRemoval() {
        super.onRemoval();
        postCPUClusterChangeEvent();
    }

    @Override
    public void onPlacement() {
        super.onPlacement();
        postCPUClusterChangeEvent();
    }

    protected IActionSource getActionSource() {
        if (this.getHolder() instanceof IActionHost holder) {
            return new MachineSource(holder);
        }
        return new BaseActionSource();
    }

    @Nullable
    private AENetworkProxy createProxy() {
        if (this.getHolder() instanceof IGridProxyable holder) {
            AENetworkProxy proxy = new AENetworkProxy(holder, "mte_proxy", this.getStackForm(), true);
            proxy.setFlags(GridFlags.REQUIRE_CHANNEL);
            proxy.setIdlePowerUsage(ConfigHolder.compat.ae2.meHatchEnergyUsage);
            proxy.setValidSides(getConnectableSides());
            return proxy;
        }
        return null;
    }

    public EnumSet<EnumFacing> getConnectableSides() {
        return this.allowExtraConnections ? EnumSet.allOf(EnumFacing.class) : EnumSet.of(getFrontFacing());
    }

    public void updateConnectableSides() {
        if (this.aeProxy != null) {
            this.aeProxy.setValidSides(getConnectableSides());
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("AllowExtraConnections", this.allowExtraConnections);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.allowExtraConnections = data.getBoolean("AllowExtraConnections");
    }

    @Override
    public boolean isWorkingEnabled() {
        return false;
    }

    @Override
    public void setWorkingEnabled(boolean b) {

    }

    // cluster
    //todo: finish controller
//    public List<CraftingCPUCluster> getCPUs() {
//        MultiblockCPUController controller = this.getController();
//        if (!onRemoval) {
//            System.out.println(123123);
//            controller.getCpus();
//        }
//        return Collections.emptyList();
//    }

    public List<CraftingCPUCluster> getCPUs() {
//        WorldCoord pos = new WorldCoord(this.getPos());
//        if (this.cpuClusters.isEmpty() && this.getPos()!=null) {
//            CraftingCPUCluster ccc = new CraftingCPUCluster(pos,pos);
//            this.cpuClusters.add(new CraftingCPUCluster(pos,pos));
//        }
//        return this.cpuClusters;
        System.out.println(123123);
        //todo
        if (!onRemoval) {
            System.out.println(123123);
        }
        return Collections.emptyList();

    }

    @MENetworkEventSubscribe
    public void stateChange(final MENetworkPowerStatusChange c) {
        final boolean currentActive = this.getProxy().isActive();
        if (this.wasActive != currentActive) {
            this.wasActive = currentActive;
            postCPUClusterChangeEvent();
        }
    }

    @MENetworkEventSubscribe
    public void stateChange(final MENetworkChannelsChanged c) {
        final boolean currentActive = this.getProxy().isActive();
        if (this.wasActive != currentActive) {
            this.wasActive = currentActive;
            postCPUClusterChangeEvent();
        }
    }

    protected void postCPUClusterChangeEvent() {
        if (this.getProxy().isActive()) {
            try {
                this.getProxy().getGrid().postEvent(new MENetworkCraftingCpuChange(this.getProxy().getNode()));
            } catch (final GridAccessException ignored) {
            }
        }
    }


}
