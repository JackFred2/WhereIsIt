package red.jackf.whereisit;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;

public class WhereIsIt implements ModInitializer {
    public static final Logger LOGGER = LogUtils.getLogger();

	@Override
	public void onInitialize() {
		LOGGER.debug("Setup Common");
	}
}