package red.jackf.whereisit.compat;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import red.jackf.whereisit.FoundType;
import red.jackf.whereisit.api.CustomItemBehavior;
import red.jackf.whereisit.api.CustomWorldBehavior;
import red.jackf.whereisit.api.InventoryUtils;
import red.jackf.whereisit.api.WhereIsItEntrypoint;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class VanillaItemBehaviors implements WhereIsItEntrypoint {
    private static final Map<BlockState, Boolean> validInventoryLookups = new HashMap<>();
    private static final Map<BlockState, Boolean> validInventoryProviderLookups = new HashMap<>();
    private static final Map<BlockState, Boolean> validLecternLookups = new HashMap<>();

    @Override
    public void setupWorldBehaviors(Map<Predicate<BlockState>, CustomWorldBehavior> behaviors) {

        // Behaviors for standard inventories - Chests, hoppers, anything that implements
        // {@link net.minecraft.inventory.Inventory}
        behaviors.put(
            blockState ->
                validInventoryLookups.computeIfAbsent(blockState,
                    (blockState1 -> blockState1.getBlock() instanceof BlockWithEntity)),
            (searchingFor, state, pos, world) -> {
                BlockEntity be = world.getBlockEntity(pos);
                if (be instanceof Inventory) {
                    return InventoryUtils.invContains((Inventory) be, searchingFor);
                }
                return FoundType.NOT_FOUND;
            }
        );

        // Behaviors for miscellaneous inventories - Compostors, LinkedStorage chests
        // Anything that implements {@link net.minecraft.block.InventoryProvider}
        behaviors.put(
            blockState ->
                validInventoryProviderLookups.computeIfAbsent(blockState,
                    (blockState1 -> blockState1.getBlock() instanceof InventoryProvider)),
            (searchingFor, state, pos, world) -> {
                Inventory inv = ((InventoryProvider) state.getBlock()).getInventory(state, world, pos);
                if (inv == null) return FoundType.NOT_FOUND;
                return InventoryUtils.invContains(inv, searchingFor);
            }
        );

        // Lecterns
        behaviors.put(
            blockState ->
                validLecternLookups.computeIfAbsent(blockState,
                    blockState1 -> blockState.getBlock() instanceof LecternBlock && blockState1.get(LecternBlock.HAS_BOOK)),
            ((searchingFor, state, pos, world) -> {
                BlockEntity be = world.getBlockEntity(pos);
                if (be instanceof LecternBlockEntity) {
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

    @Override
    public int getPriority() {
        return -1000;
    }
}
