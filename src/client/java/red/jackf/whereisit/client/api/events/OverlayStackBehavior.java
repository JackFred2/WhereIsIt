package red.jackf.whereisit.client.api.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.item.ItemStack;
import red.jackf.whereisit.api.criteria.Criterion;

import java.util.function.Consumer;

/**
 * <p>This allows you to use Recipe Viewer (JEI, REI, EMI) stacks as custom triggers for searches. For example, Where Is It
 * overrides behavior for enchanted books in order to search for enchantments directly.</p>
 *
 * <p>For examples, see {@link red.jackf.whereisit.client.defaults.OverlayStackBehaviorDefaults}</p>
 */
public interface OverlayStackBehavior {
    /**
     * <p>Takes an ItemStack when selected from an Item Overlay, and adds relevant criterion to the request. This allows
     * for custom search behavior, such as enchanted books looking for enchantments instead.</p>
     *
     * <p>This event should return true if the behavior was triggered, short-circuiting the event and preventing standard
     * grabbing behavior.</p>
     */
    Event<OverlayStackBehavior> EVENT = EventFactory.createArrayBacked(OverlayStackBehavior.class, listeners -> (consumer, stack, alternate) -> {
        for (OverlayStackBehavior listener : listeners)
            if (listener.processOverlayStackBehavior(consumer, stack, alternate)) return true;
        return false;
    });

    /**
     * Check if this handler should apply to the given stack, and if so create specific criterion for the request.
     * @param consumer Callback to send generated criterion to.
     * @param stack ItemStack to check and generate criterion from
     * @param alternate Alternate behavior; largely arbitrary, true if the user is holding shift. Allows you to specify
     *                  other criteria if needed.
     * @return True if this behavior was triggered, false otherwise.
     */
    boolean processOverlayStackBehavior(Consumer<Criterion> consumer, ItemStack stack, boolean alternate);
}
