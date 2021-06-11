package red.jackf.whereisit;

import net.minecraft.block.BlockState;
import net.minecraft.block.InventoryProvider;
import net.minecraft.block.LecternBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;

import java.util.*;

public abstract class Searcher {
    public static Map<BlockPos, FoundType> searchWorld(BlockPos basePos, ServerWorld world, Item toFind, NbtCompound toFindTag) {
        Map<BlockPos, FoundType> positions = new HashMap<>();
        final int radius = WhereIsIt.CONFIG.getSearchRadius();
        int checkedBECount = 0;

        int minChunkX = (-radius + basePos.getX()) >> 4;
        int maxChunkX = (radius + 1 + basePos.getX()) >> 4;
        int minChunkZ = (-radius + basePos.getZ()) >> 4;
        int maxChunkZ = (radius + 1 + basePos.getZ()) >> 4;

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {

                WorldChunk chunk = world.getChunk(chunkX, chunkZ);
                if (chunk == null) continue;

                checkedBECount += chunk.getBlockEntities().size();

                for (Map.Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
                    BlockPos pos = entry.getKey();
                    BlockEntity be = entry.getValue();
                    if (pos.isWithinDistance(basePos, radius)) {
                        BlockState state = chunk.getBlockState(pos);
                        FoundType result = FoundType.NOT_FOUND;

                        // Lecterns
                        if (state.getBlock() instanceof LecternBlock && state.get(LecternBlock.HAS_BOOK)) {
                            result = searchItemStack(((LecternBlockEntity) be).getBook(), toFind, toFindTag, true);
                        // Inventories (Chests etc)
                        } else if (be instanceof Inventory) {
                            result = invContains((Inventory) be, toFind, toFindTag, true);
                        // Alternative inventories (Composters)
                        } else if (state.getBlock() instanceof InventoryProvider) {
                            Inventory inv = ((InventoryProvider) state.getBlock()).getInventory(state, world, pos);
                            if (inv != null)
                                result = invContains(inv, toFind, toFindTag, true);
                        }

                        if (result != FoundType.NOT_FOUND)
                            positions.put(pos.toImmutable(), result);
                    }
                }
            }
        }

        if (WhereIsIt.CONFIG.printSearchTime()) {
            WhereIsIt.log("Checked " + checkedBECount + " BlockEntities");
        }

        return positions;
    }

    public static FoundType searchItemStack(ItemStack itemStack, Item toFind, NbtCompound toFindTag, boolean deepSearch) {
        if (itemStack.getItem() == toFind && (toFindTag == null || toFindTag.equals(itemStack.getTag()))) {
            return FoundType.FOUND;
        } else if (!itemStack.isEmpty() && WhereIsIt.CONFIG.doDeepSearch() && deepSearch) {
            // Shulker Boxes
            if (itemStack.getItem() instanceof BlockItem && ((BlockItem) itemStack.getItem()).getBlock() instanceof ShulkerBoxBlock) {
                NbtCompound tag = itemStack.getSubTag("BlockEntityTag");
                if (tag != null && tag.contains("Items", 9)) {
                    NbtList items = tag.getList("Items", 10);
                    for (int i = 0; i < items.size(); i++) {
                        ItemStack containedStack = ItemStack.fromNbt(items.getCompound(i));
                        if (containedStack.getItem() == toFind && (toFindTag == null || toFindTag.equals(containedStack.getTag()))) {
                            return FoundType.FOUND_DEEP;
                        }
                    }
                }
            }
        }
        return FoundType.NOT_FOUND;
    }

    /**
     * Search an inventory for any instances of an item, non-recursively checking any sub-items.
     *
     * @param inv          The {@link Inventory} to search.
     * @param searchingFor The item being searched for.
     * @return Whether the inventory contains any instances of {@code searchingFor}.
     */
    public static FoundType invContains(Inventory inv, Item searchingFor, NbtCompound searchingForNbt, boolean deepSearch) {
        for (int i = 0; i < inv.size(); i++) {
            FoundType result = searchItemStack(inv.getStack(i), searchingFor, searchingForNbt, deepSearch);
            if (result != FoundType.NOT_FOUND) return result;
        }
        return FoundType.NOT_FOUND;
    }

    public static boolean areStacksEqual(Item item1, NbtCompound tag1, Item item2, NbtCompound tag2, boolean ignoreNbt) {
        return item1.equals(item2) && (ignoreNbt || Objects.equals(tag1,tag2));
    }
}
