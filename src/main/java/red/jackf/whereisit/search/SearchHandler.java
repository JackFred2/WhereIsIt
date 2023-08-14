package red.jackf.whereisit.search;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import red.jackf.whereisit.WhereIsIt;
import red.jackf.whereisit.api.SearchRequest;
import red.jackf.whereisit.api.SearchResult;
import red.jackf.whereisit.api.search.BlockSearcher;
import red.jackf.whereisit.api.search.ConnectedBlocksGrabber;
import red.jackf.whereisit.config.WhereIsItConfig;
import red.jackf.whereisit.networking.ClientboundResultsPacket;
import red.jackf.whereisit.networking.ServerboundSearchForItemPacket;
import red.jackf.whereisit.serverside.ServerSideRenderer;
import red.jackf.whereisit.util.RateLimiter;

import java.util.Collection;
import java.util.HashMap;
import java.util.function.Consumer;

public class SearchHandler {

    public static void handleFromPacket(ServerboundSearchForItemPacket packet, ServerPlayer player, PacketSender responder) {
        handle(packet.request(), player, results -> responder.sendPacket(new ClientboundResultsPacket(packet.id(), results)));
    }

    public static void handle(SearchRequest request, ServerPlayer player, Consumer<Collection<SearchResult>> resultConsumer) {
        // check rate limit for players
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER && WhereIsItConfig.INSTANCE.getConfig().getServer().rateLimit) {
            //don't close the level
            //noinspection resource
            var time = player.level().getGameTime();
            if (RateLimiter.rateLimited(player, time)) {
                player.sendSystemMessage(Component.literal("[WhereIsIt] Slow down!").withStyle(ChatFormatting.RED));
                return;
            }

            RateLimiter.add(player, time);
        }

        // empty requests
        if (!request.hasCriteria()) {
            WhereIsIt.LOGGER.warn("Empty request from {}", player.getGameProfile().getName());
            return;
        }

        WhereIsIt.LOGGER.debug("Server search for {}: {}", player.getScoreboardName(), request);

        var startTime = System.nanoTime();

        // do the search
        var startPos = player.blockPosition();
        var level = (ServerLevel) player.level();
        var pos = new BlockPos.MutableBlockPos();
        var results = new HashMap<BlockPos, SearchResult>();
        var range = WhereIsItConfig.INSTANCE.getConfig().getCommon().searchRangeBlocks;
        var maxRange = range * range;
        for (int x = startPos.getX() - range; x <= startPos.getX() + range; x++) {
            pos.setX(x);
            for (int y = startPos.getY() - range; y <= startPos.getY() + range; y++) {
                pos.setY(y);
                for (int z = startPos.getZ() - range; z <= startPos.getZ() + range; z++) {
                    pos.setZ(z);
                    if (pos.distSqr(startPos) > maxRange) continue;

                    var state = level.getBlockState(pos);

                    var connected = ConnectedBlocksGrabber.getConnected(level, state, pos);
                    var adjustedRoot = connected.get(0);

                    if (results.containsKey(adjustedRoot)) continue;

                    var result = BlockSearcher.EVENT.invoker().searchPosition(request, player, level, state, adjustedRoot);
                    if (result.hasValue()) {
                        results.put(adjustedRoot, result.get().withOtherPositions(connected));
                    }
                }
            }
        }

        WhereIsIt.LOGGER.debug("Server search results for {}: {}", player.getScoreboardName(), results);

        // timing
        var time = System.nanoTime() - startTime;
        var timingStr = "Search time: %.2fms (%dns)".formatted((float) time / 1_000_000, time);
        WhereIsIt.LOGGER.debug(timingStr);
        if (WhereIsItConfig.INSTANCE.getConfig().getCommon().printSearchTime) player.sendSystemMessage(Component.literal("[Where Is It] " + timingStr).withStyle(ChatFormatting.YELLOW));

        // send to player
        if (!results.isEmpty()) {
            if (WhereIsItConfig.INSTANCE.getConfig().getCommon().forceServerSideHighlightsOnly) {
                ServerSideRenderer.send(player, results.values());
            } else {
                resultConsumer.accept(results.values());
            }
        }
    }
}