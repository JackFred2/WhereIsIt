package red.jackf.whereisit.defaults;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;
import red.jackf.whereisit.api.search.NestedItemsGrabber;

import java.util.Set;
import java.util.stream.Stream;

public class DefaultNestedItemStackSearchers {
    static void setup() {
        setupShulkerBoxes();
        setupBundles();
    }

    private static void setupShulkerBoxes() {
        NestedItemsGrabber.EVENT.register(source -> {
            ItemContainerContents contents = source.get(DataComponents.CONTAINER);

            if (contents != null) {
                return contents.stream();
            }
            return Stream.empty();
        });
    }

    private static void setupBundles() {
        NestedItemsGrabber.EVENT.register((source) -> {
            ItemContainerContents contents = source.get(DataComponents.CONTAINER);

            if (contents != null) {
                return contents.stream();
            }
            return Stream.empty();
        });
    }
}
