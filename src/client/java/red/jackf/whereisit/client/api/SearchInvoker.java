package red.jackf.whereisit.client.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import red.jackf.whereisit.api.SearchRequest;
import red.jackf.whereisit.api.SearchResult;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * Initiates a search for a given request. Can be something like asking the server, or checking a local cache.
 */
public interface SearchInvoker {
    Event<SearchInvoker> EVENT = EventFactory.createArrayBacked(SearchInvoker.class, listeners -> (request, resultConsumer) -> {
        boolean hasAnySucceeded = false;
        for (SearchInvoker invoker : listeners) {
            hasAnySucceeded |= invoker.search(request, resultConsumer);
        }
        // none have triggered, most likely the mod is not on the server and there are no other handlers
        return hasAnySucceeded;
    });

    /**
     * @return if the request was successfully started; not necessarily finished.
     */
    boolean search(SearchRequest request, Consumer<Collection<SearchResult>> resultConsumer);
}
