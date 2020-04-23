package red.jackf.whereisit;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.math.BlockPos;

import java.util.*;

import static red.jackf.whereisit.WhereIsIt.id;

public class WhereIsItClient implements ClientModInitializer {
    public static final int FOUND_ITEMS_LIFESPAN = 140;

    public static class FoundItemPos {
        public BlockPos pos;
        public long time;

        protected FoundItemPos(BlockPos pos, long time) {
            this.pos = pos;
            this.time = time;
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
                FOUND_ITEM_POSITIONS.add(new FoundItemPos(pos, packetContext.getPlayer().world.getTime()));
            });
        });
    }
}
