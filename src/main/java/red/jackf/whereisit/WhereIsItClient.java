package red.jackf.whereisit;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.block.EntityShapeContext;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static red.jackf.whereisit.WhereIsIt.id;

@Environment(EnvType.CLIENT)
public class WhereIsItClient implements ClientModInitializer {
    public static final int FOUND_ITEMS_LIFESPAN = 140;
    private static final ShapeContext SHAPE_CONTEXT = new EntityShapeContext(false, -1.7976931348623157E308D,Items.AIR) {
        public boolean isAbove(VoxelShape shape, BlockPos pos, boolean defaultValue) {
            return defaultValue;
        }
    };

    public static class FoundItemPos {
        public BlockPos pos;
        public long time;
        public VoxelShape shape;

        protected FoundItemPos(BlockPos pos, long time, VoxelShape shape) {
            this.pos = pos;
            this.time = time;
            this.shape = shape;
        }
    }

    public static final List<FoundItemPos> FOUND_ITEM_POSITIONS = new ArrayList<>();

    public static final FabricKeyBinding FIND_ITEMS = FabricKeyBinding.Builder.create(
                    id("find_items"),
                    InputUtil.Type.KEYSYM,
                    89,
                    "key.categories.inventory"
               ).build();

    @Override
    public void onInitializeClient() {
        KeyBindingRegistry.INSTANCE.register(FIND_ITEMS);

        ClientSidePacketRegistry.INSTANCE.register(WhereIsIt.FOUND_ITEMS_PACKET_ID, (packetContext, packetByteBuf) -> {
            BlockPos pos = packetByteBuf.readBlockPos();
            packetContext.getTaskQueue().execute(() -> {
                //packetContext.getPlayer().sendMessage(new LiteralText(pos.toShortString()), false);
                World world = packetContext.getPlayer().world;
                FOUND_ITEM_POSITIONS.add(new FoundItemPos(
                        pos,
                        world.getTime(),
                        world.getBlockState(pos).getOutlineShape(world, pos, SHAPE_CONTEXT)
                ));
            });
        });
    }

    public static void sendItemFindPacket(@NotNull Item item) {
        System.out.println("Finding " + item.toString());
        PacketByteBuf findItemRequest = new PacketByteBuf(Unpooled.buffer());
        findItemRequest.writeIdentifier(Registry.ITEM.getId(item));
        ClientSidePacketRegistry.INSTANCE.sendToServer(WhereIsIt.FIND_ITEM_PACKET_ID, findItemRequest);
    }
}
