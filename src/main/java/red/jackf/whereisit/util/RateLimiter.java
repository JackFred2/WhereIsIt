package red.jackf.whereisit.util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import net.minecraft.SharedConstants;
import net.minecraft.server.level.ServerPlayer;

public class RateLimiter {
    private static final Multimap<GameProfile, Long> rateMap = ArrayListMultimap.create(8, 5);
    private static final int MAX_IN_PERIOD = 5;
    private static final int PERIOD = 5 * SharedConstants.TICKS_PER_SECOND;

    public static boolean rateLimited(ServerPlayer player, long gameTime) {
        var playerMap = rateMap.get(player.getGameProfile());
        playerMap.removeIf(l -> (gameTime - l) > PERIOD);
        return playerMap.size() >= MAX_IN_PERIOD;
    }

    public static void add(ServerPlayer player, long gameTime) {
        rateMap.put(player.getGameProfile(), gameTime);
    }

    public static void disconnected(ServerPlayer player) {
        rateMap.removeAll(player.getGameProfile());
    }
}
