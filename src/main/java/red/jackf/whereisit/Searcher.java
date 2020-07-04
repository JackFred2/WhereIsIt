package red.jackf.whereisit;

import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;
import red.jackf.whereisit.api.CustomItemBehavior;
import red.jackf.whereisit.api.CustomWorldBehavior;

import java.util.*;
import java.util.function.Predicate;

import static red.jackf.whereisit.WhereIsIt.log;

public class Searcher {
    private final List<ItemBehavior> itemBehaviors = new LinkedList<>();
    private final List<WorldBehavior> worldBehaviors = new LinkedList<>();

    public Map<BlockPos, FoundType> searchWorld(BlockPos basePos, ServerWorld world, Item toFind) {
        Map<BlockPos, FoundType> positions = new HashMap<>();
        final int radius = WhereIsIt.CONFIG.getSearchRadius();
        BlockPos.Mutable checkPos = new BlockPos.Mutable();

        int minChunkX = (-radius + basePos.getX()) >> 4;
        int maxChunkX = (radius + 1 + basePos.getX()) >> 4;
        int minChunkZ = (-radius + basePos.getZ()) >> 4;
        int maxChunkZ = (radius + 1 + basePos.getZ()) >> 4;

        int minX = -radius + basePos.getX();
        int minZ = -radius + basePos.getZ();
        int maxX = radius + 1 + basePos.getX();
        int maxZ = radius + 1 + basePos.getZ();

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            int minXThisChunk = Math.max(minX, chunkX * 16);
            int maxXThisChunk = Math.min(maxX, chunkX * 16 + 16);
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                int minZThisChunk = Math.max(minZ, chunkZ * 16);
                int maxZThisChunk = Math.min(maxZ, chunkZ * 16 + 16);

                WorldChunk chunk = world.getChunk(chunkX, chunkZ);

                for (int y = Math.max(-radius + basePos.getY(), 0); y < Math.min(radius + 1 + basePos.getY(), world.getDimensionHeight()); y++)
                    for (int x = minXThisChunk; x < maxXThisChunk; x++)
                        for (int z = minZThisChunk; z < maxZThisChunk; z++) {
                            checkPos.set(x, y, z);
                            BlockState state = chunk.getBlockState(checkPos);
                            try {
                                for (WorldBehavior entry : worldBehaviors) {
                                    if (entry.getTest().test(state)) {
                                        FoundType result = entry.getAction().containsItem(toFind, state, checkPos, world);
                                        if (result != FoundType.NOT_FOUND) {
                                            positions.put(checkPos.toImmutable(), result);
                                            break;
                                        }
                                    }
                                }
                            } catch (Exception ex) {
                                log("Error searching for item in " + state.getBlock() + ": " + ex.toString() + "|" + Arrays.toString(ex.getStackTrace()));
                            }
                        }
            }

        }

        return positions;
    }

    public FoundType searchItemStack(ItemStack itemStack, Item toFind) {
        if (itemStack.getItem() == toFind) {
            return FoundType.FOUND;
        } else if (!itemStack.isEmpty() && WhereIsIt.CONFIG.doDeepSearch()) {
            for (ItemBehavior behavior : itemBehaviors) {
                if (behavior.getTest().test(itemStack) && behavior.getAction().containsItem(itemStack, toFind)) {
                    return FoundType.FOUND_DEEP;
                }
            }
        }
        return FoundType.NOT_FOUND;
    }

    public void addItemBehavior(Predicate<ItemStack> test, CustomItemBehavior action) {
        itemBehaviors.add(new ItemBehavior(test, action));
    }

    public void addWorldBehavior(Predicate<BlockState> test, CustomWorldBehavior action) {
        worldBehaviors.add(new WorldBehavior(test, action));
    }

    public static class ItemBehavior {
        private final Predicate<ItemStack> test;
        private final CustomItemBehavior action;

        public ItemBehavior(Predicate<ItemStack> test, CustomItemBehavior action) {
            this.test = test;
            this.action = action;
        }

        public CustomItemBehavior getAction() {
            return action;
        }

        public Predicate<ItemStack> getTest() {
            return test;
        }
    }

    public static class WorldBehavior {
        private final Predicate<BlockState> test;
        private final CustomWorldBehavior action;

        public WorldBehavior(Predicate<BlockState> test, CustomWorldBehavior action) {
            this.test = test;
            this.action = action;
        }

        public CustomWorldBehavior getAction() {
            return action;
        }

        public Predicate<BlockState> getTest() {
            return test;
        }
    }
}
