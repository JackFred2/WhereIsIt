package red.jackf.whereisit.api;

import org.jetbrains.annotations.ApiStatus;
import red.jackf.whereisit.Searcher;
import red.jackf.whereisit.compat.VanillaItemBehaviors;

import java.util.function.Predicate;

/**
 * Entrypoint for a plugin.
 *
 * @see VanillaItemBehaviors VanillaItemBehaviors.
 */
public interface WhereIsItEntrypoint {

    /**
     * Set up custom item behaviors for items in chest.
     * Intended for items with inventories such as shulker boxes, backpacks, etc.
     *
     * @param searcher The searcher object to use - call {@link red.jackf.whereisit.Searcher#addItemBehavior(Predicate, CustomItemBehavior)}
     */

    @ApiStatus.OverrideOnly
    default void setupItemBehaviors(Searcher searcher) {
    }

    /**
     * Set up custom block behaviors for blocks in world.
     * Intended for blocks that hold items in world that do not implement {@link net.minecraft.inventory.Inventory}
     *
     * @param searcher The searcher object to use - call {@link red.jackf.whereisit.Searcher#addWorldBehavior(Predicate, CustomWorldBehavior)}
     */

    @ApiStatus.OverrideOnly
    default void setupWorldBehaviors(Searcher searcher) {
    }

    /**
     * @return The priority of this plugin to load. Plugins will load in ascending order (0 first, 1 second, ...)
     */
    @ApiStatus.OverrideOnly
    default int getPriority() {
        return 0;
    }
}
