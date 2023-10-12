package red.jackf.whereisit.serverside;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import red.jackf.jackfredlib.api.colour.Colour;
import red.jackf.jackfredlib.api.lying.Debris;
import red.jackf.jackfredlib.api.lying.Lie;
import red.jackf.jackfredlib.api.lying.entity.EntityLie;
import red.jackf.jackfredlib.api.lying.entity.builders.EntityBuilders;
import red.jackf.jackfredlib.api.lying.entity.builders.display.BlockDisplayBuilder;
import red.jackf.whereisit.WhereIsIt;
import red.jackf.whereisit.api.SearchResult;
import red.jackf.whereisit.config.WhereIsItConfig;

import java.util.Collection;

import static net.minecraft.SharedConstants.TICKS_PER_SECOND;

/**
 * Handles sending results to a player which may not have the mod installed client side
 */
public class ServerSideRenderer {
    private static final Block MAIN_BLOCK = Blocks.STRUCTURE_BLOCK;
    private static final Block ALT_BLOCK = Blocks.JIGSAW;
    private static final float HIGHLIGHT = 0.7f;
    private static final Multimap<GameProfile, EntityLie<Display.BlockDisplay>> playerHighlightLies = MultimapBuilder.hashKeys()
            .arrayListValues().build();

    /**
     * Fade all server-side lies for a player
     *
     * @param player Player to fade lies for
     */
    public static void fadeServerSide(ServerPlayer player) {
        playerHighlightLies.removeAll(player.getGameProfile()).forEach(Lie::fade);
    }

    /**
     * Build an entity lie to show the given player, using ALT_BLOCK as the block.
     *
     * @param level  Level to place the lie in
     * @param pos    Position to place the lie at
     * @param colour Colour of the lie
     * @return Built entity lie at the given position
     */
    private static Display.BlockDisplay makeAlternateBlockDisplay(ServerLevel level, BlockPos pos, Colour colour) {
        return EntityBuilders.blockDisplay(level).positionCentered(pos).state(ALT_BLOCK.defaultBlockState())
                .scaleAndCenter(HIGHLIGHT).glowing(true, colour).build();
    }

    /**
     * Return a random fade time, centered on the config time with some jitter
     *
     * @return Randomly generated fade time
     */
    private static int randomFadeTime() {
        int baseTime = WhereIsItConfig.INSTANCE.instance().getCommon().fadeoutTimeTicks;
        int random = (int) ((4 * TICKS_PER_SECOND) * Math.random() - (2 * TICKS_PER_SECOND));
        return baseTime + random;
    }

    /**
     * Render a set of results to the player, using JackFredLib's lying module
     *
     * @param player  Player to render server-side results for
     * @param results Results to render
     */
    public static void doServersideRendering(ServerPlayer player, Collection<SearchResult> results) {
        WhereIsIt.LOGGER.debug("Doing server-side rendering for {}", player.getScoreboardName());
        var level = (ServerLevel) player.level();

        for (SearchResult result : results) {
            Colour colour = Colour.fromHSV((float) Math.random(), 1, 1);

            int timeoutTicks = randomFadeTime();

            Vec3 mainPos = Vec3.atBottomCenterOf(result.pos().above());
            if (result.name() != null) mainPos = mainPos.add(result.nameOffset().subtract(0, 1, 0));

            BlockDisplayBuilder mainEntity = EntityBuilders.blockDisplay(level).position(mainPos)
                    .state(MAIN_BLOCK.defaultBlockState()).scaleAndCenter(HIGHLIGHT)
                    .addTranslation(new Vector3f(0, -0.5f, 0)).glowing(true, colour);

            if (result.name() != null) {
                mainEntity.customName(result.name()).alwaysRenderName(true);
                mainEntity.addTranslation(result.nameOffset().subtract(0, 1, 0).reverse().toVector3f());
            }

            EntityLie<Display.BlockDisplay> main = EntityLie.builder(mainEntity.build())
                    .onFade((player1, lie) -> playerHighlightLies.remove(player1.getGameProfile(), lie))
                    .createAndShow(player);

            playerHighlightLies.put(player.getGameProfile(), main);

            Debris.INSTANCE.schedule(main, timeoutTicks);

            for (BlockPos otherPos : result.otherPositions()) {
                EntityLie<Display.BlockDisplay> proxy = EntityLie.builder(makeAlternateBlockDisplay(level, otherPos, colour))
                        .onFade((player1, lie) -> playerHighlightLies.remove(player1.getGameProfile(), lie))
                        .createAndShow(player);
                playerHighlightLies.put(player.getGameProfile(), proxy);
                Debris.INSTANCE.schedule(proxy, timeoutTicks);
            }
        }
    }
}
