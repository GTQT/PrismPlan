package keqing.gtqt.prismplan.api.tile;

import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.security.IActionSource;
import appeng.crafting.MECraftingInventory;

public interface ICustomCraftingCPU {
    // 修改参数类型为ICraftingCPU
    boolean customSetJob(MECraftingInventory inv, ICraftingCPU cpu, IActionSource src);
}
