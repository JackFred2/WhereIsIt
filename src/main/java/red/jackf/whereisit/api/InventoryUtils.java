package red.jackf.whereisit.api;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import red.jackf.whereisit.WhereIsIt;

import java.util.function.Predicate;

/**
 * A collection of utilities for searching inventories.
 */
public abstract class InventoryUtils {

    /**
     * Search an inventory for any instances of an item, non-recursively checking any sub-items.
     * @param inv The {@link net.minecraft.inventory.Inventory} to search.
     * @param searchingFor The item being searched for.
     * @return Whether the inventory contains any instances of {@code searchingFor}.
     */
    public static boolean invContains(Inventory inv, Item searchingFor) {
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
            if (stack.getItem() == searchingFor) {
                return true;
            } else if (!stack.isEmpty()) {
                for (Predicate<ItemStack> predicate : WhereIsIt.itemBehaviors.keySet()) {
                    if (predicate.test(stack) && WhereIsIt.itemBehaviors.get(predicate).containsItem(searchingFor, stack)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
