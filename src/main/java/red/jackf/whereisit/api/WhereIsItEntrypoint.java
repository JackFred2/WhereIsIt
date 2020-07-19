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
     * Set up custom item and world behaviors to search.
     *
     * @param searcher The searcher object to use -
     *                 call {@link red.jackf.whereisit.Searcher#addItemBehavior(Predicate, CustomItemBehavior)} and
     *                 call {@link red.jackf.whereisit.Searcher#addWorldBehavior(Predicate, CustomWorldBehavior)}
     */

    @ApiStatus.OverrideOnly
    default void setupBehaviors(Searcher searcher) {
    }

    /**
     * @return The priority of this plugin to load. Plugins will load in ascending order (0 first, 1 second, ...)
     */
    @ApiStatus.OverrideOnly
    default int getPriority() {
        return 0;
    }
}
