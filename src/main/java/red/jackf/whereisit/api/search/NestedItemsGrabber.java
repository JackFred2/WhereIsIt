package red.jackf.whereisit.api.search;


import com.google.common.collect.Lists;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.item.ItemStack;
import red.jackf.whereisit.api.EventPhases;

import java.util.List;
import java.util.stream.Stream;

/**
 * Recursively gets a list of all items that are directly contained within another item, such as Shulker Boxes, bundles,
 * or backpacks. Not meant to be used with indirect storage, such as 'ender pouches' or remote terminals.
 */
public interface NestedItemsGrabber {
    /**
     * Get a stream of all items directly contained within this item.
     *
     * @param source ItemStack to pull items from.
     * @return Stream of items that are contained within this stack.
     */
    static Stream<ItemStack> get(ItemStack source) {
        return EVENT.invoker().grab(source);
    }

    Event<NestedItemsGrabber> EVENT = EventFactory.createWithPhases(NestedItemsGrabber.class, listeners -> stack -> {
        List<ItemStack> result = Lists.newArrayList();

        for (NestedItemsGrabber listener : listeners) {
            listener.grab(stack).forEach(nested -> {
                result.add(nested);
                result.addAll(get(nested).toList());
            });
        }

        return result.stream();
    }, EventPhases.PRIORITY, EventPhases.DEFAULT, EventPhases.FALLBACK);

    /**
     * Pulls a stream of item stacks from a source stack. Should return a {@link Stream#empty()} if none are contained.
     *
     * @param source ItemStack to pull items from.
     * @return Stream of items that are contained within this stack.
     */
    Stream<ItemStack> grab(ItemStack source);
}
