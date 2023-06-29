package red.jackf.whereisit.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import red.jackf.whereisit.api.SearchResult;
import red.jackf.whereisit.client.api.SearchInvoker;
import red.jackf.whereisit.networking.ClientboundResultsPacket;
import red.jackf.whereisit.networking.ServerboundSearchForItemPacket;

import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * If WhereIsIt is installed on the server, asks it for nearby items.
 */
public class DefaultSearchInvoker {
    private static final AtomicLong packetCounter = new AtomicLong(0);
    private static final ConcurrentMap<Long, Consumer<Collection<SearchResult>>> consumers = new ConcurrentHashMap<>();

    private static final Timer consumerCleanupTimer = new Timer("WhereIsIt Network Consumer Cleanup", true);
    private static final Long CONSUMER_CLEANUP_DELAY = 10_000L; // milliseconds

    private static TimerTask removeFromConsumerMap(long id) {
        return new TimerTask() {
            @Override
            public void run() {
                consumers.remove(id);
            }
        };
    }

    public static void setup() {
        SearchInvoker.EVENT.register((request, resultConsumer) -> {
            if (ClientPlayNetworking.canSend(ServerboundSearchForItemPacket.TYPE)) {
                var id = packetCounter.incrementAndGet();
                ClientPlayNetworking.send(new ServerboundSearchForItemPacket(id, request));
                consumers.put(id, resultConsumer);

                // prevent requests piling up
                consumerCleanupTimer.schedule(removeFromConsumerMap(id), CONSUMER_CLEANUP_DELAY);

                return true;
            } else {
                return false;
            }
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> { // on server connect
            ClientPlayNetworking.registerGlobalReceiver(ClientboundResultsPacket.TYPE, (packet, player, responseSender) -> {
                var consumer = consumers.remove(packet.id());
                if (consumer != null) consumer.accept(packet.results());
            });
        });
    }
}
