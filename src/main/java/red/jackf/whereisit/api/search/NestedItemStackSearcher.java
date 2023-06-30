package red.jackf.whereisit.api.search;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.item.ItemStack;
import red.jackf.whereisit.api.SearchRequest;

import java.util.function.Predicate;

/**
 * Search for nested ItemStacks. This should handle things like bundles, shulker boxes or backpacks, but not indirectly
 * contained stacks such as remote terminals.
 */
public interface NestedItemStackSearcher {
    Event<NestedItemStackSearcher> EVENT = EventFactory.createArrayBacked(NestedItemStackSearcher.class, listeners -> (source, predicate) -> {
        for (var listener : listeners)
            if (listener.check(source, predicate)) return true;
        return false;
    });

    boolean check(ItemStack source, Predicate<ItemStack> predicate);

    // check an item or any possible sub-items
    static boolean check(ItemStack source, SearchRequest request) {
        return request.test(source) || EVENT.invoker().check(source, request::test);
    }
}
