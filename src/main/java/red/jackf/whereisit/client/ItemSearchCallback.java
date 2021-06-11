package red.jackf.whereisit.client;

import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;

/**
 * Called on search key pressed. Mainly used for ChestTracker
 */
public interface ItemSearchCallback {
    void searchForItem(Item item, boolean matchNbt, NbtCompound tag);
}
