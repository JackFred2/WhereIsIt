package red.jackf.whereisit.client;

import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundTag;

/**
 * Called on search key pressed. Mainly used for ChestTracker
 */
public interface ItemSearchCallback {
    void searchForItem(Item item, boolean matchNbt, CompoundTag tag);
}
