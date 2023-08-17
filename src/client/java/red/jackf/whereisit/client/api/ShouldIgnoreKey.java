package red.jackf.whereisit.client.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

import java.util.Arrays;

/**
 * <p>Provides a hook to check if a key press should be ignored, due to a text field being selected.</p>
 *
 * <p>For examples, see {@link red.jackf.whereisit.client.defaults.ShouldIgnoreKeyDefaults}, which handles vanilla edit
 * boxes, or any recipe viewer plugin in {@link red.jackf.whereisit.client.compat.recipeviewers}, which handle their
 * search bars.</p>
 */
public interface ShouldIgnoreKey {
    Event<ShouldIgnoreKey> EVENT = EventFactory.createArrayBacked(ShouldIgnoreKey.class, listeners -> () ->
            Arrays.stream(listeners).anyMatch(ShouldIgnoreKey::shouldIgnoreKey)
    );

    /**
     * Check if a keybinding should be ignored at this moment, due to being used by another mod.
     * @return If a key press should be ignored.
     */
    boolean shouldIgnoreKey();
}
