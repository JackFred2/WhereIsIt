package red.jackf.whereisit;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import red.jackf.whereisit.criteria.VanillaCriteria;
import red.jackf.whereisit.networking.ServerboundSearchForItemPacket;
import red.jackf.whereisit.search.DefaultNestedItemStackSearchers;
import red.jackf.whereisit.search.SearchHandler;

public class WhereIsIt implements ModInitializer {
    public static final Logger LOGGER = LogUtils.getLogger();
	public static final String MODID = "whereisit";
	public static ResourceLocation id(String path) {
		return new ResourceLocation(MODID, path);
	}

	@Override
	public void onInitialize() {
		LOGGER.debug("Setup Common");
		VanillaCriteria.setup();
		DefaultNestedItemStackSearchers.setup();

		ServerPlayNetworking.registerGlobalReceiver(ServerboundSearchForItemPacket.TYPE, SearchHandler::handle);
	}
}