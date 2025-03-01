package keqing.gtqt.prismplan.mixin.gregtech;

import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.recipes.RecipeMap;
import keqing.gtqt.prismplan.api.multiblock.PrismPlanMultiblockAbility;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.ArrayList;
import java.util.List;

@Mixin(MultiblockRecipeLogic.class)
public abstract class MultiblockRecipeLogicMixin extends AbstractRecipeLogic {


    public MultiblockRecipeLogicMixin(MetaTileEntity tileEntity, RecipeMap<?> recipeMap) {
        super(tileEntity, recipeMap);
    }

    @Overwrite
    protected List<IItemHandlerModifiable> getInputBuses() {
        RecipeMapMultiblockController controller = (RecipeMapMultiblockController)this.metaTileEntity;
        var im_item = controller.getAbilities(MultiblockAbility.IMPORT_ITEMS);
        var im_itemCC = controller.getAbilities(PrismPlanMultiblockAbility.PRISMPLAN_AE);
        List<IItemHandlerModifiable> itemlist = new ArrayList<>();
        itemlist.addAll(im_item);
        itemlist.addAll(im_itemCC);
        return itemlist;
    }
}
