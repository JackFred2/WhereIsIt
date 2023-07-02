package red.jackf.whereisit.client.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import red.jackf.whereisit.api.SearchRequest;
import red.jackf.whereisit.api.criteria.*;
import red.jackf.whereisit.client.WhereIsItClient;

import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * Takes a {@link Screen}, and attempts to add search criteria to a given request. Goes through all populators until
 * one adds criteria.
 * <p>
 * Normally, this just involves parsing a hovered {@link ItemStack}, using {@link red.jackf.whereisit.api.criteria.ItemCriterion}.
 * It may also be used to add more specific criteria, such as a {@link ItemTagCriterion}
 * in a recipe viewer, or fluids/energy if hovered over a tank/battery.
 * </p>
 */
public interface SearchRequestPopulator {
    Event<SearchRequestPopulator> EVENT = EventFactory.createArrayBacked(SearchRequestPopulator.class, listeners -> (request, screen, mouseX, mouseY) -> {
        for (SearchRequestPopulator listener : listeners) {
            listener.grabStack(request, screen, mouseX, mouseY);
            if (request.hasCriteria()) break;
        }
    });

    /**
     * Takes an ItemStack when selected from an Item Overlay, and adds relevant criterion to the request. This allows
     * for custom search behavior, such as enchanted books looking for enchantments instead.
     * This event should return true if the behavior was triggered, short-circuiting the event.
     */
    Event<OverlayStackBehavior> OVERLAY_STACK_BEHAVIOR = EventFactory.createArrayBacked(OverlayStackBehavior.class, listeners -> (consumer, stack) -> {
        for (OverlayStackBehavior listener : listeners)
            if (listener.processOverlayStackBehavior(consumer, stack)) return true;
        return false;
    });

    /**
     * Add search criteria from a given screen. All criteria should be added to <code>request</code>.
     *
     * @param request Request to add criteria to
     * @param screen  Screen currently open on the client
     * @param mouseX  Transformed mouse X
     * @param mouseY  Transformed mouse Y
     */
    void grabStack(SearchRequest request, Screen screen, int mouseX, int mouseY);

    /**
     * Adds an ItemStack to a given request; NBT will be ignored unless the user is holding Shift.
     *
     * @param consumer Consumer to add criterion to, usually a search request.
     * @param stack    Stack to parse depending on <code>context</code>
     * @param context  Context that this stack is being parsed by
     */
    static void addItemStack(Consumer<Criterion> consumer, ItemStack stack, Context context) {
        WhereIsItClient.LOGGER.debug("Adding {}, context: {}", stack, context);
        var criterion = new ArrayList<Criterion>();
        // checks if it's an overlay stack without custom behavior
        if (context != Context.OVERLAY || !OVERLAY_STACK_BEHAVIOR.invoker().processOverlayStackBehavior(criterion::add, stack)) {
            criterion.add(new ItemCriterion(stack.getItem()));
            if (context == Context.INVENTORY_PRECISE || context == Context.OVERLAY_ALTERNATE) {
                criterion.add(new NbtCriterion(stack.getTag(), true));
            } else if (context == Context.FAVOURITE) {
                EnchantmentHelper.getEnchantments(stack).forEach((ench, level) -> criterion.add(new EnchantmentCriterion(ench, level)));
                if (stack.hasCustomHoverName()) criterion.add(new NameCriterion(stack.getHoverName().getString()));
                // TODO add potions
            }
        }

        consumer.accept(new AllOfCriterion(criterion).compact());
    }

    interface OverlayStackBehavior {
        boolean processOverlayStackBehavior(Consumer<Criterion> consumer, ItemStack stack);
    }

    enum Context {
        /**
         * Tangible item in an inventory. Only uses the Item ID.
         */
        INVENTORY,
        /**
         * Tangible item in an inventory, with precision due to user holding shift. Additionally, adds criterion for an
         * exact match of NBT.
         */
        INVENTORY_PRECISE,
        /**
         * An ingredient in a recipe display, such as EMI, REI, JEI or the vanilla recipe book. Uses an Item ID, Fluid ID,
         * or relevant Tags.
         */
        RECIPE,
        /**
         * A mass item list, such as the overlays in recipe viewers. Normally just searches for the item ID, however may
         * instead return special Criterion instead (such as EnchantmentCriterion for enchanted books).
         */
        OVERLAY,
        /**
         * A mass item list, such as the overlays in recipe viewers. Alternate behaviour triggered by the user holding
         * shift. Functions the same as INVENTORY_PRECISE.
         */
        OVERLAY_ALTERNATE,
        /**
         * An item the player specifically remembers, such as the favourites section in a recipe viewer, or the remembered
         * items in ChestTracker. Adds selective criterion such as enchantments, name and potions but not for general NBT.
         * This is so someone can favourite an e.g. shulker box or pickaxe.
         */
        FAVOURITE;

        public static Context inventory() {
            return Screen.hasShiftDown() ? INVENTORY_PRECISE : INVENTORY;
        }

        public static Context overlay() {
            return Screen.hasShiftDown() ? OVERLAY_ALTERNATE : OVERLAY;
        }
    }
}
