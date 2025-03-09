package keqing.gtqt.prismplan.common.item;

import gregtech.api.items.metaitem.MetaItem;
import keqing.gtqt.prismplan.common.item.prismplan.PrismPlanMetaItem;

public class PrismPlanMetaItems {

    public static PrismPlanMetaItem PrismPlan_META_ITEM;

    public static MetaItem<?>.MetaValueItem PARALLEL_CIRCUIT_BOARD;
    public static MetaItem<?>.MetaValueItem SPECULATIVE_CIRCUIT_BOARD;
    public static MetaItem<?>.MetaValueItem FLUIX_SOC;
    public static MetaItem<?>.MetaValueItem FLUIX_CPU;
    public static MetaItem<?>.MetaValueItem DEDUCTION_CIRCUIT_BOARD;
    public static MetaItem<?>.MetaValueItem DEDUCTION_PROCESSOR;
    public static MetaItem<?>.MetaValueItem FLUIX_LOGIC_PROCESSOR;
    public static MetaItem<?>.MetaValueItem FLUIX_LOGIC_ASSEMBLY;
    public static MetaItem<?>.MetaValueItem FLUIX_LOGIC_COMPUTER;
    public static MetaItem<?>.MetaValueItem FLUIX_LOGIC_MAINFRAME;
    public static void initialization() {
        PrismPlan_META_ITEM = new PrismPlanMetaItem();
    }

    public static void initSubItems() {
        PrismPlanMetaItem.registerItems();

    }
}
