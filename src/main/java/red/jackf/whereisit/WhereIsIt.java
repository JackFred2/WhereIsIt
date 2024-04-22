package red.jackf.whereisit;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import red.jackf.whereisit.command.WhereIsCommand;
import red.jackf.whereisit.config.WhereIsItConfig;
import red.jackf.whereisit.networking.ClientboundResultsPacket;
import red.jackf.whereisit.networking.ServerboundSearchForItemPacket;
import red.jackf.whereisit.plugin.WhereIsItPluginLoader;
import red.jackf.whereisit.search.SearchHandler;
import red.jackf.whereisit.util.RateLimiter;

public class WhereIsIt implements ModInitializer {
    public static final Logger LOGGER = LogUtils.getLogger();
	public static final String MODID = "whereisit";
	public static ResourceLocation id(String path) {
		return new ResourceLocation(MODID, path);
	}

	@Override
	public void onInitialize() {
		try {
			WhereIsItConfig.cleanupOldConfig();
			WhereIsItConfig.INSTANCE.load();
			WhereIsItConfig.INSTANCE.instance().validate();
		} catch (Exception ex) {
			LOGGER.error("Error loading WhereIsIt config, restoring default", ex);
		}
		WhereIsItConfig.INSTANCE.save();
		LOGGER.debug("Setup Common");

		CommandRegistrationCallback.EVENT.register(WhereIsCommand::register);

		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> RateLimiter.disconnected(handler.player));

		PayloadTypeRegistry.playS2C().register(ClientboundResultsPacket.TYPE, ClientboundResultsPacket.STREAM_CODEC);
		PayloadTypeRegistry.playC2S().register(ServerboundSearchForItemPacket.TYPE, ServerboundSearchForItemPacket.STREAM_CODEC);

		ServerPlayNetworking.registerGlobalReceiver(ServerboundSearchForItemPacket.TYPE, (payload, context) ->
				SearchHandler.handleFromPacket(payload, context.player(), context.responseSender()));

		WhereIsItPluginLoader.load();
	}
}