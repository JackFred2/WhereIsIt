package red.jackf.whereisit.client.api.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import red.jackf.whereisit.api.SearchResult;

import java.util.Collection;

/**
 * <p>Allows you to hook in to the results of any search, for example if you wanted to add additional markers to found
 * results.</p>
 */
public interface OnResult {
    Event<OnResult> EVENT = EventFactory.createArrayBacked(OnResult.class, invokers -> results -> {
        for (OnResult invoker : invokers) {
            invoker.onResults(results);
        }
    });

    void onResults(Collection<SearchResult> results);
}
