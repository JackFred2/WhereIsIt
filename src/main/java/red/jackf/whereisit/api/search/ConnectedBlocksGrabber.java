package red.jackf.whereisit.api.search;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <p>Gets all connected blocks to a given position, and adds to a given set. This is used to deduplicate search results
 * in the case of multi-block storages, such as double chests in vanilla.
 */
public interface ConnectedBlocksGrabber {
    Event<ConnectedBlocksGrabber> EVENT = EventFactory.createArrayBacked(ConnectedBlocksGrabber.class, handlers -> ((positions, pos, level, state) -> {
        for (ConnectedBlocksGrabber handler : handlers)
            handler.getConnected(positions, pos, level, state);
    }));

    /**
     * Get all connected blocks to a given position and add it to a set.
     * @param positions Set of positions connected to <code>pos</code>. You should add linked positions to this set if
     *                  your handler handles this.
     * @param pos Position being queried
     * @param level Level that is being queried
     * @param state Block state at the given position
     */
    void getConnected(Set<BlockPos> positions, BlockPos pos, Level level, BlockState state);

    /**
     * Gets all connected blocks linked to a given position. Always includes <code>pos</code>.
     * @param level Level that's being queried
     * @param pos Position to check for linked blocks
     * @return List of all blocks linked to this position. Always includes <code>pos</code>. This list is stable, such
     * that the order of positions are the same each times it is called.
     */
    static List<BlockPos> getConnected(Level level, BlockPos pos) {
        var set = new HashSet<BlockPos>();
        set.add(pos.immutable());
        EVENT.invoker().getConnected(set, pos, level, level.getBlockState(pos));
        if (set.size() == 1) return List.of(pos.immutable());
        return set.stream().sorted(Comparator.comparingLong(BlockPos::asLong)).toList();
    }
}
