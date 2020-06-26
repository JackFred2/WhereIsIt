package red.jackf.whereisit.api;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import red.jackf.whereisit.FoundType;

/**
 * Defines a custom behavior for an item when searching for 'sub-items'.
 * Intended for items with inventories such as Shulker Boxes, Backpacks, Strongboxes etc.
 *
 * These are not ran by default for {@link red.jackf.whereisit.api.CustomWorldBehavior}s , and must be ran for each item
 * separately.
 */
public interface CustomItemBehavior {

    /**
     * @param searchingFor The identifier that is currently being searched for.
     * @param item The custom item that is being searched. (Ex: Shulker Box).
     * @return Whether `item` contains any instances of `searchingFor`.
     */
    boolean containsItem(Item searchingFor, ItemStack item);
}
