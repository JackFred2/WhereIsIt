package red.jackf.whereisit.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import red.jackf.whereisit.api.SearchRequest;
import red.jackf.whereisit.client.api.SearchInvoker;
import red.jackf.whereisit.client.api.SearchRequestPopulator;
import red.jackf.whereisit.client.api.ShouldIgnoreKey;
import red.jackf.whereisit.client.defaults.OverlayStackBehaviorDefaults;
import red.jackf.whereisit.client.defaults.SearchInvokerDefaults;
import red.jackf.whereisit.client.defaults.SearchRequestPopulatorDefaults;
import red.jackf.whereisit.client.defaults.ShouldIgnoreKeyDefaults;
import red.jackf.whereisit.client.render.CurrentGradientHolder;
import red.jackf.whereisit.client.render.ScreenRendering;
import red.jackf.whereisit.client.render.WorldRendering;
import red.jackf.whereisit.client.util.NotificationToast;
import red.jackf.whereisit.client.util.TextUtil;
import red.jackf.whereisit.config.WhereIsItConfig;

public class WhereIsItClient implements ClientModInitializer {
    public static final Logger LOGGER = LogUtils.getLogger();
    private static final KeyMapping SEARCH = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.whereisit.search", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Y, "key.categories.whereisit"));

    private boolean inGame = false;
    @Nullable
    public static SearchRequest lastRequest = null;
    public static long lastSearchTime = 0;
    public static boolean closedScreenThisSearch = false;

    @Override
    public void onInitializeClient() {
        LOGGER.debug("Setup Client");
        CurrentGradientHolder.refreshColourScheme();

        ScreenEvents.BEFORE_INIT.register((client, _screen, scaledWidth, scaledHeight) -> {

			if (inGame) {
                if (WhereIsItConfig.INSTANCE.getConfig().getClient().showSlotHighlights)
                    ScreenEvents.afterRender(_screen).register(ScreenRendering::render);

                // listen for keypress in-GUI
                ScreenKeyboardEvents.afterKeyPress(_screen).register((screen, key, scancode, modifiers) -> {
                    if (SEARCH.matches(key, scancode) && !ShouldIgnoreKey.EVENT.invoker().shouldIgnoreKey()) {
                        SearchRequest request = createRequest(client, screen);
                        if (request.hasCriteria()) {
                            doSearch(request);
                        }
                    }
                });
            }
        });

        // don't try to search in the main menu lmao
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> inGame = true);
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> inGame = false);

        ClientTickEvents.START_WORLD_TICK.register(level -> {
            if (lastSearchTime == -1) {
                lastSearchTime = level.getGameTime();
            } else if (level.getGameTime() > lastSearchTime + WhereIsItConfig.INSTANCE.getConfig().getClient().fadeoutTimeTicks) {
                // clear rendered slots after time limit
                lastRequest = null;
                WorldRendering.clearResults();
            }

            if (WhereIsItConfig.INSTANCE.getConfig().getClient().searchUsingItemInHand && Minecraft.getInstance().screen == null && SEARCH.consumeClick()) {
                var player = Minecraft.getInstance().player;
                if (player == null) return;
                ItemStack item = player.getItemInHand(InteractionHand.MAIN_HAND);
                if (item.isEmpty()) item = player.getItemInHand(InteractionHand.OFF_HAND);
                if (item.isEmpty()) return;
                var request = new SearchRequest();
                SearchRequestPopulator.addItemStack(request, item, SearchRequestPopulator.Context.inventory());
                if (request.hasCriteria()) SearchInvoker.doSearch(request);
            }
        });

        // mod default handlers
        SearchRequestPopulator.EVENT.register(SearchRequestPopulatorDefaults::populate);
        OverlayStackBehaviorDefaults.setup();
        SearchInvokerDefaults.setup();
        ShouldIgnoreKeyDefaults.setup();

        WorldRendering.setup();
    }

    public static boolean doSearch(SearchRequest request) {
        updateRendering(request);
        LOGGER.debug("Starting request: %s".formatted(request));

        if (WhereIsItConfig.INSTANCE.getConfig().getClient().printSearchRequestsInChat && Minecraft.getInstance().player != null) {
            var text = TextUtil.prettyPrint(request.pack());
            for (Component component : text)
                Minecraft.getInstance().player.sendSystemMessage(component);
        }

        var anySucceeded = SearchInvoker.EVENT.invoker().search(request, results -> {
            WhereIsItClient.LOGGER.debug("Search results: %s".formatted(results));
            if (WhereIsItConfig.INSTANCE.getConfig().getClient().closeGuiOnFoundResults && !closedScreenThisSearch) {
                closedScreenThisSearch = true;
                if (Minecraft.getInstance().screen != null && Minecraft.getInstance().player != null)
                    Minecraft.getInstance().player.closeContainer();
            }
            WorldRendering.addResults(results);
        });
        if (!anySucceeded) NotificationToast.sendNotInstalledOnServer();
        else if (WhereIsItConfig.INSTANCE.getConfig().getClient().playSoundOnRequest) playRequestSound();

        return anySucceeded;
    }

    // clear previous state for rendering
    private static void updateRendering(SearchRequest request) {
        lastRequest = request;
        lastSearchTime = -1;
        closedScreenThisSearch = false;
        WorldRendering.clearResults();

        CurrentGradientHolder.refreshColourScheme();
    }

    @NotNull
    private static SearchRequest createRequest(Minecraft client, Screen screen1) {
        int mouseX = (int) (client.mouseHandler.xpos() * (double) client.getWindow()
                .getGuiScaledWidth() / (double) client.getWindow().getScreenWidth());
        int mouseY = (int) (client.mouseHandler.ypos() * (double) client.getWindow()
                .getGuiScaledHeight() / (double) client.getWindow().getScreenHeight());
        var request = new SearchRequest();
        SearchRequestPopulator.EVENT.invoker().grabStack(request, screen1, mouseX, mouseY);
        return request;
    }

    private static void playRequestSound() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_CHIME.value(), 2f, 0.5f));
    }
}