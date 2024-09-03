package red.jackf.whereisit.defaults;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;
import red.jackf.whereisit.api.search.NestedItemsGrabber;

import java.util.stream.Stream;

public class DefaultNestedItemsGrabbers {
    static void setup() {
        setupContainers();
        setupBundles();
    }

    private static void setupContainers() {
        NestedItemsGrabber.EVENT.register(source -> {
            ItemContainerContents container = source.get(DataComponents.CONTAINER);
            if (container == null) return Stream.empty();

            return container.nonEmptyStream();
        });
    }

    private static void setupBundles() {
        NestedItemsGrabber.EVENT.register(source -> {
            BundleContents contents = source.get(DataComponents.BUNDLE_CONTENTS);
            if (contents == null) return Stream.empty();

            return contents.itemCopyStream().filter(stack -> !stack.isEmpty());
        });
    }
}
