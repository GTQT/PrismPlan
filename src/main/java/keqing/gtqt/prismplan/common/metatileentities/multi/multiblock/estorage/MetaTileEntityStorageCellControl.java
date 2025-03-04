package keqing.gtqt.prismplan.common.metatileentities.multi.multiblock.estorage;

import appeng.api.storage.ICellInventory;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.data.IAEItemStack;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import keqing.gtqt.prismplan.api.capability.ICellHatch;
import keqing.gtqt.prismplan.api.capability.INetWorkProxy;
import keqing.gtqt.prismplan.api.capability.INetWorkStore;
import keqing.gtqt.prismplan.api.multiblock.PrismPlanMultiblockAbility;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;

import java.util.List;

import static gregtech.api.util.RelativeDirection.*;

public class MetaTileEntityStorageCellControl extends MultiblockWithDisplayBase {

    public MetaTileEntityStorageCellControl(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }
    protected double idleDrain = 64;
    @Override
    protected void updateFormedValid() {
        if(isStructureFormed())
        {
            //getCellDrives().forEach(MetaTileEntityStorageCellHatch::updateWriteState);
        }
    }

    public void recalculateEnergyUsage() {
        double newIdleDrain = 64;
        for (final ICellHatch drive : getCellDrives()) {
            ECellDriveWatcher<IAEItemStack> watcher = drive.getWatcher();
            if (watcher == null) {
                continue;
            }
            ICellInventoryHandler<?> cellInventory = (ICellInventoryHandler<?>) watcher.getInternal();
            if (cellInventory == null) {
                continue;
            }
            ICellInventory<?> cellInv = cellInventory.getCellInv();
            if (cellInv == null) {
                continue;
            }
            newIdleDrain += cellInv.getIdleDrain();
        }
        this.idleDrain = newIdleDrain;
        if (this.getNetWorkStoreHatch().getProxy() != null) {
            this.getNetWorkStoreHatch().getProxy().setIdlePowerUsage(idleDrain);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    //仓口
        public INetWorkStore getNetWorkStoreHatch() {
            List<INetWorkStore> abilities = getAbilities(PrismPlanMultiblockAbility.NETWORK_STORE);
            if (abilities.isEmpty())
                return null;
            return abilities.get(0);
        }

    public ICellHatch getCellHatch() {
        List<ICellHatch> abilities = getAbilities(PrismPlanMultiblockAbility.CELL_HATCH);
        if (abilities.isEmpty())
            return null;
        return abilities.get(0);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    //多方块
    protected BlockPattern createStructurePattern() {
        FactoryBlockPattern pattern = FactoryBlockPattern.start(FRONT, UP, RIGHT)
                .aisle("XX", "XX", "XX")
                .aisle("TX", "TX", "TX").setRepeatable(1,16)
                .aisle("XX", "XX", "XX")
                .aisle("NX", "SX", "XX")
                .where('S', this.selfPredicate())
                .where('N', abilities(PrismPlanMultiblockAbility.NETWORK_STORE))
                .where('T', abilities(PrismPlanMultiblockAbility.CELL_HATCH))
                .where('X', states(this.getCasingState()));
        return pattern.build();
    }

    private IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @Override
    public boolean hasMaintenanceMechanics() {
        return false;
    }

    @Override
    public boolean hasMufflerMechanics() {
        return false;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityStorageCellControl(metaTileEntityId);
    }

    public List<ICellHatch> getCellDrives() {
        return getAbilities(PrismPlanMultiblockAbility.CELL_HATCH);
    }
}
