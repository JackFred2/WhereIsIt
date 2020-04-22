package red.jackf.randomadditions;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class RandomAdditions implements ModInitializer {
	public static final String MODID = "randomadditions";

	public static Identifier id(String path) {
		return new Identifier(MODID, path);
	}

	public static final Identifier FIND_ITEM_PACKET_ID = id("find_item_c2s");
	public static final Identifier FOUND_ITEMS_PACKET_ID = id("found_item_s2c");
	private static final int FIND_ITEM_RADIUS = 12;

	@Override
	public void onInitialize() {
		ServerSidePacketRegistry.INSTANCE.register(FIND_ITEM_PACKET_ID, ((packetContext, packetByteBuf) -> {
			Identifier itemId = packetByteBuf.readIdentifier();
			if (Registry.ITEM.containsId(itemId)) {
				Item toFind = Registry.ITEM.get(itemId);
				packetContext.getTaskQueue().execute(() -> {

					BlockPos basePos = packetContext.getPlayer().getBlockPos();
					ServerWorld world = (ServerWorld) packetContext.getPlayer().getEntityWorld();
					Set<Item> findSet = new HashSet<>();
					findSet.add(toFind);

					boolean closeScreen = false;

					for (int y = Math.max(-FIND_ITEM_RADIUS + basePos.getY(), 0); y < Math.min(FIND_ITEM_RADIUS + 1 + basePos.getY(), world.getDimensionHeight()); y++) {
						for (int x = -FIND_ITEM_RADIUS + basePos.getX(); x < FIND_ITEM_RADIUS + 1 + basePos.getX(); x++) {
							for (int z = -FIND_ITEM_RADIUS + basePos.getZ(); z < FIND_ITEM_RADIUS + 1 + basePos.getZ(); z++) {
								BlockPos checkPos = new BlockPos(x, y, z);
								BlockEntity be = world.getBlockEntity(checkPos);
								if (be instanceof Inventory) {
									Inventory inv = (Inventory) be;
									if (inv.containsAny(findSet)) {
										PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
										passedData.writeBlockPos(checkPos);
										ServerSidePacketRegistry.INSTANCE.sendToPlayer(packetContext.getPlayer(), FOUND_ITEMS_PACKET_ID, passedData);

										closeScreen = true;
									}
								}
							}
						}
					}

					if (closeScreen) {
						((ServerPlayerEntity) packetContext.getPlayer()).closeHandledScreen();
					}

				});
			}
		}));
	}
}
