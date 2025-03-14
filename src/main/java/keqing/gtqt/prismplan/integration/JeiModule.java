package keqing.gtqt.prismplan.integration;

import appeng.api.definitions.IItemDefinition;
import gregtech.integration.IntegrationSubmodule;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import keqing.gtqt.prismplan.Tags;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JEIPlugin;
import net.minecraft.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@JEIPlugin
public class JeiModule extends IntegrationSubmodule implements IModPlugin {
    public static final Logger logger = LogManager.getLogger("Prism Plan JEI Integration");

    private static final ObjectOpenHashSet<ItemStack> ingredientItemBlacklist = new ObjectOpenHashSet<>();
    private static final ObjectOpenHashSet<IItemDefinition> ingredientAE2ItemBlacklist = new ObjectOpenHashSet<>();

    private static final Object2ObjectOpenHashMap<ItemStack, String[]> ingredientDescriptions = new Object2ObjectOpenHashMap<>();
    private static final Object2ObjectOpenHashMap<IItemDefinition, String[]> ingredientAE2Descriptions = new Object2ObjectOpenHashMap<>();

    /**
     * Add Item Stack to JEI Blacklist.
     *
     * @author Magic_Sweepy
     *
     * @param ingredient  Item Stack.
     *
     * @since 1.0.0
     */
    public static void addItemToBlacklist(ItemStack ingredient) {
        ingredientItemBlacklist.add(ingredient);
    }

    /**
     * Add Item Definition to JEI Blacklist.
     *
     * @author Magic_Sweepy
     *
     * @param ingredient  Item Definition (AE2 Item form).
     *
     * @since 1.0.0
     */
    public static void addItemToBlacklist(IItemDefinition ingredient) {
        ingredientAE2ItemBlacklist.add(ingredient);
    }

    /**
     * Add JEI Description to Item Stack.
     *
     * @author Magic_Sweepy
     *
     * @param stack         Item Stack.
     * @param descriptions  Description texts.
     *
     * @since 1.0.0
     */
    public static void addDescription(ItemStack stack, String... descriptions) {
        ingredientDescriptions.put(stack, descriptions);
    }

    /**
     * Add JEI Description to Item Definition.
     *
     * @author Magic_Sweepy
     *
     * @param definition    Item Definition (AE2 Item form).
     * @param descriptions  Description texts.
     *
     * @since 1.0.0
     */
    public static void addDescription(IItemDefinition definition, String... descriptions) {
        ingredientAE2Descriptions.put(definition, descriptions);
    }

    @Override
    public Logger getLogger() {
        return logger;
    }
}
