package red.jackf.whereisit;

import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.collection.DefaultedList;
import red.jackf.whereisit.api.CustomItemBehavior;
import red.jackf.whereisit.api.CustomWorldBehavior;
import red.jackf.whereisit.api.InventoryUtils;
import red.jackf.whereisit.api.WhereIsItEntrypoint;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class VanillaItemBehaviors implements WhereIsItEntrypoint {

    @Override
    public void setupWorldBehaviors(Map<Predicate<BlockState>, CustomWorldBehavior> behaviors) {

        // Behaviors for standard inventories - Chests, hoppers, anything that implements
        // {@link net.minecraft.inventory.Inventory}
        behaviors.put(
            (blockState -> blockState.getBlock() instanceof BlockWithEntity),
            ((searchingFor, state, pos, world) -> {
                BlockEntity be = world.getBlockEntity(pos);
                if (be instanceof Inventory) {
                    return InventoryUtils.invContains((Inventory) be, searchingFor);
                }
                return FoundType.NOT_FOUND;
            })
        );
    }

    @Override
    public void setupItemBehaviors(Map<Predicate<ItemStack>, CustomItemBehavior> behaviors) {

        // Shulker Box
        behaviors.put(
            (itemStack -> itemStack.getItem() instanceof BlockItem && ((BlockItem) (itemStack.getItem())).getBlock() instanceof ShulkerBoxBlock),
            ((searchingFor, stack) -> {
                CompoundTag tag = stack.getSubTag("BlockEntityTag");
                if (tag != null && tag.contains("Items", 9)) {
                    ListTag items = tag.getList("Items", 10);
                    for (int i = 0; i < items.size(); i++) {
                        if (ItemStack.fromTag(items.getCompound(i)).getItem() == searchingFor) {
                            return true;
                        }
                    }
                }
                return false;
            })
        );
    }
}
