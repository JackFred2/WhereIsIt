package red.jackf.whereisit;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import red.jackf.whereisit.network.FoundS2C;
import red.jackf.whereisit.network.SearchC2S;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WhereIsIt implements ModInitializer {
    public static final String MODID = "whereisit";
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map<UUID, Long> rateLimitMap = new HashMap<>();
    public static WhereIsItConfig CONFIG = AutoConfig.register(WhereIsItConfig.class, GsonConfigSerializer::new).getConfig();
    public static boolean REILoaded = false;

    public static Identifier id(String path) {
        return new Identifier(MODID, path);
    }

    public static void log(String str) {
        LOGGER.info(str);
    }

    public static void error(String str) {
        LOGGER.error(str);
    }

    public static void error(Exception ex) {
        LOGGER.error(ex);
    }

    @Override
    public void onInitialize() {
        AutoConfig.getConfigHolder(WhereIsItConfig.class).registerSaveListener((configHolder, whereIsItConfig) -> {
            whereIsItConfig.validatePostLoad();
            return ActionResult.PASS;
        });

        if (FabricLoader.getInstance().isModLoaded("roughlyenoughitems")) {
            REILoaded = true;
            log("REI Found");
        }

        ServerPlayNetworking.registerGlobalReceiver(SearchC2S.ID, ((server, player, handler, buf, responseSender) -> {
            var searchContext = SearchC2S.read(buf);
            var itemToFind = searchContext.item();
            if (itemToFind != Items.AIR) {
                server.execute(() -> {

                    var basePos = player.getBlockPos();
                    var world = player.getWorld();

                    long beforeTime = System.nanoTime();

                    if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT || world.getTime() >= rateLimitMap.getOrDefault(player.getUuid(), 0L) + WhereIsIt.CONFIG.getCooldown()) {
                        var positions = Searcher.searchWorld(basePos, world, itemToFind, searchContext.tag(), searchContext.maximum());
                        if (positions.size() > 0) {
                            var packet = new FoundS2C(positions);
                            ServerPlayNetworking.send(player, FoundS2C.ID, packet);
                            player.closeHandledScreen();
                        }
                        rateLimitMap.put(player.getUuid(), world.getTime());
                    } else {
                        player.sendMessage(Text.translatable("whereisit.slowDown").formatted(Formatting.YELLOW), false);
                    }

                    if (WhereIsIt.CONFIG.printSearchTime()) {
                        long time = (System.nanoTime() - beforeTime);
                        player.sendMessage(Text.literal("Lookup Time: " + time + "ns"), false);
                        WhereIsIt.LOGGER.info("Lookup Time: " + time + "ns");
                    }
                });
            }
        }));
    }
}
