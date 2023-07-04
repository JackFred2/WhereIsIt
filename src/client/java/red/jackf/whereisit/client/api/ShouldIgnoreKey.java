package red.jackf.whereisit.client.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

import java.util.Arrays;

/**
 * Returns true if the search key should be ignored right now. Usually indicates a search bar is in use.
 */
public interface ShouldIgnoreKey {
    Event<ShouldIgnoreKey> EVENT = EventFactory.createArrayBacked(ShouldIgnoreKey.class, listeners -> () ->
            Arrays.stream(listeners).anyMatch(ShouldIgnoreKey::shouldIgnoreKey)
    );

    boolean shouldIgnoreKey();
}
