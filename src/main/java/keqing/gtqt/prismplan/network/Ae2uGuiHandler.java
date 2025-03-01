package keqing.gtqt.prismplan.network;

import appeng.api.AEApi;
import appeng.api.config.SecurityPermissions;
import appeng.api.exceptions.AppEngException;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.implementations.guiobjects.IGuiItem;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.GuiNull;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerNull;
import appeng.container.ContainerOpenContext;
import appeng.container.implementations.ContainerDrive;
import appeng.core.sync.GuiHostType;
import appeng.core.sync.GuiWrapper;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.tile.storage.TileDrive;
import appeng.util.Platform;
import keqing.gtqt.prismplan.client.gui.container.ContainerPPDrive;
import keqing.gtqt.prismplan.common.metatileentities.multi.multiblock.prismstore.TilePPDrive;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;

import static appeng.core.sync.GuiHostType.ITEM;

public enum Ae2uGuiHandler implements IGuiHandler {
    
    GUI_PP_DRIVE(ContainerPPDrive.class, TilePPDrive.class, GuiHostType.WORLD, SecurityPermissions.BUILD);

    private final Class tileClass;
    private final Class containerClass;
    private Class guiClass;
    private GuiHostType type;
    private SecurityPermissions requiredPermission;
    private GuiWrapper.IExternalGui externalGui = null;

    private Ae2uGuiHandler() {
        this.tileClass = null;
        this.guiClass = null;
        this.containerClass = null;
    }

    private Ae2uGuiHandler(GuiWrapper.IExternalGui obj) {
        this.tileClass = null;
        this.containerClass = null;
        this.externalGui = obj;
    }

    public GuiWrapper.IExternalGui getExternalGui() {
        return this.externalGui;
    }

    private Ae2uGuiHandler(Class containerClass, SecurityPermissions requiredPermission) {
        this.requiredPermission = requiredPermission;
        this.containerClass = containerClass;
        this.tileClass = null;
        this.getGui();
    }

    private void getGui() {
        if (Platform.isClient()) {
            AEBaseGui.class.getName();
            String start = this.containerClass.getName();
            String guiClass = start.replaceFirst("container.", "client.gui.").replace(".Container", ".Gui");
            if (start.equals(guiClass)) {
                throw new IllegalStateException("Unable to find gui class");
            }

            this.guiClass = ReflectionHelper.getClass(this.getClass().getClassLoader(), new String[]{guiClass});
            if (this.guiClass == null) {
                throw new IllegalStateException("Cannot Load class: " + guiClass);
            }
        }

    }

    private Ae2uGuiHandler(Class containerClass, Class tileClass, GuiHostType type, SecurityPermissions requiredPermission) {
        this.requiredPermission = requiredPermission;
        this.containerClass = containerClass;
        this.type = type;
        this.tileClass = tileClass;
        this.getGui();
    }

    public Object getServerGuiElement(int ordinal, EntityPlayer player, World w, int x, int y, int z) {
        AEPartLocation side = AEPartLocation.fromOrdinal(ordinal & 7);
        Ae2uGuiHandler ID = values()[ordinal >> 4];
        boolean usingItemOnTile = (ordinal >> 3 & 1) == 1;
        if (ID.type.isItem()) {
            ItemStack it = ItemStack.EMPTY;
            if (usingItemOnTile) {
                it = player.inventory.getCurrentItem();
            } else if (y == 0) {
                if (x >= 0 && x < player.inventory.mainInventory.size()) {
                    it = player.inventory.getStackInSlot(x);
                }
            } else if (y == 1 && z == Integer.MIN_VALUE) {
                //it = BaublesApi.getBaublesHandler(player).getStackInSlot(x);
            }

            Object myItem = this.getGuiObject(it, player, w, x, y, z);
            if (myItem != null && ID.CorrectTileOrPart(myItem)) {
                return this.updateGui(ID.ConstructContainer(player.inventory, side, myItem), w, x, y, z, side, myItem);
            }
        }

        if (ID.type != ITEM) {
            TileEntity TE = w.getTileEntity(new BlockPos(x, y, z));
            if (TE instanceof IPartHost) {
                ((IPartHost)TE).getPart(side);
                IPart part = ((IPartHost)TE).getPart(side);
                if (ID.CorrectTileOrPart(part)) {
                    return this.updateGui(ID.ConstructContainer(player.inventory, side, part), w, x, y, z, side, part);
                }
            } else if (ID.CorrectTileOrPart(TE)) {
                return this.updateGui(ID.ConstructContainer(player.inventory, side, TE), w, x, y, z, side, TE);
            }
        }

        return new ContainerNull();
    }

    private Object getGuiObject(ItemStack it, EntityPlayer player, World w, int x, int y, int z) {
        if (!it.isEmpty()) {
            if (it.getItem() instanceof IGuiItem) {
                return ((IGuiItem)it.getItem()).getGuiObject(it, w, new BlockPos(x, y, z));
            }

            IWirelessTermHandler wh = AEApi.instance().registries().wireless().getWirelessTerminalHandler(it);
            if (wh != null) {
                return new WirelessTerminalGuiObject(wh, it, player, w, x, y, z);
            }
        }

        return null;
    }

    public boolean CorrectTileOrPart(Object tE) {
        if (this.tileClass == null) {
            throw new IllegalArgumentException("This Gui Cannot use the standard Handler.");
        } else {
            return this.tileClass.isInstance(tE);
        }
    }

    private Object updateGui(Object newContainer, World w, int x, int y, int z, AEPartLocation side, Object myItem) {
        if (newContainer instanceof AEBaseContainer) {
            AEBaseContainer bc = (AEBaseContainer)newContainer;
            bc.setOpenContext(new ContainerOpenContext(myItem));
            bc.getOpenContext().setWorld(w);
            bc.getOpenContext().setX(x);
            bc.getOpenContext().setY(y);
            bc.getOpenContext().setZ(z);
            bc.getOpenContext().setSide(side);
        }

        return newContainer;
    }

    public Object ConstructContainer(InventoryPlayer inventory, AEPartLocation side, Object tE) {
        try {
            Constructor[] c = this.containerClass.getConstructors();
            if (c.length == 0) {
                throw new AppEngException("Invalid Gui Class");
            } else {
                Constructor target = this.findConstructor(c, inventory, tE);
                if (target == null) {
                    throw new IllegalStateException("Cannot find " + this.containerClass.getName() + "( " + this.typeName(inventory) + ", " + this.typeName(tE) + " )");
                } else {
                    return target.newInstance(inventory, tE);
                }
            }
        } catch (Throwable var6) {
            Throwable t = var6;
            throw new IllegalStateException(t);
        }
    }

    private Constructor findConstructor(Constructor[] c, InventoryPlayer inventory, Object tE) {
        Constructor[] var4 = c;
        int var5 = c.length;

        for(int var6 = 0; var6 < var5; ++var6) {
            Constructor con = var4[var6];
            Class[] types = con.getParameterTypes();
            if (types.length == 2 && types[0].isAssignableFrom(inventory.getClass()) && types[1].isAssignableFrom(tE.getClass())) {
                return con;
            }
        }

        return null;
    }

    private String typeName(Object inventory) {
        return inventory == null ? "NULL" : inventory.getClass().getName();
    }

    public Object getClientGuiElement(int ordinal, EntityPlayer player, World w, int x, int y, int z) {
        AEPartLocation side = AEPartLocation.fromOrdinal(ordinal & 7);
        Ae2uGuiHandler ID = values()[ordinal >> 4];
        boolean usingItemOnTile = (ordinal >> 3 & 1) == 1;
        if (ID.type.isItem()) {
            ItemStack it = ItemStack.EMPTY;
            if (usingItemOnTile) {
                it = player.inventory.getCurrentItem();
            } else if (y == 0 && x >= 0 && x < player.inventory.mainInventory.size()) {
                it = player.inventory.getStackInSlot(x);
            }

            if (y == 1 && z == Integer.MIN_VALUE) {
                //it = BaublesApi.getBaublesHandler(player).getStackInSlot(x);
            }

            Object myItem = this.getGuiObject(it, player, w, x, y, z);
            if (myItem != null && ID.CorrectTileOrPart(myItem)) {
                return ID.ConstructGui(player.inventory, side, myItem);
            }
        }

        if (ID.type != ITEM) {
            TileEntity TE = w.getTileEntity(new BlockPos(x, y, z));
            if (TE instanceof IPartHost) {
                ((IPartHost)TE).getPart(side);
                IPart part = ((IPartHost)TE).getPart(side);
                if (ID.CorrectTileOrPart(part)) {
                    return ID.ConstructGui(player.inventory, side, part);
                }
            } else if (ID.CorrectTileOrPart(TE)) {
                return ID.ConstructGui(player.inventory, side, TE);
            }
        }

        return new GuiNull(new ContainerNull());
    }

    public Object ConstructGui(InventoryPlayer inventory, AEPartLocation side, Object tE) {
        try {
            Constructor[] c = this.guiClass.getConstructors();
            if (c.length == 0) {
                throw new AppEngException("Invalid Gui Class");
            } else {
                Constructor target = this.findConstructor(c, inventory, tE);
                if (target == null) {
                    throw new IllegalStateException("Cannot find " + this.containerClass.getName() + "( " + this.typeName(inventory) + ", " + this.typeName(tE) + " )");
                } else {
                    return target.newInstance(inventory, tE);
                }
            }
        } catch (Throwable var6) {
            Throwable t = var6;
            throw new IllegalStateException(t);
        }
    }

    public boolean hasPermissions(TileEntity te, int x, int y, int z, AEPartLocation side, EntityPlayer player) {
        World w = player.getEntityWorld();
        BlockPos pos = new BlockPos(x, y, z);
        if (Platform.hasPermissions(te != null ? new DimensionalCoord(te) : new DimensionalCoord(player.world, pos), player)) {
            if (this.type.isItem()) {
                ItemStack it = player.inventory.getCurrentItem();
                if (!it.isEmpty() && it.getItem() instanceof IGuiItem) {
                    Object myItem = ((IGuiItem)it.getItem()).getGuiObject(it, w, pos);
                    if (this.CorrectTileOrPart(myItem)) {
                        return true;
                    }
                }
            }

            if (this.type != ITEM) {
                TileEntity TE = w.getTileEntity(pos);
                if (TE instanceof IPartHost) {
                    ((IPartHost)TE).getPart(side);
                    IPart part = ((IPartHost)TE).getPart(side);
                    if (this.CorrectTileOrPart(part)) {
                        return this.securityCheck(part, player);
                    }
                } else if (this.CorrectTileOrPart(TE)) {
                    return this.securityCheck(TE, player);
                }
            }
        }

        return false;
    }

    private boolean securityCheck(Object te, EntityPlayer player) {
        if (te instanceof IActionHost && this.requiredPermission != null) {
            IGridNode gn = ((IActionHost)te).getActionableNode();
            if (gn != null) {
                IGrid g = gn.getGrid();
                if (g != null) {
                    boolean requirePower = false;
                    ISecurityGrid sg = (ISecurityGrid)g.getCache(ISecurityGrid.class);
                    return sg.hasPermission(player, this.requiredPermission);
                }
            }

            return false;
        } else {
            return true;
        }
    }

    public GuiHostType getType() {
        return this.type;
    }
}
