package red.jackf.whereisit;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import red.jackf.whereisit.config.WhereIsItConfig;
import red.jackf.whereisit.criteria.VanillaCriteria;
import red.jackf.whereisit.networking.ServerboundSearchForItemPacket;
import red.jackf.whereisit.search.DefaultNestedItemStackSearchers;
import red.jackf.whereisit.search.SearchHandler;

import java.io.IOException;
import java.nio.file.Files;

public class WhereIsIt implements ModInitializer {
    public static final Logger LOGGER = LogUtils.getLogger();
	public static final String MODID = "whereisit";
	public static ResourceLocation id(String path) {
		return new ResourceLocation(MODID, path);
	}

	@Override
	public void onInitialize() {
		try {
			WhereIsItConfig.INSTANCE.load();
			WhereIsItConfig.INSTANCE.getConfig().validate();
		} catch (Exception ex) {
			LOGGER.error("Error loading WhereIsIt config, restoring default", ex);
		}
		WhereIsItConfig.INSTANCE.save();
		LOGGER.debug("Setup Common");
		VanillaCriteria.setup();
		DefaultNestedItemStackSearchers.setup();

		ServerPlayNetworking.registerGlobalReceiver(ServerboundSearchForItemPacket.TYPE, SearchHandler::handle);
	}
}