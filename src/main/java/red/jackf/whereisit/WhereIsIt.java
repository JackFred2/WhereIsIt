package red.jackf.whereisit;

import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.GsonConfigSerializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import red.jackf.whereisit.api.WhereIsItEntrypoint;
import red.jackf.whereisit.network.FoundS2C;
import red.jackf.whereisit.network.SearchC2S;

import java.util.*;

public class WhereIsIt implements ModInitializer {
    public static final String MODID = "whereisit";
    public static final Identifier FIND_ITEM_PACKET_ID = id("find_item_c2s");
    public static final Identifier FOUND_ITEMS_PACKET_ID = id("found_item_s2c");
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map<UUID, Long> rateLimitMap = new HashMap<>();
    public static WhereIsItConfig CONFIG;
    public static Searcher SEARCHER;
    public static boolean REILoaded = false;

    public static Identifier id(String path) {
        return new Identifier(MODID, path);
    }

    public static void log(String str) {
        LOGGER.info(str);
    }

    @Override
    public void onInitialize() {
        if (FabricLoader.getInstance().isModLoaded("roughlyenoughitems")) {
            REILoaded = true;
            log("REI Found");
        }

        AutoConfig.register(WhereIsItConfig.class, GsonConfigSerializer::new);

        CONFIG = AutoConfig.getConfigHolder(WhereIsItConfig.class).getConfig();
        SEARCHER = new Searcher();


        // Plugins
        List<EntrypointContainer<WhereIsItEntrypoint>> entrypointContainers = FabricLoader.getInstance().getEntrypointContainers("whereisit", WhereIsItEntrypoint.class);
        entrypointContainers.sort(Comparator.comparingInt(e -> e.getEntrypoint().getPriority()));
        StringBuilder pluginList = new StringBuilder();
        for (EntrypointContainer<WhereIsItEntrypoint> entrypointContainer : entrypointContainers) {
            try {
                WhereIsItEntrypoint entrypoint = entrypointContainer.getEntrypoint();
                entrypoint.setupBehaviors(SEARCHER);
                pluginList.append(entrypointContainer.getProvider().getMetadata().getId()).append(", ");
            } catch (Exception ex) {
                log("Error loading plugin from " + entrypointContainer.getProvider().getMetadata().getId() + ": " + ex.getLocalizedMessage());
            }
        }
        log("Loaded plugins: " + pluginList.substring(0, pluginList.length() - 2));

        ServerPlayNetworking.registerGlobalReceiver(FIND_ITEM_PACKET_ID, ((server, player, handler, buf, responseSender) -> {
            SearchC2S.Context searchContext = SearchC2S.read(buf);
            Item toFind = searchContext.getItem();
            if (toFind != Items.AIR) {
                server.execute(() -> {

                    BlockPos basePos = player.getBlockPos();
                    ServerWorld world = player.getServerWorld();

                    long beforeTime = System.nanoTime();

                    if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT || world.getTime() >= rateLimitMap.getOrDefault(player.getUuid(), 0L) + WhereIsIt.CONFIG.getCooldown()) {
                        Map<BlockPos, FoundType> positions = SEARCHER.searchWorld(basePos, world, toFind, searchContext.getTag());
                        if (positions.size() > 0) {
                            FoundS2C packet = new FoundS2C(positions);

                            ServerPlayNetworking.send(player, FOUND_ITEMS_PACKET_ID, packet);
                            player.closeHandledScreen();
                        }
                        rateLimitMap.put(player.getUuid(), world.getTime());
                    } else {
                        player.sendMessage(new TranslatableText("whereisit.slowDown").formatted(Formatting.YELLOW), false);
                    }

                    if (WhereIsIt.CONFIG.printSearchTime()) {
                        long time = (System.nanoTime() - beforeTime);
                        player.sendMessage(new LiteralText("Lookup Time: " + time + "ns"), false);
                        WhereIsIt.LOGGER.info("Lookup Time: " + time + "ns");
                    }
                });
            }
        }));
    }
}
