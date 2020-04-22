package red.jackf.randomadditions;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static red.jackf.randomadditions.RandomAdditions.id;

public class RandomAdditionsClient implements ClientModInitializer {
    public static final int FOUND_ITEMS_LIFESPAN = 60;

    public static class FoundItemPos {
        public BlockPos pos;
        public long time;

        protected FoundItemPos(BlockPos pos, long time) {
            this.pos = pos;
            this.time = time;
        }
    }

    public static final List<FoundItemPos> FOUND_ITEM_POSITIONS = new LinkedList<>();

    public static final FabricKeyBinding FIND_ITEMS = FabricKeyBinding.Builder.create(
                    id("key.find_items"),
                    InputUtil.Type.KEYSYM,
                    84,
                    "key.categories.inventory"
               ).build();

    @Override
    public void onInitializeClient() {
        KeyBindingRegistry.INSTANCE.register(FIND_ITEMS);

        ClientSidePacketRegistry.INSTANCE.register(RandomAdditions.FOUND_ITEMS_PACKET_ID, (packetContext, packetByteBuf) -> {
            BlockPos pos = packetByteBuf.readBlockPos();
            packetContext.getTaskQueue().execute(() -> {
                //packetContext.getPlayer().sendMessage(new LiteralText(pos.toShortString()), false);
                if (MinecraftClient.getInstance().currentScreen instanceof HandledScreen) {
                    MinecraftClient.getInstance().currentScreen.onClose();
                }
                FOUND_ITEM_POSITIONS.add(new FoundItemPos(pos, packetContext.getPlayer().world.getTime()));
            });
        });
    }
}
