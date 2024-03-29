package red.jackf.whereisit.client.plugin;

import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import red.jackf.whereisit.WhereIsIt;
import red.jackf.whereisit.client.api.WhereIsItClientPlugin;
import red.jackf.whereisit.client.api.events.ShouldIgnoreKey;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class WhereIsItClientPluginLoader {
    private static final Logger LOGGER = LogManager.getLogger(WhereIsIt.class.getCanonicalName() + "/Plugins/Client");
    private static final String KEY = "whereisit_client";
    private static final String IGNORE_KEY_OBJECTSHARE_KEY = "whereisit:shouldignorekey";
    public static void load() {
        for (var container : FabricLoader.getInstance().getEntrypointContainers(KEY, WhereIsItClientPlugin.class)) {
            LOGGER.debug("Loading client entrypoint from mod {}", container.getProvider().getMetadata().getId());
            container.getEntrypoint().load();
        }

        Consumer<Supplier<Boolean>> eventTranslator = callback -> {
            LOGGER.debug("Registering ignore key callback from object share");
            ShouldIgnoreKey.EVENT.register(callback::get);
        };

        // Use ShouldIgnoreKey via fabric object share
        FabricLoader.getInstance().getObjectShare().put(IGNORE_KEY_OBJECTSHARE_KEY, eventTranslator);
    }
}
