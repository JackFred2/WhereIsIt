package red.jackf.whereisit.defaults;

import net.minecraft.core.NonNullList;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.*;
import red.jackf.whereisit.api.search.NestedItemStackSearcher;

import java.util.Set;

public class DefaultNestedItemStackSearchers {
    public static void setup() {
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

        NestedItemStackSearcher.EVENT.register((source, predicate) -> {
            if (!shulkerBoxes.contains(source.getItem())) return false;

            var beTag = BlockItem.getBlockEntityData(source);
            if (beTag == null) return false;

            var items = NonNullList.withSize(27, ItemStack.EMPTY);
            ContainerHelper.loadAllItems(beTag, items);

            for (var item : items)
                if (predicate.test(item)) return true;

            return false;
        });
    }

    private static void setupBundles() {
        NestedItemStackSearcher.EVENT.register((source, predicate) -> {
            if (!source.is(Items.BUNDLE)) return false;

            return BundleItem.getContents(source).anyMatch(predicate);
        });
    }
}
