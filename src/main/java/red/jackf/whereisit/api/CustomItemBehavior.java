package red.jackf.whereisit.api;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

/**
 * Defines a custom behavior for an item when searching for 'sub-items'.
 * Intended for items with inventories such as Shulker Boxes, Backpacks, Strongboxes etc.
 */
public interface CustomItemBehavior {

    /**
     * @param item         The custom item that is being searched. (Ex: Shulker Box).
     * @param searchingFor The item that is currently being searched for.
     * @return Whether `item` contains any instances of `searchingFor`.
     */
    boolean containsItem(ItemStack item, Item searchingFor, CompoundTag searchingForNbt);
}
