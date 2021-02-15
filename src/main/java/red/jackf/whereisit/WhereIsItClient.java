package red.jackf.whereisit;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import red.jackf.whereisit.client.RenderUtils;
import red.jackf.whereisit.compat.OptifineHooks;
import red.jackf.whereisit.network.FoundS2C;
import red.jackf.whereisit.network.SearchC2S;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        WorldRenderEvents.LAST.register(context -> OptifineHooks.doOptifineAwareRender(context, (context1, simple) -> {
            RenderUtils.renderOutlines(context1, simple || WhereIsIt.CONFIG.forceSimpleRender());
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
