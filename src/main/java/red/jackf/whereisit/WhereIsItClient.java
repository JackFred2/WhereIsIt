package red.jackf.whereisit;

import me.shedaniel.cloth.api.client.events.v0.ClothClientHooks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import red.jackf.whereisit.client.PositionData;
import red.jackf.whereisit.client.ItemSearchCallback;
import red.jackf.whereisit.client.RenderUtils;
import red.jackf.whereisit.compat.OptifineHooks;
import red.jackf.whereisit.network.FoundS2C;
import red.jackf.whereisit.network.SearchC2S;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public class WhereIsItClient implements ClientModInitializer {
    public static final KeyBinding FIND_ITEMS = new KeyBinding(
        "key.whereisit.finditem",
        InputUtil.Type.KEYSYM,
        89,
        "key.categories.whereisit"
    );

    private static Item lastSearchedItem = null;
    private static NbtCompound lastSearchedTag = null;
    private static boolean lastSearchedIgnoreNbt = false;

    public static Item getLastSearchedItem() {
        return lastSearchedItem;
    }

    public static NbtCompound getLastSearchedTag() {
        return lastSearchedTag;
    }

    public static boolean lastSearchIgnoreNbt() {
        return lastSearchedIgnoreNbt;
    }

    /**
     * Triggered when a search is requested (i.e. the search key is pressed)
     */
    public static final Event<ItemSearchCallback> SEARCH_FOR_ITEM = EventFactory.createArrayBacked(ItemSearchCallback.class, listeners -> (item, matchNbt, tag) -> {
        for (ItemSearchCallback callback : listeners)
            callback.searchForItem(item, matchNbt, tag);
    });

    /**
     * Trigger a search request
     */
    public static void searchForItem(@NotNull Item item, boolean matchNbt, NbtCompound tag) {
        SEARCH_FOR_ITEM.invoker().searchForItem(item, matchNbt, tag);
        WhereIsItClient.lastSearchedItem = item;
        WhereIsItClient.lastSearchedIgnoreNbt = matchNbt;
        WhereIsItClient.lastSearchedTag = tag;
    }

    public static void handleFoundItems(Collection<PositionData> results) {
        for (PositionData result : results) {
            RenderUtils.FOUND_ITEM_POSITIONS.put(result.pos, result);
        }
    }

    public static void clearLastItem() {
        WhereIsItClient.lastSearchedItem = null;
        WhereIsItClient.lastSearchedTag = null;
        WhereIsItClient.lastSearchedIgnoreNbt = false;
    }

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(FIND_ITEMS);

        // send a request to the server
        SEARCH_FOR_ITEM.register((item, matchNbt, tag) -> ClientPlayNetworking.send(SearchC2S.ID, new SearchC2S(item, matchNbt, tag)));

        ClientPlayNetworking.registerGlobalReceiver(FoundS2C.ID, ((client, handler, buf, responseSender) -> {
            var results = FoundS2C.read(buf);

            client.execute(() -> {
                World world = handler.getWorld();
                List<PositionData> found = results.entrySet().stream().map(
                    entry -> {
                        var pos = entry.getKey();
                        var type = entry.getValue().foundType();
                        var name = entry.getValue().name();
                        return PositionData.from(pos, world.getTime(), world.getBlockState(pos).getOutlineShape(world, pos), type, name);
                    }
                ).collect(Collectors.toList());
                handleFoundItems(found);
            });
        }));

        WorldRenderEvents.AFTER_ENTITIES.register(context -> OptifineHooks.doOptifineAwareRender(context, (context1, simple) -> RenderUtils.renderTexts(context1, simple || WhereIsIt.CONFIG.forceSimpleRender())));

        WorldRenderEvents.LAST.register(context -> OptifineHooks.doOptifineAwareRender(context, (context1, simple) -> RenderUtils.renderHighlights(context1, simple || WhereIsIt.CONFIG.forceSimpleRender())));

        ClothClientHooks.SCREEN_LATE_RENDER.register(((stack, client, screen, x, y, tickDelta) -> RenderUtils.drawLastSlot(stack, screen)));

        RenderUtils.RENDER_LOCATION_EVENT.register(((context, simpleRendering, positionData) -> {
            if (!WhereIsIt.CONFIG.isRainbowMode()) return;
            Vec3f colour = RenderUtils.hueToColour(3 * context.world().getTime() + (positionData.pos.getX() + positionData.pos.getY() + positionData.pos.getZ()) * 8L);
            positionData.r = colour.getX();
            positionData.g = colour.getY();
            positionData.b = colour.getZ();
        }));
    }

}
