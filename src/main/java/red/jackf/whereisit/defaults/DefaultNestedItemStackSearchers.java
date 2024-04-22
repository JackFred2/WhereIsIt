package red.jackf.whereisit.defaults;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;
import red.jackf.whereisit.api.search.NestedItemStackSearcher;

import java.util.Set;

public class DefaultNestedItemStackSearchers {
    static void setup() {
        setupShulkerBoxes();
        setupBundles();
    }

    private static void setupShulkerBoxes() {
        NestedItemStackSearcher.EVENT.register((source, predicate) -> {
            ItemContainerContents contents = source.get(DataComponents.CONTAINER);

            if (contents != null) {
                return contents.stream().anyMatch(predicate);
            } else {
                return false;
            }
        });
    }

    private static void setupBundles() {
        NestedItemStackSearcher.EVENT.register((source, predicate) -> {
            BundleContents contents = source.get(DataComponents.BUNDLE_CONTENTS);

            if (contents != null) {
                return contents.itemCopyStream().anyMatch(predicate);
            }
            return false;
        });
    }
}
