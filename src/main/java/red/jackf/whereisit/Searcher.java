package red.jackf.whereisit;

import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import red.jackf.whereisit.api.CustomItemBehavior;
import red.jackf.whereisit.api.CustomWorldBehavior;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import static red.jackf.whereisit.WhereIsIt.log;

public class Searcher {
    public static final Map<Predicate<ItemStack>, CustomItemBehavior> itemBehaviors = new HashMap<>();
    public static final Map<Predicate<BlockState>, CustomWorldBehavior> worldBehaviors = new HashMap<>();

    public static Map<BlockPos, FoundType> search(BlockPos basePos, ServerWorld world, Item toFind) {
        Map<BlockPos, FoundType> positions = new HashMap<>();
        final int radius = WhereIsIt.CONFIG.searchRadius;
        BlockPos.Mutable checkPos = new BlockPos.Mutable();

        for (int y = Math.max(-radius + basePos.getY(), 0); y < Math.min(radius + 1 + basePos.getY(), world.getDimensionHeight()); y++) {
            for (int x = -radius + basePos.getX(); x < radius + 1 + basePos.getX(); x++) {
                for (int z = -radius + basePos.getZ(); z < radius + 1 + basePos.getZ(); z++) {
                    checkPos.set(x, y, z);
                    BlockState state = world.getBlockState(checkPos);
                    try {
                        for (Map.Entry<Predicate<BlockState>, CustomWorldBehavior> entry : worldBehaviors.entrySet()) {
                            if (entry.getKey().test(state)) {
                                FoundType result = entry.getValue().containsItem(toFind, state, checkPos, world);
                                if (result != FoundType.NOT_FOUND) {
                                    positions.put(checkPos.toImmutable(), result);
                                    break;
                                }
                            }
                        }
                    } catch (Exception ex) {
                        log("Error searching for item: " + ex.toString() + "|" + Arrays.toString(ex.getStackTrace()));
                    }
                }
            }
        }

        return positions;
    }
}
