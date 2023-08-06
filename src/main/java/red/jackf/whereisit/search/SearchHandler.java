package red.jackf.whereisit.search;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Nameable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import red.jackf.whereisit.WhereIsIt;
import red.jackf.whereisit.api.SearchRequest;
import red.jackf.whereisit.api.SearchResult;
import red.jackf.whereisit.api.search.ConnectedBlocksGrabber;
import red.jackf.whereisit.config.WhereIsItConfig;
import red.jackf.whereisit.networking.ClientboundResultsPacket;
import red.jackf.whereisit.networking.ServerboundSearchForItemPacket;
import red.jackf.whereisit.util.RateLimiter;

import java.util.*;

public class SearchHandler {

    public static void handle(ServerboundSearchForItemPacket packet, ServerPlayer player, PacketSender response) {
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
        if (!packet.request().hasCriteria()) {
            WhereIsIt.LOGGER.warn("Empty request from {}", player.getGameProfile().getName());
            return;
        }

        WhereIsIt.LOGGER.debug("Server search id %d: %s".formatted(packet.id(), packet.request().toString()));

        var startTime = System.nanoTime();

        // do the search
        var startPos = player.blockPosition();
        var level = player.level();
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

                    var result = checkPosition(packet.request(), level, state, adjustedRoot);
                    if (result != null) {
                        results.put(adjustedRoot, result.withOtherPositions(connected));
                    }
                }
            }
        }

        WhereIsIt.LOGGER.debug("Server search results id %d: %s".formatted(packet.id(), results.toString()));

        // timing
        var time = System.nanoTime() - startTime;
        var timingStr = "Search time: %.2fms (%dns)".formatted((float) time / 1_000_000, time);
        WhereIsIt.LOGGER.debug(timingStr);
        if (WhereIsItConfig.INSTANCE.getConfig().getCommon().printSearchTime) player.sendSystemMessage(Component.literal("[Where Is It] " + timingStr).withStyle(ChatFormatting.YELLOW));

        // send to player
        if (!results.isEmpty())
            response.sendPacket(new ClientboundResultsPacket(packet.id(), results.values()));
    }

    /**
     * Checks a single position to see if it matches a search request
     * @param request Request to test against
     * @param level Level the test is in
     * @param state Block state at the given position
     * @param pos Position the test is at
     * @return A search result if there is a positive match, or null if not matching.
     */
    @SuppressWarnings("UnstableApiUsage")
    private static SearchResult checkPosition(SearchRequest request, Level level, BlockState state, BlockPos pos) {
        var checked = new HashSet<Storage<ItemVariant>>();
        for (var direction : Direction.values()) { // each side
            var storage = ItemStorage.SIDED.find(level, pos, direction);
            if (storage != null && !checked.contains(storage)) { // side valid and we haven't already tried this inv
                checked.add(storage);
                for (var view : storage) { // for each view in this side
                    if (view.isResourceBlank()) continue;
                    var resource = view.getResource().toStack((int) view.getAmount()); // get stack

                    if (SearchRequest.check(resource, request)) {
                        var result = SearchResult.builder(pos);
                        result.item(resource);
                        if (level.getBlockEntity(pos) instanceof Nameable nameable)
                            result.name(nameable.getCustomName(), null);
                        return result.build();
                    }
                }
            }
        }

        return null;
    }
}