package red.jackf.whereisit.compat;

import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.LecternBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.collection.DefaultedList;
import red.jackf.whereisit.FoundType;
import red.jackf.whereisit.api.CustomItemBehavior;
import red.jackf.whereisit.api.CustomWorldBehavior;
import red.jackf.whereisit.api.InventoryUtils;
import red.jackf.whereisit.api.WhereIsItEntrypoint;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class VanillaItemBehaviors implements WhereIsItEntrypoint {

    @Override
    public void setupWorldBehaviors(Map<BiPredicate<BlockState, BlockEntity>, CustomWorldBehavior> behaviors) {

        // Behaviors for standard inventories - Chests, hoppers, anything that implements
        // {@link net.minecraft.inventory.Inventory}
        behaviors.put(
            ((blockState, blockEntity) -> blockEntity instanceof Inventory),
            ((searchingFor, state, pos, world) -> {
                BlockEntity be = world.getBlockEntity(pos);
                if (be instanceof Inventory) {
                    return InventoryUtils.invContains((Inventory) be, searchingFor);
                }
                return FoundType.NOT_FOUND;
            })
        );

        // Lecterns
        behaviors.put(
            ((blockState, blockEntity) -> blockState.getBlock() instanceof LecternBlock && blockState.get(LecternBlock.HAS_BOOK)),
            ((searchingFor, state, pos, world) -> {
                BlockEntity be = world.getBlockEntity(pos);
                if (be instanceof LecternBlockEntity) {
                    System.out.println("Lectern");
                    return InventoryUtils.itemContains(((LecternBlockEntity) be).getBook(), searchingFor);
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
