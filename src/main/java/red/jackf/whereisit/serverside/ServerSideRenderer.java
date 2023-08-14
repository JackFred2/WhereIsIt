package red.jackf.whereisit.serverside;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import red.jackf.jackfredlib.api.colour.Colour;
import red.jackf.jackfredlib.api.lying.ActiveLie;
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
    private static final Multimap<GameProfile, ActiveLie<EntityLie>> playerHighlightLies = ArrayListMultimap.create();

    public static void fadeServerSide(ServerPlayer player) {
        playerHighlightLies.removeAll(player.getGameProfile()).forEach(ActiveLie::fade);
    }

    public static void send(ServerPlayer player, Collection<SearchResult> results) {
        if (WhereIsItConfig.INSTANCE.getConfig().getCommon().forceServerSideHighlightsOnly
                || !ServerPlayNetworking.canSend(player, ClientboundResultsPacket.TYPE)) {
            // draw server side highlights
            doServersideRendering(player, results);
        } else {

            // send to player via packet
            ServerPlayNetworking.send(player, new ClientboundResultsPacket(ClientboundResultsPacket.WHEREIS_COMMAND_ID, results));
        }
    }

    private static EntityLie makeAlternateBlockDisplay(ServerLevel level, BlockPos pos, Colour colour) {
        return EntityLie.builder(EntityBuilders.blockDisplay(level)
                        .positionCentered(pos)
                        .state(Blocks.JIGSAW.defaultBlockState())
                        .scaleAndCenter(0.7f)
                        .glowing(true, colour)
                        .build())
                .onFade(activeLie -> playerHighlightLies.get(activeLie.player().getGameProfile()).remove(activeLie))
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

            var timeout = randomFadeTime();

            var mainEntity = EntityBuilders.blockDisplay(level)
                    .position(Vec3.atBottomCenterOf(result.pos().above())
                            .add(result.name() != null ? result.nameOffset().subtract(0, 1, 0) : Vec3.ZERO))
                    .state(Blocks.STRUCTURE_BLOCK.defaultBlockState())
                    .scaleAndCenter(0.7f)
                    .addTranslation(new Vector3f(0, -0.5f, 0))
                    .glowing(true, colour);

            if (result.name() != null) {
                mainEntity.customName(result.name()).alwaysRenderName(true);
                mainEntity.addTranslation(result.nameOffset().subtract(0, 1, 0).reverse().toVector3f());
            }

            var active = Lies.INSTANCE.addEntity(player, EntityLie.builder(mainEntity.build())
                    .onFade(activeLie -> playerHighlightLies.get(activeLie.player().getGameProfile()).remove(activeLie))
                    .build());
            playerHighlightLies.put(player.getGameProfile(), active);
            Debris.INSTANCE.schedule(active, timeout);

            for (BlockPos otherPos : result.otherPositions()) {
                var proxy = Lies.INSTANCE.addEntity(player, makeAlternateBlockDisplay(level, otherPos, colour));
                playerHighlightLies.put(player.getGameProfile(), proxy);
                Debris.INSTANCE.schedule(proxy, timeout);
            }
        }
    }
}
