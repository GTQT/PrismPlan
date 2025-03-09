package keqing.gtqt.prismplan.common.item.prismplan;

import gregtech.api.GregTechAPI;
import gregtech.api.items.metaitem.StandardMetaItem;
import keqing.gtqt.prismplan.PrismPlan;
import keqing.gtqt.prismplan.common.item.PrismPlanMetaItems;
import static keqing.gtqt.prismplan.common.CommonProxy.PRISM_PLAN_TAB;

public class PrismPlanMetaItem extends StandardMetaItem {
    public PrismPlanMetaItem() {
        this.setRegistryName("gtqt_meta_item_1");
        setCreativeTab(PRISM_PLAN_TAB);
    }

    public void registerSubItems() {

        PrismPlanMetaItems.PARALLEL_CIRCUIT_BOARD = this.addItem(1, "parallel_circuit_board");

        PrismPlanMetaItems.SPECULATIVE_CIRCUIT_BOARD = this.addItem(2, "speculative_circuit_board");

        PrismPlanMetaItems.FLUIX_SOC = this.addItem(3, "fluix_soc");

        PrismPlanMetaItems.FLUIX_CPU = this.addItem(4, "fluix_cpu");

        PrismPlanMetaItems.DEDUCTION_CIRCUIT_BOARD = this.addItem(5, "deduction_circuit_board");

        PrismPlanMetaItems.DEDUCTION_PROCESSOR = this.addItem(6, "deduction_processor");

        PrismPlanMetaItems.FLUIX_LOGIC_PROCESSOR = this.addItem(7, "fluix_logic_processor").addOreDict("circuitFluixBasic");

        PrismPlanMetaItems.FLUIX_LOGIC_ASSEMBLY = this.addItem(8, "fluix_logic_assembly").addOreDict("circuitFluixAdvanced");

        PrismPlanMetaItems.FLUIX_LOGIC_COMPUTER = this.addItem(9, "fluix_logic_computer").addOreDict("circuitFluixElite");

        PrismPlanMetaItems.FLUIX_LOGIC_MAINFRAME = this.addItem(10, "fluix_logic_mainframe").addOreDict("circuitFluixMaster");
    }
}
