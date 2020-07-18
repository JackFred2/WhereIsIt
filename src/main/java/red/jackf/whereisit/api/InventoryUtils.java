package red.jackf.whereisit.api;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import red.jackf.whereisit.FoundType;
import red.jackf.whereisit.Searcher;
import red.jackf.whereisit.WhereIsIt;

import java.util.function.Predicate;

/**
 * A collection of utilities for searching inventories.
 */
public abstract class InventoryUtils {

    /**
     * Search an inventory for any instances of an item, non-recursively checking any sub-items.
     *
     * @param inv          The {@link net.minecraft.inventory.Inventory} to search.
     * @param searchingFor The item being searched for.
     * @return Whether the inventory contains any instances of {@code searchingFor}.
     */
    public static FoundType invContains(Inventory inv, Item searchingFor, CompoundTag searchingForNbt, boolean deepSearch) {
        for (int i = 0; i < inv.size(); i++) {
            FoundType result = WhereIsIt.SEARCHER.searchItemStack(inv.getStack(i), searchingFor, searchingForNbt, deepSearch);
            if (result != FoundType.NOT_FOUND) return result;
        }
        return FoundType.NOT_FOUND;
    }
}
