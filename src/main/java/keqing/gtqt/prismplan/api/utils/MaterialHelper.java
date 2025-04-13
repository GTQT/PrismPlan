package keqing.gtqt.prismplan.api.utils;

import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.unification.material.Material;
import keqing.gtqt.prismplan.common.item.ae2.ecalculator.ECalculatorCell;
import keqing.gtqt.prismplan.common.item.ae2.estorage.EStorageCellFluid;
import keqing.gtqt.prismplan.common.item.ae2.estorage.EStorageCellItem;

import static gregtech.api.unification.material.Materials.*;
import static gregtech.common.items.MetaItems.*;
import static keqing.gtqt.prismplan.common.item.PrismPlanMetaItems.*;

public class MaterialHelper {
    public static MetaItem.MetaValueItem[] circuitList = {DEDUCTION_PROCESSOR, FLUIX_LOGIC_PROCESSOR, FLUIX_LOGIC_ASSEMBLY, FLUIX_LOGIC_COMPUTER, FLUIX_LOGIC_MAINFRAME};
    public static MetaItem.MetaValueItem[] roborArmList = {ROBOT_ARM_IV, ROBOT_ARM_LuV, ROBOT_ARM_ZPM, ROBOT_ARM_UV, ROBOT_ARM_UHV};
    public static MetaItem.MetaValueItem[] fieldGeneratorList = {FIELD_GENERATOR_IV, FIELD_GENERATOR_LuV, FIELD_GENERATOR_ZPM, FIELD_GENERATOR_UV, FIELD_GENERATOR_UHV};
    public static MetaItem.MetaValueItem[] emitterList = {EMITTER_IV, EMITTER_LuV, EMITTER_ZPM, EMITTER_UV, EMITTER_UHV};

    public static MetaItem.MetaValueItem[] batteryList = {ENERGY_LAPOTRONIC_ORB, ENERGY_LAPOTRONIC_ORB_CLUSTER, ENERGY_MODULE, ENERGY_CLUSTER, ULTIMATE_BATTERY};

    public static Material[] Plate = {TungstenSteel, RhodiumPlatedPalladium, NaquadahAlloy, Darmstadtium, Neutronium};
    public static Material[] Pipe = {NiobiumTitanium, Iridium, Naquadah, Europium, EnrichedNaquadahTriniumEuropiumDuranide};

    public static EStorageCellFluid[] fluidCellList = {EStorageCellFluid.LEVEL_A, EStorageCellFluid.LEVEL_B, EStorageCellFluid.LEVEL_C, EStorageCellFluid.LEVEL_D, EStorageCellFluid.LEVEL_E};
    public static EStorageCellItem[] itemCellList = {EStorageCellItem.LEVEL_A, EStorageCellItem.LEVEL_B, EStorageCellItem.LEVEL_C, EStorageCellItem.LEVEL_D, EStorageCellItem.LEVEL_E};
    public static ECalculatorCell[] calculatorCellList = {ECalculatorCell.L1, ECalculatorCell.L2, ECalculatorCell.L3, ECalculatorCell.L4, ECalculatorCell.L5};
}
