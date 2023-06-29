package red.jackf.whereisit.search;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import red.jackf.whereisit.WhereIsIt;
import red.jackf.whereisit.api.SearchResult;
import red.jackf.whereisit.networking.ClientboundResultsPacket;
import red.jackf.whereisit.networking.ServerboundSearchForItemPacket;

import java.util.HashSet;

public class SearchHandler {
    private static final int RANGE = 8;

    @SuppressWarnings("UnstableApiUsage")
    public static void handle(ServerboundSearchForItemPacket packet, ServerPlayer player, PacketSender response) {
        var startPos = player.blockPosition();
        var level = player.level();
        var pos = new BlockPos.MutableBlockPos();
        var results = new HashSet<SearchResult>();
        WhereIsIt.LOGGER.debug("Server search id %d: %s".formatted(packet.id(), packet.request().toString()));
        for (int x = startPos.getX() - RANGE; x <= startPos.getX() + RANGE; x++) {
            pos.setX(x);
            for (int y = startPos.getY() - RANGE; y <= startPos.getY() + RANGE; y++) {
                pos.setY(y);
                for (int z = startPos.getZ() - RANGE; z <= startPos.getZ() + RANGE; z++) {
                    pos.setZ(z);
                    invCheck:
                    for (var direction : Direction.values()) {
                        var storage = ItemStorage.SIDED.find(level, pos, direction);
                        if (storage != null) for (var view : storage) {
                            var resource = view.getResource().toStack((int) view.getAmount());
                            if (packet.request().test(resource)) {
                                results.add(new SearchResult(pos.immutable(), resource));
                                break invCheck;
                            }
                        }
                    }
                }
            }
        }
        WhereIsIt.LOGGER.debug("Server search results id %d: %s".formatted(packet.id(), results.toString()));
        if (results.size() > 0)
            response.sendPacket(new ClientboundResultsPacket(packet.id(), results));
    }
}