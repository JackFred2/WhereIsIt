package red.jackf.whereisit.client.plugin;

import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import red.jackf.whereisit.WhereIsIt;

public class WhereIsItClientPluginLoader {
    private static final Logger LOGGER = LogManager.getLogger(WhereIsIt.class.getCanonicalName() + "/Plugins/Client");
    private static final String KEY = "whereisit_client";
    public static void load() {
        for (var container : FabricLoader.getInstance().getEntrypointContainers(KEY, Runnable.class)) {
            LOGGER.debug("Loading entrypoint from mod {}", container.getProvider().getMetadata().getId());
            container.getEntrypoint().run();
        }
    }
}
