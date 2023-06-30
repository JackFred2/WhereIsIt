package red.jackf.whereisit.search;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import red.jackf.whereisit.WhereIsIt;
import red.jackf.whereisit.api.SearchRequest;
import red.jackf.whereisit.api.SearchResult;
import red.jackf.whereisit.api.search.NestedItemStackSearcher;
import red.jackf.whereisit.config.WhereIsItConfig;
import red.jackf.whereisit.networking.ClientboundResultsPacket;
import red.jackf.whereisit.networking.ServerboundSearchForItemPacket;

import java.util.HashSet;

public class SearchHandler {

    public static void handle(ServerboundSearchForItemPacket packet, ServerPlayer player, PacketSender response) {
        var startPos = player.blockPosition();
        var level = player.level();
        var pos = new BlockPos.MutableBlockPos();
        var results = new HashSet<SearchResult>();
        var range = WhereIsItConfig.INSTANCE.getConfig().common.searchRangeBlocks;
        WhereIsIt.LOGGER.debug("Server search id %d: %s".formatted(packet.id(), packet.request().toString()));
        for (int x = startPos.getX() - range; x <= startPos.getX() + range; x++) {
            pos.setX(x);
            for (int y = startPos.getY() - range; y <= startPos.getY() + range; y++) {
                pos.setY(y);
                for (int z = startPos.getZ() - range; z <= startPos.getZ() + range; z++) {
                    pos.setZ(z);
                    if (pos.distSqr(startPos) > range * range) continue;
                    checkPosition(packet.request(), level, pos, results);
                }
            }
        }
        WhereIsIt.LOGGER.debug("Server search results id %d: %s".formatted(packet.id(), results.toString()));
        if (results.size() > 0)
            response.sendPacket(new ClientboundResultsPacket(packet.id(), results));
    }

    // check all items from a given world position, and returns if any match
    @SuppressWarnings("UnstableApiUsage")
    private static void checkPosition(SearchRequest request, Level level, BlockPos.MutableBlockPos pos, HashSet<SearchResult> results) {
        var checked = new HashSet<Storage<ItemVariant>>();
        for (var direction : Direction.values()) {
            var storage = ItemStorage.SIDED.find(level, pos, direction);
            if (storage != null && !checked.contains(storage)) {
                checked.add(storage);
                for (var view : storage) {
                    if (view.isResourceBlank()) continue;
                    var resource = view.getResource().toStack((int) view.getAmount());
                    if (NestedItemStackSearcher.check(resource, request)) {
                        results.add(new SearchResult(pos.immutable(), resource));
                    }
                }
            }
        }
    }
}