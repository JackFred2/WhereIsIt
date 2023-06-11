package red.jackf.whereisit;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public class WhereIsIt implements ModInitializer {
    public static final Logger LOGGER = LogUtils.getLogger();
	public static final String MODID = "whereisit";
	public static ResourceLocation id(String path) {
		return new ResourceLocation(MODID, path);
	}

	@Override
	public void onInitialize() {
		LOGGER.debug("Setup Common");
	}
}