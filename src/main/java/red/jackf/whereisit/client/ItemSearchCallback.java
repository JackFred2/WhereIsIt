package red.jackf.whereisit.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;

/**
 * Called on search key pressed. Mainly used for ChestTracker
 */
@Environment(EnvType.CLIENT)
public interface ItemSearchCallback {
    void searchForItem(Item item, boolean matchNbt, NbtCompound tag);
}
