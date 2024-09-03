package red.jackf.whereisit.defaults;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import red.jackf.whereisit.api.search.NestedItemsGrabber;

import java.util.Set;
import java.util.stream.Stream;

public class DefaultNestedItemsGrabbers {
    static void setup() {
        setupShulkerBoxes();
        setupBundles();
    }

    private static void setupShulkerBoxes() {
        Set<Item> shulkerBoxes = Set.of(
                Items.SHULKER_BOX,
                Items.WHITE_SHULKER_BOX,
                Items.LIGHT_GRAY_SHULKER_BOX,
                Items.GRAY_SHULKER_BOX,
                Items.BLACK_SHULKER_BOX,
                Items.RED_SHULKER_BOX,
                Items.ORANGE_SHULKER_BOX,
                Items.YELLOW_SHULKER_BOX,
                Items.LIME_SHULKER_BOX,
                Items.GREEN_SHULKER_BOX,
                Items.CYAN_SHULKER_BOX,
                Items.LIGHT_BLUE_SHULKER_BOX,
                Items.BLUE_SHULKER_BOX,
                Items.PURPLE_SHULKER_BOX,
                Items.MAGENTA_SHULKER_BOX,
                Items.PINK_SHULKER_BOX,
                Items.BROWN_SHULKER_BOX
        );

        NestedItemsGrabber.EVENT.register(source -> {
            if (!shulkerBoxes.contains(source.getItem())) return Stream.empty();

            CompoundTag beTag = BlockItem.getBlockEntityData(source);
            if (beTag == null) return Stream.empty();

            NonNullList<ItemStack> items = NonNullList.withSize(ShulkerBoxBlockEntity.CONTAINER_SIZE, ItemStack.EMPTY);
            ContainerHelper.loadAllItems(beTag, items);

            return items.stream().filter(stack -> !stack.isEmpty());
        });
    }

    private static void setupBundles() {
        NestedItemsGrabber.EVENT.register(BundleItem::getContents);
    }
}
