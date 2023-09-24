package red.jackf.whereisit.client.api.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface OnResultsCleared {
    Event<OnResultsCleared> EVENT = EventFactory.createArrayBacked(OnResultsCleared.class, invokers -> () -> {
        for (OnResultsCleared invoker : invokers) {
            invoker.onResultsCleared();
        }
    });

    void onResultsCleared();
}
