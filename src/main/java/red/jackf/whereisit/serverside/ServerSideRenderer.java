package red.jackf.whereisit.serverside;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import red.jackf.jackfredlib.api.colour.Colour;
import red.jackf.jackfredlib.api.lying.Debris;
import red.jackf.jackfredlib.api.lying.Lies;
import red.jackf.jackfredlib.api.lying.entity.EntityLie;
import red.jackf.jackfredlib.api.lying.entity.builders.EntityBuilders;
import red.jackf.whereisit.WhereIsIt;
import red.jackf.whereisit.api.SearchResult;
import red.jackf.whereisit.config.WhereIsItConfig;
import red.jackf.whereisit.networking.ClientboundResultsPacket;

import java.util.Collection;

import static net.minecraft.SharedConstants.TICKS_PER_SECOND;

/**
 * Handles sending results to a player which may not have the mod installed client side
 */
public class ServerSideRenderer {
    public static void send(ServerPlayer player, Collection<SearchResult> results) {
        if (WhereIsItConfig.INSTANCE.getConfig().getCommon().forceServerSideHighlightsOnly
                || !ServerPlayNetworking.canSend(player, ClientboundResultsPacket.TYPE)) {
            doServersideRendering(player, results);
        } else {
            ServerPlayNetworking.send(player, new ClientboundResultsPacket(ClientboundResultsPacket.WHEREIS_COMMAND_ID, results));
        }
    }

    private static EntityLie makeBlockDisplay(ServerLevel level, BlockPos pos, Colour colour) {
        return EntityLie.builder(EntityBuilders.blockDisplay(level)
                        .positionCentered(pos)
                        .state(Blocks.STRUCTURE_BLOCK.defaultBlockState())
                        .scaleAndCenter(0.5f)
                        .glowing(true, colour)
                        .build())
                .onLeftClick((activeLie, shiftDown, relativeToEntity) -> activeLie.fade())
                .onRightClick((activeLie, shiftDown, hand, relativeToEntity) -> activeLie.fade())
                .build();
    }

    private static int randomFadeTime() {
        var baseTime = WhereIsItConfig.INSTANCE.getConfig().getCommon().serverSideHighlightFadeTime;
        int random = (int) ((4 * TICKS_PER_SECOND) * Math.random() - (2 * TICKS_PER_SECOND));
        return baseTime + random;
    }

    private static void doServersideRendering(ServerPlayer player, Collection<SearchResult> results) {
        WhereIsIt.LOGGER.debug("Doing server-side rendering for {}", player.getScoreboardName());
        var level = (ServerLevel) player.level();

        for (SearchResult result : results) {
            var colour = Colour.fromHSV((float) Math.random(), 1, 1);
            var mainLie = makeBlockDisplay(level, result.pos(), colour);

            Debris.INSTANCE.schedule(Lies.INSTANCE.addEntity(player, mainLie), randomFadeTime());

            for (BlockPos otherPos : result.otherPositions()) {
                Debris.INSTANCE.schedule(Lies.INSTANCE.addEntity(player, makeBlockDisplay(level, otherPos, colour)), randomFadeTime());
            }
        }
    }
}
