package red.jackf.whereisit.client.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import red.jackf.whereisit.api.SearchRequest;
import red.jackf.whereisit.api.SearchResult;
import red.jackf.whereisit.client.WhereIsItClient;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * Initiates a search for a given request. Can be something like asking the server, or checking a local cache. Allows for
 * custom handling of results.
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
     * Initiates a search request with Where Is It's default handling - showing a fading overlay over the positions.
     *
     * @param request Request to search with
     * @return Whether any search methods succeeded in starting. This will return true even if no items were found but
     * a request was started.
     */
    @SuppressWarnings("UnusedReturnValue")
    static boolean doSearch(SearchRequest request) {
        return WhereIsItClient.doSearch(request);
    }

    /**
     * Process a search request. For examples, see {@link red.jackf.whereisit.client.defaults.SearchInvokerDefaults} which
     * sets up a network channel to ask the server, or Chest Tracker, which queries the local memory.
     *
     * @param request Request to search using
     * @param resultConsumer Callback for successful results; should be called when
     * @return if the request was successfully started; not necessarily finished.
     */
    boolean search(SearchRequest request, Consumer<Collection<SearchResult>> resultConsumer);
}
