package red.jackf.whereisit;

import io.netty.buffer.Unpooled;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.GsonConfigSerializer;
import me.shedaniel.rei.api.REIPluginEntry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import red.jackf.whereisit.api.CustomItemBehavior;
import red.jackf.whereisit.api.CustomWorldBehavior;
import red.jackf.whereisit.api.WhereIsItEntrypoint;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class WhereIsIt implements ModInitializer {
	public static final String MODID = "whereisit";

	public static Identifier id(String path) {
		return new Identifier(MODID, path);
	}
	protected static void log(String str) {
		LOGGER.info("[Where Is It] " + str);
	}

	public static WhereIsItConfig CONFIG;

	public static final Identifier FIND_ITEM_PACKET_ID = id("find_item_c2s");
	public static final Identifier FOUND_ITEMS_PACKET_ID = id("found_item_s2c");

	public static boolean REILoaded = false;

	private static final Logger LOGGER = LogManager.getLogger();

	public static final Map<Predicate<ItemStack>, CustomItemBehavior> itemBehaviors = new HashMap<>();
	public static final Map<Predicate<BlockState>, CustomWorldBehavior> worldBehaviors = new HashMap<>();

	@Override
	public void onInitialize() {
		if (FabricLoader.getInstance().isModLoaded("roughlyenoughitems")) {
			REILoaded = true;
			log("REI Found");
		}

		AutoConfig.register(WhereIsItConfig.class, GsonConfigSerializer::new);

		CONFIG = AutoConfig.getConfigHolder(WhereIsItConfig.class).getConfig();

		// Plugins
		List<EntrypointContainer<WhereIsItEntrypoint>> entrypointContainers = FabricLoader.getInstance().getEntrypointContainers("whereisit", WhereIsItEntrypoint.class);
		entrypointContainers.sort(Comparator.comparingInt(e -> e.getEntrypoint().getPriority()));
		StringBuilder pluginList = new StringBuilder();
		for (EntrypointContainer<WhereIsItEntrypoint> entrypointContainer : entrypointContainers) {
			try {
				WhereIsItEntrypoint entrypoint = entrypointContainer.getEntrypoint();
				entrypoint.setupItemBehaviors(itemBehaviors);
				entrypoint.setupWorldBehaviors(worldBehaviors);
				pluginList.append(entrypointContainer.getProvider().getMetadata().getId() + ", ");
			} catch (Exception ex) {
				log("Error loading plugin from " + entrypointContainer.getProvider().getMetadata().getId() + ": " + ex.getLocalizedMessage());
			}
		}
		log("Loaded plugins: " + pluginList.toString().substring(0, pluginList.length() - 2));

		ServerSidePacketRegistry.INSTANCE.register(FIND_ITEM_PACKET_ID, ((packetContext, packetByteBuf) -> {
			Identifier itemId = packetByteBuf.readIdentifier();
			Item toFind = Registry.ITEM.get(itemId);
			if (toFind != Items.AIR) {
				packetContext.getTaskQueue().execute(() -> {

					BlockPos basePos = packetContext.getPlayer().getBlockPos();
					ServerWorld world = (ServerWorld) packetContext.getPlayer().getEntityWorld();

					boolean closeScreen = false;

					List<BlockPos> positions = new LinkedList<>();

					final int radius = WhereIsIt.CONFIG.searchRadius;

					BlockPos.Mutable checkPos = new BlockPos.Mutable();

					for (int y = Math.max(-radius + basePos.getY(), 0); y < Math.min(radius + 1 + basePos.getY(), world.getDimensionHeight()); y++) {
						for (int x = -radius + basePos.getX(); x < radius + 1 + basePos.getX(); x++) {
							for (int z = -radius + basePos.getZ(); z < radius + 1 + basePos.getZ(); z++) {
								checkPos.set(x, y, z);
								BlockState state = world.getBlockState(checkPos);
								try {
									for (Predicate<BlockState> predicate : worldBehaviors.keySet()) {
										if (predicate.test(state) &&
											worldBehaviors.get(predicate).containsItem(toFind, state, checkPos, world)) {
											positions.add(checkPos.toImmutable());
											closeScreen = true;
											break;
										}
									}
								} catch (Exception ex) {
									log("Error searching for item: " + ex.getLocalizedMessage());
								}
							}
						}
					}

					if (positions.size() > 0) {
						PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
						passedData.writeInt(positions.size());
						for (BlockPos pos : positions) {
							passedData.writeBlockPos(pos);
						}
						ServerSidePacketRegistry.INSTANCE.sendToPlayer(packetContext.getPlayer(), FOUND_ITEMS_PACKET_ID, passedData);
					}

					if (closeScreen) {
						((ServerPlayerEntity) packetContext.getPlayer()).closeHandledScreen();
					}

				});
			}
		}));
	}
}
