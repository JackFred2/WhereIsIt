package red.jackf.whereisit.api.search;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import red.jackf.jackfredlib.api.base.ResultHolder;
import red.jackf.whereisit.WhereIsIt;
import red.jackf.whereisit.api.SearchRequest;
import red.jackf.whereisit.api.SearchResult;

/**
 * <p>Handles searching an individual position in the world.</p>
 *
 * <p>The default handler uses Fabric's transfer API, so if your inventory handles that you're good. You can also use
 * this event for overrides; for example, Where Is It has an override for ender chests to search a player's ender
 * inventory.</p>
 */
public interface BlockSearcher {
    /**
     * Called before any other events. Use this to override other custom handlers.
     */
    ResourceLocation OVERRIDE = WhereIsIt.id("override");
    /**
     * Called before the fallback transfer API handler. Use this for custom behaviors on blocks.
     */
    ResourceLocation DEFAULT = Event.DEFAULT_PHASE;
    /**
     * Used for the transfer API fallback. Not recommended for general use.
     */
    ResourceLocation FALLBACK = WhereIsIt.id("fallback");

    Event<BlockSearcher> EVENT = EventFactory.createWithPhases(BlockSearcher.class, handlers -> ((request, player, level, state, pos) -> {
        for (BlockSearcher handler : handlers) {
            var result = handler.searchPosition(request, player, level, state, pos);
            if (result.shouldTerminate()) return result;
        }
        return ResultHolder.empty();
    }), OVERRIDE, DEFAULT, FALLBACK);

    /**
     * <p>Search a block in the world for a given result.</p>
     *
     * <p>This should return a {@link ResultHolder#value(Object)} if your handlers finds a result, a
     * {@link ResultHolder#empty()} if your handler does <i>not</i> find a result but should logically be the only
     * handler for the given arguments, and {@link ResultHolder#pass()} otherwise in order to pass onto further
     * processing.</p>
     * @param request Search request to test against. Use {@link SearchRequest#check(ItemStack, SearchRequest)} for each
     *                {@link ItemStack} in your block until the first stack returns true, then return a
     *                {@link ResultHolder#value(Object))} holding a {@link SearchResult} with at least the given position.
     *                If no result is found, return {@link ResultHolder#empty()} or {@link ResultHolder#pass()} depending
     *                on the context.
     * @param player Player initiating the request, use for per-player storages.
     * @param level Server level the search is being conducted in. Use {@link Level#getBlockEntity(BlockPos)} if needed.
     * @param state BlockState at the given position in the level.
     * @param pos Position in the level that is being checked.
     * @return A {@link ResultHolder} either containing a positive search result ({@link ResultHolder#value(Object)}), a
     * definitive lack of result ({@link ResultHolder#empty()}) or a pass on to further processing ({@link ResultHolder#pass()}).
     */
    ResultHolder<SearchResult> searchPosition(SearchRequest request, ServerPlayer player, ServerLevel level, BlockState state, BlockPos pos);
}
