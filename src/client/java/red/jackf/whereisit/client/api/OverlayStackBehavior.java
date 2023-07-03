package red.jackf.whereisit.client.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.item.ItemStack;
import red.jackf.whereisit.api.criteria.Criterion;

import java.util.function.Consumer;

public interface OverlayStackBehavior {
    /**
     * Takes an ItemStack when selected from an Item Overlay, and adds relevant criterion to the request. This allows
     * for custom search behavior, such as enchanted books looking for enchantments instead.
     * This event should return true if the behavior was triggered, short-circuiting the event.
     */
    Event<OverlayStackBehavior> EVENT = EventFactory.createArrayBacked(OverlayStackBehavior.class, listeners -> (consumer, stack, alternate) -> {
        for (OverlayStackBehavior listener : listeners)
            if (listener.processOverlayStackBehavior(consumer, stack, alternate)) return true;
        return false;
    });

    boolean processOverlayStackBehavior(Consumer<Criterion> consumer, ItemStack stack, boolean alternate);
}
