//package keqing.gtqt.prismplan.common.metatileentities.multi.multiblock;
//
//import appeng.api.networking.security.IActionHost;
//import appeng.api.storage.IMEMonitorHandlerReceiver;
//import appeng.api.storage.data.IAEItemStack;
//import appeng.me.cluster.implementations.CraftingCPUCluster;
//import appeng.me.helpers.MachineSource;
//import gregtech.api.metatileentity.MetaTileEntity;
//import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
//import gregtech.api.metatileentity.multiblock.IMultiblockPart;
//import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
//import gregtech.api.pattern.BlockPattern;
//import gregtech.api.pattern.FactoryBlockPattern;
//import gregtech.api.pattern.PatternMatchContext;
//import gregtech.client.renderer.ICubeRenderer;
//import gregtech.client.renderer.texture.Textures;
//import gregtech.common.blocks.BlockMetalCasing;
//import gregtech.common.blocks.MetaBlocks;
//import it.unimi.dsi.fastutil.objects.ObjectArrayList;
//import keqing.gtqt.prismplan.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockCPUMEChannel;
//import net.minecraft.block.state.IBlockState;
//import net.minecraft.util.ResourceLocation;
//
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.List;
//
//public class MetaTileEntityMultiblockCPU extends MultiblockWithDisplayBase {
//
//    private final HashMap<IMEMonitorHandlerReceiver<IAEItemStack>, Object> listeners = new HashMap<>();
//    private long bytes;
//    private int coProcessors;
//    private int MaxCoreCount;
//    private MachineSource machineSrc = null;
//    protected final ObjectArrayList<CraftingCPUCluster> cpus = new ObjectArrayList<>();
//    private MetaTileEntityMultiblockCPUMEChannel meChannel;
//
//    public MetaTileEntityMultiblockCPU(ResourceLocation metaTileEntityId) {
//        super(metaTileEntityId);
//    }
//
//    @Override
//    protected void formStructure(PatternMatchContext context) {
//        super.formStructure(context);
//        // 统计结构中的存储元件和协处理器数量
//        this.bytes = context.getOrDefault("StorageComponentCount", 0) * 1024L;
//        this.coProcessors = context.getOrDefault("CoProcessorCount", 0);
//        this.machineSrc = new MachineSource((IActionHost) this.getHolder());
//    }
//
//
//    @Override
//    protected void updateFormedValid() {
//        if (isStructureFormed()) {
//            this.bytes = 0;
//            this.coProcessors = 0;
//            this.MaxCoreCount = 0;
//        }
//    }
//
//    protected BlockPattern createStructurePattern() {
//        return FactoryBlockPattern.start()
//                .aisle("XXX", "XXX", "XXX")
//                .aisle("XXX", "XXX", "XXX")
//                .aisle("XXX", "XSX", "XXX")
//                .where('S', this.selfPredicate())
//                .where('X', states(this.getCasingState()))
//                .build();
//    }
//
//    private IBlockState getCasingState() {
//        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
//    }
//
//    @Override
//    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
//        return Textures.SOLID_STEEL_CASING;
//    }
//
//    @Override
//    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
//        return new MetaTileEntityMultiblockCPU(metaTileEntityId);
//    }
//
//    @Override
//    public boolean hasMaintenanceMechanics() {
//        return false;
//    }
//
//    @Override
//    public boolean hasMufflerMechanics() {
//        return false;
//    }
//
//    public List<CraftingCPUCluster> getCPUs() {
//        return this.cpus;
//    }
//
//}