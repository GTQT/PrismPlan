package keqing.gtqt.prismplan.mixin.ae2;

import appeng.api.networking.crafting.ICraftingCPU;
import appeng.container.implementations.CraftingCPUStatus;
import keqing.gtqt.prismplan.api.capability.ECPUStatus;
import keqing.gtqt.prismplan.api.capability.Levels;
import keqing.gtqt.prismplan.common.metatileentities.multi.multiblock.ecalculator.ECPUCluster;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CraftingCPUStatus.class, remap = false)
public class MixinCraftingCPUStatus implements ECPUStatus {

    @Unique
    private Levels prismplan_ec$ecLevel;

    @Inject(method = "<init>(Lappeng/api/networking/crafting/ICraftingCPU;I)V", at = @At("RETURN"))
    private void injectInit(final ICraftingCPU cluster, final int serial, final CallbackInfo ci) {
        if (cluster instanceof ECPUCluster ecpuCluster) {
            this.prismplan_ec$ecLevel = ecpuCluster.prismplan_ec$getControllerLevel();
        }
    }

    @Inject(method = "<init>(Lnet/minecraft/nbt/NBTTagCompound;)V", at = @At("RETURN"))
    private void injectInit(final NBTTagCompound i, final CallbackInfo ci) {
        if (i.hasKey("ecLevel")) {
            this.prismplan_ec$ecLevel = Levels.values()[i.getByte("ecLevel")];
        }
    }

    @Inject(method = "writeToNBT", at = @At("RETURN"))
    private void injectWriteToNBT(final NBTTagCompound i, final CallbackInfo ci) {
        if (prismplan_ec$ecLevel == null) {
            return;
        }
        i.setByte("ecLevel", (byte) prismplan_ec$ecLevel.ordinal());
    }

    @Override
    public Levels prismplan_ec$getLevel() {
        return prismplan_ec$ecLevel;
    }

}
