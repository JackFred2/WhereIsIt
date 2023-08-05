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
import red.jackf.whereisit.WhereIsIt;
import red.jackf.whereisit.api.SearchRequest;
import red.jackf.whereisit.api.SearchResult;
import red.jackf.whereisit.config.WhereIsItConfig;
import red.jackf.whereisit.networking.ClientboundResultsPacket;
import red.jackf.whereisit.networking.ServerboundSearchForItemPacket;
import red.jackf.whereisit.util.RateLimiter;

import java.util.HashSet;

public class SearchHandler {

    public static void handle(ServerboundSearchForItemPacket packet, ServerPlayer player, PacketSender response) {
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

        if (!packet.request().hasCriteria()) {
            WhereIsIt.LOGGER.warn("Empty request from {}", player.getGameProfile().getName());
            return;
        }

        var startTime = System.nanoTime();

        var startPos = player.blockPosition();
        var level = player.level();
        var pos = new BlockPos.MutableBlockPos();
        var results = new HashSet<SearchResult>();
        var range = WhereIsItConfig.INSTANCE.getConfig().getCommon().searchRangeBlocks;
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
        var time = System.nanoTime() - startTime;
        var timingStr = "Search time: %.2fms (%dns)".formatted((float) time / 1_000_000, time);
        WhereIsIt.LOGGER.debug(timingStr);
        if (WhereIsItConfig.INSTANCE.getConfig().getCommon().printSearchTime) player.sendSystemMessage(Component.literal("[Where Is It] " + timingStr).withStyle(ChatFormatting.YELLOW));

        if (!results.isEmpty())
            response.sendPacket(new ClientboundResultsPacket(packet.id(), results));
    }

    // check all items from a given world position, and returns if any match
    @SuppressWarnings("UnstableApiUsage")
    private static void checkPosition(SearchRequest request, Level level, BlockPos.MutableBlockPos pos, HashSet<SearchResult> results) {
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
                        results.add(result.build());
                        return;
                    }
                }
            }
        }
    }
}