package keqing.gtqt.prismplan.client.gui.container;

import appeng.api.parts.IPart;
import appeng.container.AEBaseContainer;
import appeng.container.slot.SlotRestrictedInput;
import appeng.tile.storage.TileDrive;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerPPDrive extends AEBaseContainer {
    public ContainerPPDrive(InventoryPlayer ip, TileDrive drive) {
        super(ip, drive, (IPart)null);

        for(int y = 0; y < 5; ++y) {
            for(int x = 0; x < 3; ++x) {
                this.addSlotToContainer(new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.STORAGE_CELLS, drive.getInternalInventory(), x + y * 2, 71 + x * 18, 14 + y * 18, this.getInventoryPlayer()));
            }
        }

        this.bindPlayerInventory(ip, 0, 117);
    }
}

