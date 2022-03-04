package red.jackf.whereisit;

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
import net.minecraft.text.Text;
import net.minecraft.util.Nameable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;
import red.jackf.whereisit.utilities.FoundType;
import red.jackf.whereisit.utilities.SearchResult;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class Searcher {
    public static Map<BlockPos, SearchResult> searchWorld(BlockPos playerPos, ServerWorld world, Item toFind, NbtCompound toFindTag, int maximumCount) {
        Map<BlockPos, SearchResult> positions = new HashMap<>();
        final int radius = WhereIsIt.CONFIG.getSearchRadius();

        int checkedBECount = 0;

        int minChunkX = (-radius + playerPos.getX()) >> 4;
        int maxChunkX = (radius + 1 + playerPos.getX()) >> 4;
        int minChunkZ = (-radius + playerPos.getZ()) >> 4;
        int maxChunkZ = (radius + 1 + playerPos.getZ()) >> 4;

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {

                WorldChunk chunk = world.getChunk(chunkX, chunkZ);
                if (chunk == null) continue;

                checkedBECount += chunk.getBlockEntities().size();

                for (Map.Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
                    var pos = entry.getKey();
                    var be = entry.getValue();
                    if (pos.isWithinDistance(playerPos, radius)) {
                        var state = chunk.getBlockState(pos);
                        var foundType = FoundType.NOT_FOUND;
                        Text invName = null;

                        if (be instanceof Nameable name && name.hasCustomName()) invName = name.getCustomName();

                        // Lecterns
                        if (state.getBlock() instanceof LecternBlock && state.get(LecternBlock.HAS_BOOK)) {
                            foundType = searchItemStack(((LecternBlockEntity) be).getBook(), toFind, toFindTag, true);
                            // Inventories (Chests etc)
                        } else if (be instanceof Inventory) {
                            foundType = invContains((Inventory) be, toFind, toFindTag, true);
                            // Alternative inventories (Composters)
                        } else if (state.getBlock() instanceof InventoryProvider) {
                            Inventory inv = ((InventoryProvider) state.getBlock()).getInventory(state, world, pos);
                            if (inv != null)
                                foundType = invContains(inv, toFind, toFindTag, true);
                        }

                        if (foundType != FoundType.NOT_FOUND)
                            positions.put(pos.toImmutable(), new SearchResult(foundType, invName));
                    }
                }
            }
        }

        if (WhereIsIt.CONFIG.printSearchTime()) {
            WhereIsIt.log("Checked " + checkedBECount + " BlockEntities");
        }

        if (positions.size() > maximumCount) {
            return positions.entrySet().stream()
                .sorted((e1, e2) -> {
                    var e1distance = e1.getKey().getSquaredDistance(playerPos);
                    var e2distance = e2.getKey().getSquaredDistance(playerPos);
                    return Double.compare(e1distance, e2distance);
                })
                .limit(maximumCount)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (result1, result2) -> result1, LinkedHashMap::new));
        } else {
            return positions;
        }
    }

    public static FoundType searchItemStack(ItemStack itemStack, Item toFind, NbtCompound toFindTag, boolean deepSearch) {
        if (itemStack.getItem() == toFind && (toFindTag == null || toFindTag.equals(itemStack.getNbt()))) {
            return FoundType.FOUND;
        } else if (!itemStack.isEmpty() && WhereIsIt.CONFIG.doDeepSearch() && deepSearch) {
            // Shulker Boxes
            if (itemStack.getItem() instanceof BlockItem && ((BlockItem) itemStack.getItem()).getBlock() instanceof ShulkerBoxBlock) {
                NbtCompound tag = itemStack.getSubNbt("BlockEntityTag");
                if (tag != null && tag.contains("Items", 9)) {
                    NbtList items = tag.getList("Items", 10);
                    for (int i = 0; i < items.size(); i++) {
                        ItemStack containedStack = ItemStack.fromNbt(items.getCompound(i));
                        if (containedStack.getItem() == toFind && (toFindTag == null || toFindTag.equals(containedStack.getNbt()))) {
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

    public static boolean areStacksEqual(Item item1, NbtCompound tag1, Item item2, NbtCompound tag2, boolean matchNbt) {
        return Objects.equals(item1, item2) && (!matchNbt || Objects.equals(tag1, tag2));
    }
}
