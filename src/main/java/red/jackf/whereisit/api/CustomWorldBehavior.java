package red.jackf.whereisit.api;

import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

/**
 * Defines a custom behavior for a block or block entity.
 * Intended for blocks that do not implement {@link net.minecraft.inventory.Inventory}.
 */
public interface CustomWorldBehavior {
    /**
     * @param searchingFor The identifier being searched for.
     * @param state        The position's BlockState.
     * @param pos          The position.
     * @param world        The world the position is in.
     * @return Whether the position contains any instances of `searchingFor`.
     */
    boolean containsItem(Item searchingFor, BlockState state, BlockPos pos, ServerWorld world);
}
