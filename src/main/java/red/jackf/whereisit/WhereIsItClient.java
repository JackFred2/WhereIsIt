package red.jackf.whereisit;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import red.jackf.whereisit.network.FoundS2C;
import red.jackf.whereisit.network.SearchC2S;

import java.util.*;

@Environment(EnvType.CLIENT)
public class WhereIsItClient implements ClientModInitializer {

    public static final List<FoundItemPos> FOUND_ITEM_POSITIONS = new ArrayList<>();
    public static final Map<VoxelShape, List<Box>> CACHED_SHAPES = new HashMap<>();
    public static final KeyBinding FIND_ITEMS = new KeyBinding(
        "key.whereisit.finditem",
        InputUtil.Type.KEYSYM,
        89,
        "key.categories.whereisit"
    );

    public static void sendItemFindPacket(@NotNull Item item, boolean matchNbt, CompoundTag tag) {
        //WhereIsIt.log("Looking for " + item.toString());
        SearchC2S packet = new SearchC2S(item, matchNbt, tag);

        ClientPlayNetworking.send(WhereIsIt.FIND_ITEM_PACKET_ID, packet);
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
            vertexConsumer.vertex(matrix4f, (float) (box.minX + d), (float) (box.minY + e), (float) (box.minZ + f)).color(g, h, i, j).next();
            vertexConsumer.vertex(matrix4f, (float) (box.maxX + d), (float) (box.maxY + e), (float) (box.maxZ + f)).color(g, h, i, j).next();
        }
    }

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(FIND_ITEMS);

        ClientPlayNetworking.registerGlobalReceiver(WhereIsIt.FOUND_ITEMS_PACKET_ID, ((client, handler, buf, responseSender) -> {
            Map<BlockPos, FoundType> results = FoundS2C.read(buf);

            client.execute(() -> {
                //packetContext.getPlayer().sendMessage(new LiteralText(pos.toShortString()), false);
                World world = handler.getWorld();
                for (Map.Entry<BlockPos, FoundType> entry : results.entrySet())
                    FOUND_ITEM_POSITIONS.add(new FoundItemPos(
                        entry.getKey(),
                        world.getTime(),
                        world.getBlockState(entry.getKey()).getOutlineShape(world, entry.getKey()),
                        entry.getValue()
                    ));
            });
        }));
    }

    public static class FoundItemPos {
        public BlockPos pos;
        public long time;
        public VoxelShape shape;
        public FoundType type;

        protected FoundItemPos(BlockPos pos, long time, VoxelShape shape, FoundType type) {
            this.pos = pos;
            this.time = time;
            this.shape = shape;
            this.type = type;
        }
    }
}
