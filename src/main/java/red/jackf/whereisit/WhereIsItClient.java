package red.jackf.whereisit;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static red.jackf.whereisit.WhereIsIt.id;

@Environment(EnvType.CLIENT)
public class WhereIsItClient implements ClientModInitializer {

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
    public static final Map<VoxelShape, List<Box>> CACHED_SHAPES = new HashMap<>();

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
            int foundCount = packetByteBuf.readInt();
            List<BlockPos> positions = new LinkedList<>();
            for (int i = 0; i < foundCount; i++)
                positions.add(packetByteBuf.readBlockPos());

            packetContext.getTaskQueue().execute(() -> {
                //packetContext.getPlayer().sendMessage(new LiteralText(pos.toShortString()), false);
                World world = packetContext.getPlayer().world;
                for (BlockPos pos : positions)
                    FOUND_ITEM_POSITIONS.add(new FoundItemPos(
                            pos,
                            world.getTime(),
                            world.getBlockState(pos).getOutlineShape(world, pos)
                    ));
            });
        });
    }

    public static void sendItemFindPacket(@NotNull Item item) {
        WhereIsIt.log("Looking for " + item.toString());
        PacketByteBuf findItemRequest = new PacketByteBuf(Unpooled.buffer());
        findItemRequest.writeIdentifier(Registry.ITEM.getId(item));
        ClientSidePacketRegistry.INSTANCE.sendToServer(WhereIsIt.FIND_ITEM_PACKET_ID, findItemRequest);
    }

    public static void optimizedDrawShapeOutline(MatrixStack matrixStack, VertexConsumer vertexConsumer, VoxelShape voxelShape, double d, double e, double f, float g, float h, float i, float j) {
        Matrix4f matrix4f = matrixStack.peek().getModel();
        if (!CACHED_SHAPES.containsKey(voxelShape)) {
            //WhereIsIt.log("Adding new cached shape");
            List<Box> boxes = new LinkedList<>();
            voxelShape.forEachEdge((x1, y1, z1, x2, y2, z2) -> boxes.add(new Box(x1, y1, z1, x2, y2, z2)));
            CACHED_SHAPES.put(voxelShape, boxes);
        }

        for (Box box : CACHED_SHAPES.get(voxelShape)) {
            vertexConsumer.vertex(matrix4f, (float)(box.minX + d), (float)(box.minY + e), (float)(box.minZ + f)).color(g, h, i, j).next();
            vertexConsumer.vertex(matrix4f, (float)(box.maxX + d), (float)(box.maxY + e), (float)(box.maxZ + f)).color(g, h, i, j).next();
        }
    }
}
